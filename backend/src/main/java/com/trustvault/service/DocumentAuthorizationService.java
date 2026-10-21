package com.trustvault.service;

import com.trustvault.entity.*;
import com.trustvault.exception.AccessDeniedException;
import com.trustvault.exception.AccountSuspendedException;
import com.trustvault.exception.ResourceNotFoundException;
import com.trustvault.repository.DocumentGrantRepository;
import com.trustvault.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DocumentAuthorizationService {

    private final DocumentGrantRepository documentGrantRepository;
    private final AuditService auditService;

    public DocumentAuthorizationService(DocumentGrantRepository documentGrantRepository,
                                        AuditService auditService) {
        this.documentGrantRepository = documentGrantRepository;
        this.auditService = auditService;
    }

    /**
     * Confirms the account is ACTIVE. Throws AccountSuspendedException if disabled or suspended.
     */
    public void checkUserActive(User user) {
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            auditService.recordEvent(
                    user.getId(),
                    AuditAction.ACCESS_DENIED,
                    null,
                    user.getId(),
                    AuditOutcome.DENIAL,
                    SecurityUtils.getCorrelationId(),
                    "Account status is " + user.getStatus() + " - access denied"
            );
            throw new AccountSuspendedException("Your account is " + user.getStatus().name().toLowerCase() + ". Access is denied.");
        }
    }

    /**
     * Checks if user has permission to upload new documents.
     */
    public void checkCanUpload(User user) {
        checkUserActive(user);
        if (user.getRole() != Role.OWNER && user.getRole() != Role.ADMIN) {
            auditService.recordEvent(
                    user.getId(),
                    AuditAction.ACCESS_DENIED,
                    null,
                    null,
                    AuditOutcome.DENIAL,
                    SecurityUtils.getCorrelationId(),
                    "User role " + user.getRole() + " cannot upload documents"
            );
            throw new AccessDeniedException("Your role does not have permission to upload documents");
        }
    }

    /**
     * Checks if user can view document metadata.
     * Uses generic 404 to prevent IDOR enumeration.
     */
    @Transactional(readOnly = true)
    public void checkCanViewMetadata(User user, Document document) {
        checkUserActive(user);
        if (document.isDeleted()) {
            throw new ResourceNotFoundException("Document not found");
        }

        if (document.getOwner().getId().equals(user.getId())) {
            return;
        }

        boolean hasGrant = documentGrantRepository.hasActiveGrant(document.getId(), user.getId());
        if (!hasGrant) {
            auditService.recordEvent(
                    user.getId(),
                    AuditAction.ACCESS_DENIED,
                    document.getId(),
                    null,
                    AuditOutcome.DENIAL,
                    SecurityUtils.getCorrelationId(),
                    "Attempted unauthorized metadata access"
            );
            // Return 404 to avoid leaking whether inaccessible document exists
            throw new ResourceNotFoundException("Document not found");
        }
    }

    /**
     * Checks if user can download or read document content.
     */
    @Transactional(readOnly = true)
    public void checkCanReadOrDownload(User user, Document document) {
        checkUserActive(user);
        if (document.isDeleted()) {
            throw new ResourceNotFoundException("Document not found");
        }

        if (document.getOwner().getId().equals(user.getId())) {
            return;
        }

        Optional<DocumentGrant> grantOpt = documentGrantRepository.findActiveGrant(document.getId(), user.getId());
        if (grantOpt.isEmpty()) {
            auditService.recordEvent(
                    user.getId(),
                    AuditAction.DOCUMENT_DOWNLOAD,
                    document.getId(),
                    null,
                    AuditOutcome.DENIAL,
                    SecurityUtils.getCorrelationId(),
                    "Attempted unauthorized download without active grant"
            );
            throw new ResourceNotFoundException("Document not found");
        }
    }

    /**
     * Checks if user can edit document metadata.
     * Requires OWNER or EDIT grant.
     */
    @Transactional(readOnly = true)
    public void checkCanEdit(User user, Document document) {
        checkUserActive(user);
        if (document.isDeleted()) {
            throw new ResourceNotFoundException("Document not found");
        }

        if (document.getOwner().getId().equals(user.getId())) {
            return;
        }

        Optional<DocumentGrant> grantOpt = documentGrantRepository.findActiveGrant(document.getId(), user.getId());
        if (grantOpt.isPresent() && grantOpt.get().getPermission() == GrantPermission.EDIT) {
            return;
        }

        auditService.recordEvent(
                user.getId(),
                AuditAction.DOCUMENT_EDIT,
                document.getId(),
                null,
                AuditOutcome.DENIAL,
                SecurityUtils.getCorrelationId(),
                "Attempted unauthorized edit without EDIT permission"
        );
        throw new AccessDeniedException("You do not have permission to edit this document");
    }

    /**
     * Checks if user can delete document.
     * Only document owner is permitted.
     */
    public void checkCanDelete(User user, Document document) {
        checkUserActive(user);
        if (document.isDeleted()) {
            throw new ResourceNotFoundException("Document not found");
        }

        if (!document.getOwner().getId().equals(user.getId())) {
            auditService.recordEvent(
                    user.getId(),
                    AuditAction.DOCUMENT_DELETE,
                    document.getId(),
                    null,
                    AuditOutcome.DENIAL,
                    SecurityUtils.getCorrelationId(),
                    "Non-owner attempted document deletion"
            );
            throw new AccessDeniedException("Only the document owner can delete this document");
        }
    }

    /**
     * Checks if user can manage grants (grant/revoke access).
     * Only document owner is permitted. EDIT permission does NOT allow grant management.
     */
    public void checkCanManageGrants(User user, Document document) {
        checkUserActive(user);
        if (document.isDeleted()) {
            throw new ResourceNotFoundException("Document not found");
        }

        if (!document.getOwner().getId().equals(user.getId())) {
            auditService.recordEvent(
                    user.getId(),
                    AuditAction.ACCESS_DENIED,
                    document.getId(),
                    null,
                    AuditOutcome.DENIAL,
                    SecurityUtils.getCorrelationId(),
                    "Non-owner attempted grant management"
            );
            throw new AccessDeniedException("Only the document owner can manage sharing permissions");
        }
    }

    /**
     * Checks if user can view document audit history.
     * Document owner or Administrator.
     */
    public void checkCanViewDocumentAudit(User user, Document document) {
        checkUserActive(user);
        if (document.getOwner().getId().equals(user.getId()) || user.getRole() == Role.ADMIN) {
            return;
        }
        throw new AccessDeniedException("Only document owner or administrator can view document audit events");
    }

    /**
     * Checks if user has ADMIN role.
     */
    public void checkAdmin(User user) {
        checkUserActive(user);
        if (user.getRole() != Role.ADMIN) {
            auditService.recordEvent(
                    user.getId(),
                    AuditAction.ACCESS_DENIED,
                    null,
                    null,
                    AuditOutcome.DENIAL,
                    SecurityUtils.getCorrelationId(),
                    "Non-admin attempted administrative action"
            );
            throw new AccessDeniedException("Administrative privileges required");
        }
    }

    /**
     * Evaluates effective permission for current user context: "OWNER", "EDIT", "READ".
     */
    @Transactional(readOnly = true)
    public String getEffectivePermission(User user, Document document) {
        if (document.getOwner().getId().equals(user.getId())) {
            return "OWNER";
        }
        Optional<DocumentGrant> grantOpt = documentGrantRepository.findActiveGrant(document.getId(), user.getId());
        return grantOpt.map(g -> g.getPermission().name()).orElse("NONE");
    }
}
