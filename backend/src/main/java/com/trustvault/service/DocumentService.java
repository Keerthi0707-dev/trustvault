package com.trustvault.service;

import com.trustvault.dto.DocumentDto;
import com.trustvault.dto.DocumentUpdateRequest;
import com.trustvault.entity.*;
import com.trustvault.exception.ResourceNotFoundException;
import com.trustvault.exception.ValidationException;
import com.trustvault.repository.DocumentRepository;
import com.trustvault.security.SecurityUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentAuthorizationService authorizationService;
    private final DocumentStorageService storageService;
    private final AuditService auditService;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentAuthorizationService authorizationService,
                           DocumentStorageService storageService,
                           AuditService auditService) {
        this.documentRepository = documentRepository;
        this.authorizationService = authorizationService;
        this.storageService = storageService;
        this.auditService = auditService;
    }

    @Transactional
    public DocumentDto uploadDocument(MultipartFile file, String category, String description, User currentUser) {
        authorizationService.checkCanUpload(currentUser);

        if (category == null || category.trim().isBlank()) {
            category = "General";
        }
        category = category.trim();

        DocumentStorageService.StoredFileMetadata storedMeta = storageService.storeFile(file);

        try {
            Document document = new Document(
                    UUID.randomUUID(),
                    currentUser,
                    storedMeta.getSanitizedFilename(),
                    storedMeta.getSafeStorageKey(),
                    storedMeta.getDetectedMimeType(),
                    storedMeta.getFileSize(),
                    category,
                    description != null ? description.trim() : null,
                    storedMeta.getSha256Checksum()
            );

            Document saved = documentRepository.save(document);

            auditService.recordEvent(
                    currentUser.getId(),
                    AuditAction.DOCUMENT_UPLOAD,
                    saved.getId(),
                    null,
                    AuditOutcome.SUCCESS,
                    SecurityUtils.getCorrelationId(),
                    String.format("Uploaded document '%s' (category=%s, size=%d bytes, checksum=%s)",
                            saved.getOriginalDisplayName(), saved.getCategory(), saved.getFileSize(), saved.getSha256Checksum())
            );

            return toDto(saved, "OWNER");
        } catch (Exception e) {
            // Clean up storage object on failure to avoid orphans
            storageService.deleteFile(storedMeta.getSafeStorageKey());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public DocumentDto getDocumentMetadata(UUID documentId, User currentUser) {
        Document document = documentRepository.findByIdAndIsDeletedFalse(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        authorizationService.checkCanViewMetadata(currentUser, document);

        String permission = authorizationService.getEffectivePermission(currentUser, document);

        auditService.recordEvent(
                currentUser.getId(),
                AuditAction.DOCUMENT_READ,
                document.getId(),
                null,
                AuditOutcome.SUCCESS,
                SecurityUtils.getCorrelationId(),
                "Accessed metadata for document: " + document.getOriginalDisplayName()
        );

        return toDto(document, permission);
    }

    @Transactional(readOnly = true)
    public DocumentDownloadResult getDocumentDownload(UUID documentId, User currentUser) {
        Document document = documentRepository.findByIdAndIsDeletedFalse(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        authorizationService.checkCanReadOrDownload(currentUser, document);

        Resource resource = storageService.retrieveFile(document.getSafeStorageObjectKey());

        auditService.recordEvent(
                currentUser.getId(),
                AuditAction.DOCUMENT_DOWNLOAD,
                document.getId(),
                null,
                AuditOutcome.SUCCESS,
                SecurityUtils.getCorrelationId(),
                "Downloaded document: " + document.getOriginalDisplayName()
        );

        return new DocumentDownloadResult(resource, document);
    }

    @Transactional
    public DocumentDto updateDocumentMetadata(UUID documentId, DocumentUpdateRequest request, User currentUser) {
        Document document = documentRepository.findByIdAndIsDeletedFalse(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        authorizationService.checkCanEdit(currentUser, document);

        if (request.getOriginalDisplayName() != null && !request.getOriginalDisplayName().isBlank()) {
            document.setOriginalDisplayName(DocumentStorageService.sanitizeFilename(request.getOriginalDisplayName().trim()));
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            document.setCategory(request.getCategory().trim());
        }
        if (request.getDescription() != null) {
            document.setDescription(request.getDescription().trim());
        }

        Document updated = documentRepository.save(document);

        auditService.recordEvent(
                currentUser.getId(),
                AuditAction.DOCUMENT_EDIT,
                updated.getId(),
                null,
                AuditOutcome.SUCCESS,
                SecurityUtils.getCorrelationId(),
                "Updated metadata for document: " + updated.getOriginalDisplayName()
        );

        String permission = authorizationService.getEffectivePermission(currentUser, updated);
        return toDto(updated, permission);
    }

    @Transactional
    public void deleteDocument(UUID documentId, User currentUser) {
        Document document = documentRepository.findByIdAndIsDeletedFalse(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        authorizationService.checkCanDelete(currentUser, document);

        document.setDeleted(true);
        document.setDeletedAt(Instant.now());
        documentRepository.save(document);

        auditService.recordEvent(
                currentUser.getId(),
                AuditAction.DOCUMENT_DELETE,
                document.getId(),
                null,
                AuditOutcome.SUCCESS,
                SecurityUtils.getCorrelationId(),
                "Soft-deleted document: " + document.getOriginalDisplayName()
        );
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> searchDocuments(String query, String category, User currentUser) {
        authorizationService.checkUserActive(currentUser);

        String sanitizedQuery = (query != null && !query.trim().isBlank()) ? query.trim() : null;
        String sanitizedCategory = (category != null && !category.trim().isBlank()) ? category.trim() : null;

        List<Document> documents;
        if (sanitizedQuery == null && sanitizedCategory == null) {
            documents = documentRepository.findAllAccessibleToUser(currentUser.getId());
        } else if (sanitizedQuery != null && sanitizedCategory != null) {
            documents = documentRepository.searchAccessibleWithQueryAndCategory(
                    currentUser.getId(), sanitizedQuery, sanitizedCategory);
        } else if (sanitizedQuery != null) {
            documents = documentRepository.searchAccessibleWithQuery(
                    currentUser.getId(), sanitizedQuery);
        } else {
            documents = documentRepository.searchAccessibleWithCategory(
                    currentUser.getId(), sanitizedCategory);
        }

        return documents.stream().map(doc -> {
            String permission = authorizationService.getEffectivePermission(currentUser, doc);
            return toDto(doc, permission);
        }).toList();
    }

    public DocumentDto toDto(Document document, String permission) {
        return new DocumentDto(
                document.getId(),
                document.getOriginalDisplayName(),
                document.getMimeType(),
                document.getFileSize(),
                document.getCategory(),
                document.getDescription(),
                document.getSha256Checksum(),
                document.getOwner().getId(),
                document.getOwner().getEmail(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                permission
        );
    }

    public static class DocumentDownloadResult {
        private final Resource resource;
        private final Document document;

        public DocumentDownloadResult(Resource resource, Document document) {
            this.resource = resource;
            this.document = document;
        }

        public Resource getResource() { return resource; }
        public Document getDocument() { return document; }
    }
}
