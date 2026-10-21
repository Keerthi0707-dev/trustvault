package com.trustvault.controller;

import com.trustvault.dto.AuditEventDto;
import com.trustvault.dto.DocumentDto;
import com.trustvault.dto.DocumentUpdateRequest;
import com.trustvault.entity.AuditEvent;
import com.trustvault.entity.Document;
import com.trustvault.entity.User;
import com.trustvault.exception.AccessDeniedException;
import com.trustvault.exception.ResourceNotFoundException;
import com.trustvault.repository.AuditEventRepository;
import com.trustvault.repository.DocumentRepository;
import com.trustvault.repository.UserRepository;
import com.trustvault.security.SecurityUtils;
import com.trustvault.service.DocumentAuthorizationService;
import com.trustvault.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final DocumentAuthorizationService authorizationService;
    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;

    public DocumentController(DocumentService documentService,
                              DocumentRepository documentRepository,
                              DocumentAuthorizationService authorizationService,
                              AuditEventRepository auditEventRepository,
                              UserRepository userRepository) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
        this.authorizationService = authorizationService;
        this.auditEventRepository = auditEventRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto>> listDocuments(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false) String category) {

        User currentUser = getCurrentUser();
        List<DocumentDto> docs = documentService.searchDocuments(search, category, currentUser);
        return ResponseEntity.ok(docs);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false, defaultValue = "General") String category,
            @RequestParam(value = "description", required = false) String description) {

        User currentUser = getCurrentUser();
        DocumentDto created = documentService.uploadDocument(file, category, description, currentUser);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable("id") UUID id) {
        User currentUser = getCurrentUser();
        DocumentDto doc = documentService.getDocumentMetadata(id, currentUser);
        return ResponseEntity.ok(doc);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DocumentDto> updateDocument(
            @PathVariable("id") UUID id,
            @Valid @RequestBody DocumentUpdateRequest request) {

        User currentUser = getCurrentUser();
        DocumentDto updated = documentService.updateDocumentMetadata(id, request, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable("id") UUID id) {
        User currentUser = getCurrentUser();
        documentService.deleteDocument(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Document successfully deleted"));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("id") UUID id) {
        User currentUser = getCurrentUser();
        DocumentService.DocumentDownloadResult result = documentService.getDocumentDownload(id, currentUser);
        Document doc = result.getDocument();

        // Safe filename header avoiding header injection / response splitting
        String safeHeaderFilename = doc.getOriginalDisplayName().replaceAll("[\r\n\"]", "_");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeHeaderFilename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, private")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'")
                .contentType(MediaType.parseMediaType(doc.getMimeType()))
                .contentLength(doc.getFileSize())
                .body(result.getResource());
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<AuditEventDto>> getDocumentAudit(@PathVariable("id") UUID id) {
        User currentUser = getCurrentUser();
        Document document = documentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        authorizationService.checkCanViewDocumentAudit(currentUser, document);

        List<AuditEvent> events = auditEventRepository.findByDocumentIdOrderByTimestampDesc(id);

        Map<UUID, String> emailMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getEmail, (a, b) -> a));

        List<AuditEventDto> dtos = events.stream().map(e -> new AuditEventDto(
                e.getId(),
                e.getTimestamp(),
                e.getActorId(),
                e.getActorId() != null ? emailMap.get(e.getActorId()) : "System/Anonymous",
                e.getAction(),
                e.getDocumentId(),
                e.getTargetUserId(),
                e.getTargetUserId() != null ? emailMap.get(e.getTargetUserId()) : null,
                e.getOutcome(),
                e.getCorrelationId(),
                e.getDetails(),
                e.getPreviousEventHash(),
                e.getCurrentEventHash()
        )).toList();

        return ResponseEntity.ok(dtos);
    }

    private User getCurrentUser() {
        return SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));
    }
}
