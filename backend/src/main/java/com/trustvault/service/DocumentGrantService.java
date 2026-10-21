package com.trustvault.service;

import com.trustvault.dto.CreateGrantRequest;
import com.trustvault.dto.GrantDto;
import com.trustvault.entity.*;
import com.trustvault.exception.AccessDeniedException;
import com.trustvault.exception.ResourceNotFoundException;
import com.trustvault.exception.ValidationException;
import com.trustvault.repository.DocumentGrantRepository;
import com.trustvault.repository.DocumentRepository;
import com.trustvault.repository.UserRepository;
import com.trustvault.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentGrantService {

    private final DocumentRepository documentRepository;
    private final DocumentGrantRepository documentGrantRepository;
    private final UserRepository userRepository;
    private final DocumentAuthorizationService authorizationService;
    private final AuditService auditService;

    public DocumentGrantService(DocumentRepository documentRepository,
                                DocumentGrantRepository documentGrantRepository,
                                UserRepository userRepository,
                                DocumentAuthorizationService authorizationService,
                                AuditService auditService) {
        this.documentRepository = documentRepository;
        this.documentGrantRepository = documentGrantRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    @Transactional
    public GrantDto createGrant(UUID documentId, CreateGrantRequest request, User currentUser) {
        Document document = documentRepository.findByIdAndIsDeletedFalse(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        authorizationService.checkCanManageGrants(currentUser, document);

        User recipient = userRepository.findByEmailIgnoreCase(request.getRecipientEmail().trim())
                .orElseThrow(() -> new ValidationException("Recipient user with email '" + request.getRecipientEmail() + "' does not exist"));

        if (recipient.getId().equals(currentUser.getId()) || recipient.getId().equals(document.getOwner().getId())) {
            throw new ValidationException("Cannot grant permissions to the document owner");
        }

        if (recipient.getStatus() != UserStatus.ACTIVE) {
            throw new ValidationException("Cannot grant permissions to an inactive or suspended user");
        }

        boolean alreadyActive = documentGrantRepository.hasActiveGrant(document.getId(), recipient.getId());
        if (alreadyActive) {
            throw new ValidationException("An active permission grant already exists for this user. Revoke it first to change permissions.");
        }

        DocumentGrant grant = new DocumentGrant(
                UUID.randomUUID(),
                document,
                recipient,
                request.getPermission(),
                currentUser
        );

        DocumentGrant saved = documentGrantRepository.save(grant);

        auditService.recordEvent(
                currentUser.getId(),
                AuditAction.GRANT_CREATED,
                document.getId(),
                recipient.getId(),
                AuditOutcome.SUCCESS,
                SecurityUtils.getCorrelationId(),
                String.format("Granted %s permission to %s on document '%s'",
                        request.getPermission(), recipient.getEmail(), document.getOriginalDisplayName())
        );

        return toDto(saved);
    }

    @Transactional
    public void revokeGrant(UUID documentId, UUID grantId, User currentUser) {
        Document document = documentRepository.findByIdAndIsDeletedFalse(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        authorizationService.checkCanManageGrants(currentUser, document);

        DocumentGrant grant = documentGrantRepository.findById(grantId)
                .orElseThrow(() -> new ResourceNotFoundException("Grant not found"));

        if (!grant.getDocument().getId().equals(documentId)) {
            throw new ValidationException("Grant does not belong to specified document");
        }

        if (!grant.isActive()) {
            throw new ValidationException("Grant is already revoked");
        }

        grant.setRevocationTimestamp(Instant.now());
        grant.setRevokingUser(currentUser);
        documentGrantRepository.save(grant);

        auditService.recordEvent(
                currentUser.getId(),
                AuditAction.GRANT_REVOKED,
                document.getId(),
                grant.getRecipientUser().getId(),
                AuditOutcome.SUCCESS,
                SecurityUtils.getCorrelationId(),
                String.format("Revoked %s permission from %s on document '%s'",
                        grant.getPermission(), grant.getRecipientUser().getEmail(), document.getOriginalDisplayName())
        );
    }

    @Transactional(readOnly = true)
    public List<GrantDto> listGrants(UUID documentId, User currentUser) {
        Document document = documentRepository.findByIdAndIsDeletedFalse(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        authorizationService.checkCanManageGrants(currentUser, document);

        List<DocumentGrant> grants = documentGrantRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);
        return grants.stream().map(this::toDto).toList();
    }

    public GrantDto toDto(DocumentGrant grant) {
        return new GrantDto(
                grant.getId(),
                grant.getDocument().getId(),
                grant.getRecipientUser().getId(),
                grant.getRecipientUser().getEmail(),
                grant.getPermission(),
                grant.getGrantingUser().getId(),
                grant.getGrantingUser().getEmail(),
                grant.getCreatedAt(),
                grant.getRevocationTimestamp(),
                grant.getRevokingUser() != null ? grant.getRevokingUser().getId() : null,
                grant.getRevokingUser() != null ? grant.getRevokingUser().getEmail() : null,
                grant.isActive()
        );
    }
}
