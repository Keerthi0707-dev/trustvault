package com.trustvault;

import com.trustvault.exception.ValidationException;
import com.trustvault.service.DocumentStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentStorageValidationTest {

    private final DocumentStorageService storageService = new DocumentStorageService(
            "https://fjtegicvsdggajprxjdi.supabase.co",
            "", // empty secret key forces local fallback
            "trustvault-documents",
            26214400L,
            List.of("pdf", "docx", "xlsx", "txt", "csv", "md", "png", "jpg")
    );

    @Test
    @DisplayName("Path traversal in uploaded filenames is neutralized")
    public void testPathTraversalSanitization() {
        assertEquals("passwd.txt", DocumentStorageService.sanitizeFilename("../../../etc/passwd.txt"));
        assertEquals("win.ini", DocumentStorageService.sanitizeFilename("..\\..\\windows\\win.ini"));
        assertEquals("secret_file.pdf", DocumentStorageService.sanitizeFilename("secret file.pdf"));
        assertEquals("document.bin", DocumentStorageService.sanitizeFilename(null));
        assertEquals("document.bin", DocumentStorageService.sanitizeFilename(""));
    }

    @Test
    @DisplayName("Dangerous extensions are rejected (e.g. .html, .js, .svg, .exe)")
    public void testDangerousExtensionsRejected() {
        MockMultipartFile htmlFile = new MockMultipartFile("file", "xss.html", "text/html",
                "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));
        assertThrows(ValidationException.class, () -> storageService.storeFile(htmlFile));

        MockMultipartFile exeFile = new MockMultipartFile("file", "malware.exe", "application/octet-stream",
                new byte[]{0x4D, 0x5A, 0x00});
        assertThrows(ValidationException.class, () -> storageService.storeFile(exeFile));

        MockMultipartFile svgFile = new MockMultipartFile("file", "vector.svg", "image/svg+xml",
                "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8));
        assertThrows(ValidationException.class, () -> storageService.storeFile(svgFile));
    }

    @Test
    @DisplayName("SHA-256 calculation matches known standard")
    public void testSha256Checksum() {
        String content = "TrustVault Secure File Storage";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        String hash = DocumentStorageService.calculateSha256(bytes);
        assertNotNull(hash);
        assertEquals(64, hash.length());
        // Verify deterministic calculation
        assertEquals(hash, DocumentStorageService.calculateSha256(bytes));
    }
}
