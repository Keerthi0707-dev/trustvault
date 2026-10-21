package com.trustvault;

import com.trustvault.dto.AuditIntegrityReportDto;
import com.trustvault.entity.AuditAction;
import com.trustvault.entity.AuditEvent;
import com.trustvault.entity.AuditOutcome;
import com.trustvault.repository.AuditEventRepository;
import com.trustvault.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuditIntegrityTest {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    public void setup() {
        auditEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Cryptographic audit hash chain verifies successfully for valid events")
    public void testValidAuditChainVerification() {
        UUID actor1 = UUID.randomUUID();
        UUID doc1 = UUID.randomUUID();

        // 1. Record series of events
        AuditEvent event1 = auditService.recordEvent(actor1, AuditAction.LOGIN_SUCCESS, null, null,
                AuditOutcome.SUCCESS, "corr-1", "User logged in");
        AuditEvent event2 = auditService.recordEvent(actor1, AuditAction.DOCUMENT_UPLOAD, doc1, null,
                AuditOutcome.SUCCESS, "corr-2", "Uploaded document test.pdf");
        AuditEvent event3 = auditService.recordEvent(actor1, AuditAction.DOCUMENT_DOWNLOAD, doc1, null,
                AuditOutcome.SUCCESS, "corr-3", "Downloaded document");

        assertNotNull(event1.getCurrentEventHash());
        assertEquals(AuditService.GENESIS_HASH, event1.getPreviousEventHash());
        assertEquals(event1.getCurrentEventHash(), event2.getPreviousEventHash());
        assertEquals(event2.getCurrentEventHash(), event3.getPreviousEventHash());

        // 2. Verify integrity
        AuditIntegrityReportDto report = auditService.verifyAuditIntegrity();
        assertTrue(report.isValid(), "Audit chain should be valid");
        assertEquals(3, report.getTotalEventsChecked());
        assertNull(report.getFirstCorruptedEventId());
    }

    @Test
    @DisplayName("Tamper detection: modifying an event payload is immediately detected")
    public void testTamperDetectionOnPayloadModification() {
        UUID actor = UUID.randomUUID();
        AuditEvent event1 = auditService.recordEvent(actor, AuditAction.LOGIN_SUCCESS, null, null,
                AuditOutcome.SUCCESS, "corr-1", "User logged in");
        AuditEvent event2 = auditService.recordEvent(actor, AuditAction.DOCUMENT_DOWNLOAD, UUID.randomUUID(), null,
                AuditOutcome.SUCCESS, "corr-2", "Downloaded document confidential.pdf");

        // Maliciously tamper with event1 details directly in repository
        event1.setDetails("TAMPERED CONTENT: Attacker modified log");
        auditEventRepository.saveAndFlush(event1);

        // Verification must detect the tampering
        AuditIntegrityReportDto report = auditService.verifyAuditIntegrity();
        assertFalse(report.isValid(), "Tampered event must fail integrity verification");
        assertEquals(event1.getId().toString(), report.getFirstCorruptedEventId());
        assertTrue(report.getStatusMessage().contains("Data tampering detected"));
    }

    @Test
    @DisplayName("Tamper detection: breaking chain linkage is immediately detected")
    public void testTamperDetectionOnBrokenChainLink() {
        UUID actor = UUID.randomUUID();
        auditService.recordEvent(actor, AuditAction.LOGIN_SUCCESS, null, null,
                AuditOutcome.SUCCESS, "corr-1", "Event 1");
        AuditEvent event2 = auditService.recordEvent(actor, AuditAction.LOGOUT, null, null,
                AuditOutcome.SUCCESS, "corr-2", "Event 2");

        // Maliciously break the previous event hash link
        event2.setPreviousEventHash("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        auditEventRepository.saveAndFlush(event2);

        AuditIntegrityReportDto report = auditService.verifyAuditIntegrity();
        assertFalse(report.isValid());
        assertEquals(event2.getId().toString(), report.getFirstCorruptedEventId());
        assertTrue(report.getStatusMessage().contains("Broken chain link"));
    }
}
