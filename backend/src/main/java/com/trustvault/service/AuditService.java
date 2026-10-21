package com.trustvault.service;

import com.trustvault.dto.AuditIntegrityReportDto;
import com.trustvault.entity.AuditAction;
import com.trustvault.entity.AuditEvent;
import com.trustvault.entity.AuditOutcome;
import com.trustvault.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    public static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    private final AuditEventRepository auditEventRepository;
    private final ReentrantLock auditChainLock = new ReentrantLock(true);

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * Safely and serially records a security-relevant event into the tamper-evident hash chain.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public AuditEvent recordEvent(UUID actorId, AuditAction action, UUID documentId, UUID targetUserId,
                                  AuditOutcome outcome, String correlationId, String details) {
        auditChainLock.lock();
        try {
            UUID eventId = UUID.randomUUID();
            Instant now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
            String sanitizedCorrelationId = correlationId != null ? correlationId : UUID.randomUUID().toString();
            String sanitizedDetails = sanitizeDetails(details);

            // Fetch the latest event to obtain its hash for chaining
            String previousHash = auditEventRepository.findLatestEvent()
                    .map(AuditEvent::getCurrentEventHash)
                    .orElse(GENESIS_HASH);

            String canonicalRepresentation = computeCanonicalString(
                    eventId, now, actorId, action, documentId, targetUserId,
                    outcome, sanitizedCorrelationId, sanitizedDetails, previousHash
            );

            String currentHash = computeSha256(canonicalRepresentation);

            AuditEvent event = new AuditEvent(
                    eventId,
                    now,
                    actorId,
                    action,
                    documentId,
                    targetUserId,
                    outcome,
                    sanitizedCorrelationId,
                    sanitizedDetails,
                    previousHash,
                    currentHash
            );

            AuditEvent saved = auditEventRepository.saveAndFlush(event);
            log.info("SECURITY AUDIT [{}]: action={}, outcome={}, actor={}, doc={}, corrId={}",
                    saved.getId(), action, outcome, actorId, documentId, sanitizedCorrelationId);
            return saved;
        } catch (Exception ex) {
            log.error("CRITICAL: Failed to write security audit event! Action: {}, Outcome: {}", action, outcome, ex);
            throw new AuditLoggingException("Security audit trail write failed; operation blocked for security", ex);
        } finally {
            auditChainLock.unlock();
        }
    }

    /**
     * Verifies the cryptographic integrity of the entire audit chain.
     */
    @Transactional(readOnly = true)
    public AuditIntegrityReportDto verifyAuditIntegrity() {
        List<AuditEvent> events = auditEventRepository.findAllByOrderByTimestampAsc();
        if (events.isEmpty()) {
            return new AuditIntegrityReportDto(true, 0, null, "Audit log is empty (genesis state)", Instant.now());
        }

        String expectedPreviousHash = GENESIS_HASH;
        int checkedCount = 0;

        for (AuditEvent event : events) {
            checkedCount++;

            // 1. Verify previous hash link
            if (!expectedPreviousHash.equalsIgnoreCase(event.getPreviousEventHash())) {
                String error = String.format("Broken chain link at event %s: expected prevHash %s but found %s",
                        event.getId(), expectedPreviousHash, event.getPreviousEventHash());
                log.error("AUDIT INTEGRITY VIOLATION: {}", error);
                return new AuditIntegrityReportDto(false, checkedCount, event.getId().toString(), error, Instant.now());
            }

            // 2. Re-calculate canonical representation and current hash
            String canonical = computeCanonicalString(
                    event.getId(),
                    event.getTimestamp(),
                    event.getActorId(),
                    event.getAction(),
                    event.getDocumentId(),
                    event.getTargetUserId(),
                    event.getOutcome(),
                    event.getCorrelationId(),
                    event.getDetails(),
                    event.getPreviousEventHash()
            );

            String recalculatedHash = computeSha256(canonical);
            if (!recalculatedHash.equalsIgnoreCase(event.getCurrentEventHash())) {
                String error = String.format("Data tampering detected at event %s: recorded hash %s differs from recalculated hash %s",
                        event.getId(), event.getCurrentEventHash(), recalculatedHash);
                log.error("AUDIT INTEGRITY VIOLATION: {}", error);
                return new AuditIntegrityReportDto(false, checkedCount, event.getId().toString(), error, Instant.now());
            }

            expectedPreviousHash = event.getCurrentEventHash();
        }

        return new AuditIntegrityReportDto(true, checkedCount, null,
                "Cryptographic audit chain verified successfully (" + checkedCount + " events)", Instant.now());
    }

    public static String computeCanonicalString(UUID id, Instant timestamp, UUID actorId, AuditAction action,
                                                UUID documentId, UUID targetUserId, AuditOutcome outcome,
                                                String correlationId, String details, String previousHash) {
        String timestampStr = timestamp != null ? String.valueOf(timestamp.toEpochMilli()) : "";
        return String.join("|",
                id != null ? id.toString() : "",
                timestampStr,
                actorId != null ? actorId.toString() : "",
                action != null ? action.name() : "",
                documentId != null ? documentId.toString() : "",
                targetUserId != null ? targetUserId.toString() : "",
                outcome != null ? outcome.name() : "",
                correlationId != null ? correlationId : "",
                details != null ? details : "",
                previousHash != null ? previousHash : ""
        );
    }

    public static String computeSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    private String sanitizeDetails(String details) {
        if (details == null) {
            return "";
        }
        // Protect against accidental logging of passwords, tokens, or secret keys
        return details.replaceAll("(?i)(password|token|secret|key|authorization)\\s*[:=]\\s*[^,;\\s]+", "$1=[REDACTED]");
    }

    public static class AuditLoggingException extends RuntimeException {
        public AuditLoggingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
