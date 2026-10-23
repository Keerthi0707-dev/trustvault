package com.trustvault;

import com.trustvault.dto.AiAccessSuggestionDto;
import com.trustvault.dto.CreateGrantRequest;
import com.trustvault.dto.DocumentDto;
import com.trustvault.dto.GrantDto;
import com.trustvault.entity.*;
import com.trustvault.exception.ResourceNotFoundException;
import com.trustvault.repository.AuditEventRepository;
import com.trustvault.repository.DocumentGrantRepository;
import com.trustvault.repository.DocumentRepository;
import com.trustvault.repository.UserRepository;
import com.trustvault.service.AiAccessAdvisorService;
import com.trustvault.service.DocumentGrantService;
import com.trustvault.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AiAccessAdvisorAndSearchTest {

    @Autowired
    private AiAccessAdvisorService aiAdvisorService;

    @Autowired
    private DocumentGrantService grantService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentGrantRepository grantRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private User owner;
    private User secondaryOwner;
    private User viewer;
    private Document financeDoc;
    private Document legalDoc;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(UUID.randomUUID(), "finance.owner@trustvault.local", "hash", Role.OWNER, UserStatus.ACTIVE));
        secondaryOwner = userRepository.save(new User(UUID.randomUUID(), "other.owner@trustvault.local", "hash", Role.OWNER, UserStatus.ACTIVE));
        viewer = userRepository.save(new User(UUID.randomUUID(), "auditor.viewer@trustvault.local", "hash", Role.VIEWER, UserStatus.ACTIVE));

        financeDoc = documentRepository.save(new Document(
                UUID.randomUUID(),
                owner,
                "Q3_Financial_Audit.pdf",
                "fin_obj_" + UUID.randomUUID(),
                "application/pdf",
                1024L,
                "Finance",
                "Quarterly financial audit",
                "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2"
        ));

        legalDoc = documentRepository.save(new Document(
                UUID.randomUUID(),
                owner,
                "Master_Services_Agreement.pdf",
                "legal_obj_" + UUID.randomUUID(),
                "application/pdf",
                2048L,
                "Legal",
                "Vendor master services contract",
                "b1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2"
        ));
    }

    @Test
    void testAiAdvisorGeneratesLeastPrivilegeSuggestionForOwner() {
        AiAccessSuggestionDto suggestion = aiAdvisorService.generateAccessSuggestion(financeDoc.getId(), owner);

        assertNotNull(suggestion);
        assertEquals(financeDoc.getId(), suggestion.getDocumentId());
        assertEquals("Finance", suggestion.getCategory());
        // Standard least privilege recommendation must be READ
        assertEquals(GrantPermission.READ, suggestion.getSuggestedPermission());
        assertNotNull(suggestion.getSuggestedRecipientEmail());
        assertTrue(suggestion.getRationale().contains("Finance"));
        assertTrue(suggestion.getRationale().contains("least of privilege") || suggestion.getRationale().contains("least-privilege") || suggestion.getRationale().contains("least privilege") || suggestion.getRationale().contains("READ"));
        assertTrue(suggestion.getSecurityNotice().contains("human document owner"));

        // Verify audit event was recorded for the AI suggestion
        List<AuditEvent> auditEvents = auditEventRepository.findByDocumentIdOrderByTimestampDesc(financeDoc.getId());
        boolean hasAiAudit = auditEvents.stream().anyMatch(e -> e.getAction() == AuditAction.AI_ACCESS_SUGGESTION);
        assertTrue(hasAiAudit, "AI Access suggestion must be recorded in the audit trail");
    }

    @Test
    void testNonOwnerCannotRequestAiAccessSuggestion() {
        // secondaryOwner did not own the document and has no grant
        assertThrows(com.trustvault.exception.AccessDeniedException.class, () -> {
            aiAdvisorService.generateAccessSuggestion(financeDoc.getId(), secondaryOwner);
        });
    }

    @Test
    void testAiAssistedGrantCreationRecordsRationaleInAudit() {
        CreateGrantRequest request = new CreateGrantRequest(
                viewer.getEmail(),
                GrantPermission.READ,
                true,
                "Audit compliance review under least privilege"
        );

        GrantDto created = grantService.createGrant(financeDoc.getId(), request, owner);
        assertNotNull(created);
        assertEquals(GrantPermission.READ, created.getPermission());
        assertEquals(viewer.getEmail(), created.getRecipientEmail());

        // Check audit ledger details
        List<AuditEvent> events = auditEventRepository.findByDocumentIdOrderByTimestampDesc(financeDoc.getId());
        AuditEvent grantEvent = events.stream()
                .filter(e -> e.getAction() == AuditAction.GRANT_CREATED)
                .findFirst()
                .orElseThrow();

        assertTrue(grantEvent.getDetails().contains("AI Access Advisor suggestion confirmed by owner"),
                "Audit log must indicate that owner confirmed the AI suggestion");
        assertTrue(grantEvent.getDetails().contains("Audit compliance review under least privilege"));
    }

    @Test
    void testSearchDocumentsByDateRange() {
        Instant now = Instant.now();
        Instant past = now.minus(5, ChronoUnit.DAYS);
        Instant future = now.plus(5, ChronoUnit.DAYS);

        // Search within range containing today
        List<DocumentDto> inRange = documentService.searchDocuments(null, null, past, future, owner);
        assertTrue(inRange.stream().anyMatch(d -> d.getId().equals(financeDoc.getId())));

        // Search with range strictly in the past (before creation)
        Instant distantPastStart = now.minus(20, ChronoUnit.DAYS);
        Instant distantPastEnd = now.minus(10, ChronoUnit.DAYS);
        List<DocumentDto> outOfRange = documentService.searchDocuments(null, null, distantPastStart, distantPastEnd, owner);
        assertFalse(outOfRange.stream().anyMatch(d -> d.getId().equals(financeDoc.getId())));
    }
}
