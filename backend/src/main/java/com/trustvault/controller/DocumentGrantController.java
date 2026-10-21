package com.trustvault.controller;

import com.trustvault.dto.CreateGrantRequest;
import com.trustvault.dto.GrantDto;
import com.trustvault.entity.User;
import com.trustvault.exception.AccessDeniedException;
import com.trustvault.security.SecurityUtils;
import com.trustvault.service.DocumentGrantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents/{id}/grants")
public class DocumentGrantController {

    private final DocumentGrantService documentGrantService;

    public DocumentGrantController(DocumentGrantService documentGrantService) {
        this.documentGrantService = documentGrantService;
    }

    @GetMapping
    public ResponseEntity<List<GrantDto>> listGrants(@PathVariable("id") UUID documentId) {
        User currentUser = getCurrentUser();
        List<GrantDto> grants = documentGrantService.listGrants(documentId, currentUser);
        return ResponseEntity.ok(grants);
    }

    @PostMapping
    public ResponseEntity<GrantDto> createGrant(@PathVariable("id") UUID documentId,
                                                @Valid @RequestBody CreateGrantRequest request) {
        User currentUser = getCurrentUser();
        GrantDto created = documentGrantService.createGrant(documentId, request, currentUser);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{grantId}")
    public ResponseEntity<Map<String, String>> revokeGrant(@PathVariable("id") UUID documentId,
                                                          @PathVariable("grantId") UUID grantId) {
        User currentUser = getCurrentUser();
        documentGrantService.revokeGrant(documentId, grantId, currentUser);
        return ResponseEntity.ok(Map.of("message", "Permission grant revoked immediately"));
    }

    private User getCurrentUser() {
        return SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));
    }
}
