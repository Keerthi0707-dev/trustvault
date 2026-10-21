package com.trustvault.service;

import com.trustvault.exception.ValidationException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(DocumentStorageService.class);

    private final String supabaseUrl;
    private final String supabaseSecretKey;
    private final String bucketName;
    private final long maxFileSizeBytes;
    private final Set<String> allowedExtensions;
    private final Tika tika = new Tika();
    private final RestClient restClient;
    private final Path localFallbackDir;

    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            "html", "htm", "js", "jsp", "svg", "exe", "sh", "bat", "cmd", "vbs", "ps1", "php", "py", "jar", "war"
    );

    public DocumentStorageService(
            @Value("${trustvault.supabase.url:https://fjtegicvsdggajprxjdi.supabase.co}") String supabaseUrl,
            @Value("${trustvault.supabase.secret-key:}") String supabaseSecretKey,
            @Value("${trustvault.supabase.storage-bucket:trustvault-documents}") String bucketName,
            @Value("${trustvault.upload.max-file-size-bytes:26214400}") long maxFileSizeBytes,
            @Value("${trustvault.upload.allowed-extensions:pdf,docx,xlsx,pptx,txt,csv,md,png,jpg,jpeg,webp}") List<String> allowedExtensionsList) {

        this.supabaseUrl = supabaseUrl != null ? supabaseUrl.replaceAll("/+$", "") : "";
        this.supabaseSecretKey = supabaseSecretKey != null ? supabaseSecretKey.trim() : "";
        this.bucketName = bucketName;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.allowedExtensions = new HashSet<>(allowedExtensionsList.stream().map(String::toLowerCase).toList());
        this.restClient = RestClient.builder().build();

        // Local fallback storage directory
        this.localFallbackDir = Paths.get(System.getProperty("java.io.tmpdir"), "trustvault_storage");
        try {
            Files.createDirectories(this.localFallbackDir);
        } catch (IOException e) {
            log.error("Failed to initialize local fallback storage directory", e);
        }
    }

    public static class StoredFileMetadata {
        private final String safeStorageKey;
        private final String sanitizedFilename;
        private final String detectedMimeType;
        private final long fileSize;
        private final String sha256Checksum;

        public StoredFileMetadata(String safeStorageKey, String sanitizedFilename, String detectedMimeType,
                                  long fileSize, String sha256Checksum) {
            this.safeStorageKey = safeStorageKey;
            this.sanitizedFilename = sanitizedFilename;
            this.detectedMimeType = detectedMimeType;
            this.fileSize = fileSize;
            this.sha256Checksum = sha256Checksum;
        }

        public String getSafeStorageKey() { return safeStorageKey; }
        public String getSanitizedFilename() { return sanitizedFilename; }
        public String getDetectedMimeType() { return detectedMimeType; }
        public long getFileSize() { return fileSize; }
        public String getSha256Checksum() { return sha256Checksum; }
    }

    /**
     * Validates and securely stores an uploaded file into Supabase Storage or secure fallback.
     */
    public StoredFileMetadata storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Uploaded file cannot be empty");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new ValidationException("File size exceeds maximum allowed limit of " + (maxFileSizeBytes / (1024 * 1024)) + "MB");
        }

        String rawFilename = file.getOriginalFilename();
        String sanitizedFilename = sanitizeFilename(rawFilename);
        String extension = getFileExtension(sanitizedFilename);

        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            throw new ValidationException("File type ." + extension + " is prohibited for security reasons");
        }

        if (!allowedExtensions.contains(extension)) {
            throw new ValidationException("File extension ." + extension + " is not in the allowed list");
        }

        byte[] contentBytes;
        try {
            contentBytes = file.getBytes();
        } catch (IOException e) {
            throw new ValidationException("Failed to read uploaded file stream");
        }

        // Tika signature / magic bytes MIME inspection
        String detectedMimeType = tika.detect(contentBytes, sanitizedFilename);
        if (detectedMimeType.toLowerCase().contains("html") || detectedMimeType.toLowerCase().contains("javascript")
                || detectedMimeType.toLowerCase().contains("xml") && extension.equals("svg")) {
            throw new ValidationException("Uploaded content contains executable script or HTML signatures");
        }

        String sha256Checksum = calculateSha256(contentBytes);
        String safeStorageKey = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + ".bin";

        // Store to Supabase or fallback
        if (isSupabaseConfigured()) {
            uploadToSupabase(safeStorageKey, contentBytes, detectedMimeType);
        } else {
            uploadToLocalFallback(safeStorageKey, contentBytes);
        }

        return new StoredFileMetadata(safeStorageKey, sanitizedFilename, detectedMimeType, file.getSize(), sha256Checksum);
    }

    /**
     * Downloads file content as a Resource directly mediated by the backend.
     */
    public Resource retrieveFile(String safeStorageKey) {
        if (safeStorageKey == null || safeStorageKey.contains("..") || safeStorageKey.contains("/") || safeStorageKey.contains("\\")) {
            throw new ValidationException("Invalid storage key");
        }

        byte[] data;
        if (isSupabaseConfigured()) {
            data = downloadFromSupabase(safeStorageKey);
        } else {
            data = downloadFromLocalFallback(safeStorageKey);
        }

        return new ByteArrayResource(data);
    }

    /**
     * Cleans up file object in storage if a transaction or operation is aborted.
     */
    public void deleteFile(String safeStorageKey) {
        if (safeStorageKey == null || safeStorageKey.isBlank()) {
            return;
        }

        try {
            if (isSupabaseConfigured()) {
                deleteFromSupabase(safeStorageKey);
            } else {
                deleteFromLocalFallback(safeStorageKey);
            }
        } catch (Exception e) {
            log.warn("Failed to delete storage object {}: {}", safeStorageKey, e.getMessage());
        }
    }

    private boolean isSupabaseConfigured() {
        return supabaseSecretKey != null && !supabaseSecretKey.isBlank() && !supabaseSecretKey.contains("your_supabase");
    }

    private void uploadToSupabase(String objectKey, byte[] data, String mimeType) {
        String uploadEndpoint = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, objectKey);
        try {
            restClient.post()
                    .uri(uploadEndpoint)
                    .header("Authorization", "Bearer " + supabaseSecretKey)
                    .header("apikey", supabaseSecretKey)
                    .header(HttpHeaders.CONTENT_TYPE, mimeType)
                    .body(data)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Uploaded object {} to Supabase Storage bucket {}", objectKey, bucketName);
        } catch (Exception e) {
            log.error("Failed to upload object {} to Supabase: {}", objectKey, e.getMessage());
            throw new StorageException("Failed to upload document to private storage provider", e);
        }
    }

    private byte[] downloadFromSupabase(String objectKey) {
        String downloadEndpoint = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, objectKey);
        try {
            return restClient.get()
                    .uri(downloadEndpoint)
                    .header("Authorization", "Bearer " + supabaseSecretKey)
                    .header("apikey", supabaseSecretKey)
                    .retrieve()
                    .body(byte[].class);
        } catch (Exception e) {
            log.error("Failed to download object {} from Supabase: {}", objectKey, e.getMessage());
            throw new StorageException("Failed to retrieve document from private storage provider", e);
        }
    }

    private void deleteFromSupabase(String objectKey) {
        String deleteEndpoint = String.format("%s/storage/v1/object/%s", supabaseUrl, bucketName);
        Map<String, Object> body = Map.of("prefixes", List.of(objectKey));
        restClient.method(HttpMethod.DELETE)
                .uri(deleteEndpoint)
                .header("Authorization", "Bearer " + supabaseSecretKey)
                .header("apikey", supabaseSecretKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private void uploadToLocalFallback(String objectKey, byte[] data) {
        try {
            Path target = localFallbackDir.resolve(objectKey);
            Files.write(target, data);
            log.info("Stored object {} in local secure vault storage", objectKey);
        } catch (IOException e) {
            throw new StorageException("Failed to store file in local vault", e);
        }
    }

    private byte[] downloadFromLocalFallback(String objectKey) {
        try {
            Path target = localFallbackDir.resolve(objectKey);
            if (!Files.exists(target)) {
                throw new StorageException("File object not found in vault storage");
            }
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new StorageException("Failed to read file from vault", e);
        }
    }

    private void deleteFromLocalFallback(String objectKey) {
        try {
            Path target = localFallbackDir.resolve(objectKey);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Failed to delete local fallback file {}: {}", objectKey, e.getMessage());
        }
    }

    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "document.bin";
        }
        // Extract base name, strip directory paths
        String name = Paths.get(filename).getFileName().toString();
        // Replace spaces and special characters except alphanumeric, underscore, dot, hyphen
        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (name.isBlank() || name.equals(".")) {
            return "document.bin";
        }
        return name;
    }

    public static String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    public static String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static class StorageException extends RuntimeException {
        public StorageException(String message) {
            super(message);
        }
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
