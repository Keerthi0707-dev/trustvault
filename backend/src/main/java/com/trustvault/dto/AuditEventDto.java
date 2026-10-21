package com.trustvault.dto;

import com.trustvault.entity.AuditAction;
import com.trustvault.entity.AuditOutcome;

import java.time.Instant;
import java.util.UUID;

public class AuditEventDto {
    private UUID id;
    private Instant timestamp;
    private UUID actorId;
    private String actorEmail;
    private AuditAction action;
    private UUID documentId;
    private UUID targetUserId;
    private String targetUserEmail;
    private AuditOutcome outcome;
    private String correlationId;
    private String details;
    private String previousEventHash;
    private String currentEventHash;

    public AuditEventDto() {
    }

    public AuditEventDto(UUID id, Instant timestamp, UUID actorId, String actorEmail,
                         AuditAction action, UUID documentId, UUID targetUserId, String targetUserEmail,
                         AuditOutcome outcome, String correlationId, String details,
                         String previousEventHash, String currentEventHash) {
        this.id = id;
        this.timestamp = timestamp;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.action = action;
        this.documentId = documentId;
        this.targetUserId = targetUserId;
        this.targetUserEmail = targetUserEmail;
        this.outcome = outcome;
        this.correlationId = correlationId;
        this.details = details;
        this.previousEventHash = previousEventHash;
        this.currentEventHash = currentEventHash;
    }

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

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
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

    public String getTargetUserEmail() {
        return targetUserEmail;
    }

    public void setTargetUserEmail(String targetUserEmail) {
        this.targetUserEmail = targetUserEmail;
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
