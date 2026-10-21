package com.trustvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    private UUID id;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private AuditAction action;

    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "target_user_id")
    private UUID targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuditOutcome outcome;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "previous_event_hash", nullable = false, length = 64)
    private String previousEventHash;

    @Column(name = "current_event_hash", nullable = false, length = 64)
    private String currentEventHash;

    public AuditEvent() {
    }

    public AuditEvent(UUID id, Instant timestamp, UUID actorId, AuditAction action, UUID documentId,
                      UUID targetUserId, AuditOutcome outcome, String correlationId, String details,
                      String previousEventHash, String currentEventHash) {
        this.id = id != null ? id : UUID.randomUUID();
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.actorId = actorId;
        this.action = action;
        this.documentId = documentId;
        this.targetUserId = targetUserId;
        this.outcome = outcome;
        this.correlationId = correlationId;
        this.details = details;
        this.previousEventHash = previousEventHash;
        this.currentEventHash = currentEventHash;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(UUID targetUserId) {
        this.targetUserId = targetUserId;
    }

    public AuditOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(AuditOutcome outcome) {
        this.outcome = outcome;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPreviousEventHash() {
        return previousEventHash;
    }

    public void setPreviousEventHash(String previousEventHash) {
        this.previousEventHash = previousEventHash;
    }

    public String getCurrentEventHash() {
        return currentEventHash;
    }

    public void setCurrentEventHash(String currentEventHash) {
        this.currentEventHash = currentEventHash;
    }
}
