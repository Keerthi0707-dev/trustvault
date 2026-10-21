package com.trustvault;

import com.trustvault.entity.*;
import com.trustvault.exception.AccessDeniedException;
import com.trustvault.exception.AccountSuspendedException;
import com.trustvault.exception.ResourceNotFoundException;
import com.trustvault.repository.DocumentGrantRepository;
import com.trustvault.service.AuditService;
import com.trustvault.service.DocumentAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DocumentAuthorizationServiceTest {

    private DocumentGrantRepository documentGrantRepository;
    private AuditService auditService;
    private DocumentAuthorizationService authorizationService;

    private User owner;
    private User recipient;
    private User thirdParty;
    private User admin;
    private User suspendedUser;
    private Document document;

    @BeforeEach
    public void setup() {
        documentGrantRepository = Mockito.mock(DocumentGrantRepository.class);
        auditService = new AuditService(null) {
            @Override
            public AuditEvent recordEvent(UUID actorId, AuditAction action, UUID documentId, UUID targetUserId,
                                          AuditOutcome outcome, String correlationId, String details) {
                return null;
            }
        };
        authorizationService = new DocumentAuthorizationService(documentGrantRepository, auditService);

        owner = new User(UUID.randomUUID(), "owner@test.local", "hash", Role.OWNER, UserStatus.ACTIVE);
        recipient = new User(UUID.randomUUID(), "recipient@test.local", "hash", Role.VIEWER, UserStatus.ACTIVE);
        thirdParty = new User(UUID.randomUUID(), "thirdparty@test.local", "hash", Role.VIEWER, UserStatus.ACTIVE);
        admin = new User(UUID.randomUUID(), "admin@test.local", "hash", Role.ADMIN, UserStatus.ACTIVE);
        suspendedUser = new User(UUID.randomUUID(), "suspended@test.local", "hash", Role.VIEWER, UserStatus.SUSPENDED);

        document = new Document(UUID.randomUUID(), owner, "confidential.pdf", "safe_key_1",
                "application/pdf", 1024L, "Finance", "Q3 Report", "sha256");
    }

    @Test
    @DisplayName("Active check fails for suspended or disabled users")
    public void testSuspendedUserDenied() {
        assertThrows(AccountSuspendedException.class, () -> authorizationService.checkUserActive(suspendedUser));

        User disabledUser = new User(UUID.randomUUID(), "dis@test.local", "h", Role.VIEWER, UserStatus.DISABLED);
        assertThrows(AccountSuspendedException.class, () -> authorizationService.checkUserActive(disabledUser));
    }

    @Test
    @DisplayName("Viewer role cannot upload documents")
    public void testViewerCannotUpload() {
        assertThrows(AccessDeniedException.class, () -> authorizationService.checkCanUpload(recipient));
        // Owner and Admin can upload
        assertDoesNotThrow(() -> authorizationService.checkCanUpload(owner));
        assertDoesNotThrow(() -> authorizationService.checkCanUpload(admin));
    }

    @Test
    @DisplayName("Owner can read and download their own document")
    public void testOwnerCanReadAndDownload() {
        assertDoesNotThrow(() -> authorizationService.checkCanViewMetadata(owner, document));
        assertDoesNotThrow(() -> authorizationService.checkCanReadOrDownload(owner, document));
    }

    @Test
    @DisplayName("User without grant receives generic 404 on metadata and download (IDOR protection)")
    public void testUnauthorizedUserDeniedWithNotFound() {
        when(documentGrantRepository.hasActiveGrant(document.getId(), thirdParty.getId())).thenReturn(false);
        when(documentGrantRepository.findActiveGrant(document.getId(), thirdParty.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authorizationService.checkCanViewMetadata(thirdParty, document));
        assertThrows(ResourceNotFoundException.class, () -> authorizationService.checkCanReadOrDownload(thirdParty, document));
    }

    @Test
    @DisplayName("Active READ grant permits download but denies metadata edit")
    public void testReadGrantPermissions() {
        DocumentGrant readGrant = new DocumentGrant(UUID.randomUUID(), document, recipient, GrantPermission.READ, owner);
        when(documentGrantRepository.hasActiveGrant(document.getId(), recipient.getId())).thenReturn(true);
        when(documentGrantRepository.findActiveGrant(document.getId(), recipient.getId())).thenReturn(Optional.of(readGrant));

        // Read is allowed
        assertDoesNotThrow(() -> authorizationService.checkCanViewMetadata(recipient, document));
        assertDoesNotThrow(() -> authorizationService.checkCanReadOrDownload(recipient, document));

        // Edit is denied for READ grant
        assertThrows(AccessDeniedException.class, () -> authorizationService.checkCanEdit(recipient, document));
        // Delete is denied for recipient
        assertThrows(AccessDeniedException.class, () -> authorizationService.checkCanDelete(recipient, document));
        // Manage grants is denied for recipient
        assertThrows(AccessDeniedException.class, () -> authorizationService.checkCanManageGrants(recipient, document));
    }

    @Test
    @DisplayName("Active EDIT grant permits edit, but denies delete and grant management")
    public void testEditGrantPermissions() {
        DocumentGrant editGrant = new DocumentGrant(UUID.randomUUID(), document, recipient, GrantPermission.EDIT, owner);
        when(documentGrantRepository.findActiveGrant(document.getId(), recipient.getId())).thenReturn(Optional.of(editGrant));

        // Edit is allowed
        assertDoesNotThrow(() -> authorizationService.checkCanEdit(recipient, document));

        // EDIT grant recipient still CANNOT delete or grant permissions to others!
        assertThrows(AccessDeniedException.class, () -> authorizationService.checkCanDelete(recipient, document));
        assertThrows(AccessDeniedException.class, () -> authorizationService.checkCanManageGrants(recipient, document));
    }

    @Test
    @DisplayName("Immediate revocation: once revoked, next download request is immediately denied")
    public void testImmediateRevocationDenial() {
        DocumentGrant activeGrant = new DocumentGrant(UUID.randomUUID(), document, recipient, GrantPermission.READ, owner);
        when(documentGrantRepository.findActiveGrant(document.getId(), recipient.getId())).thenReturn(Optional.of(activeGrant));

        // Before revocation: download succeeds
        assertDoesNotThrow(() -> authorizationService.checkCanReadOrDownload(recipient, document));

        // Revoke grant
        activeGrant.setRevocationTimestamp(Instant.now());
        activeGrant.setRevokingUser(owner);
        // Subsequent repository lookup returns empty for active grants
        when(documentGrantRepository.findActiveGrant(document.getId(), recipient.getId())).thenReturn(Optional.empty());

        // Next request MUST immediately fail with 404 Not Found
        assertThrows(ResourceNotFoundException.class, () -> authorizationService.checkCanReadOrDownload(recipient, document));
    }
}
