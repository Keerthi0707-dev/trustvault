package com.trustvault.dto;

import com.trustvault.entity.GrantPermission;

import java.time.Instant;
import java.util.UUID;

public class GrantDto {
    private UUID id;
    private UUID documentId;
    private UUID recipientUserId;
    private String recipientEmail;
    private GrantPermission permission;
    private UUID grantingUserId;
    private String grantingUserEmail;
    private Instant createdAt;
    private Instant revocationTimestamp;
    private UUID revokingUserId;
    private String revokingUserEmail;
    private boolean active;

    public GrantDto() {
    }

    public GrantDto(UUID id, UUID documentId, UUID recipientUserId, String recipientEmail,
                    GrantPermission permission, UUID grantingUserId, String grantingUserEmail,
                    Instant createdAt, Instant revocationTimestamp, UUID revokingUserId,
                    String revokingUserEmail, boolean active) {
        this.id = id;
        this.documentId = documentId;
        this.recipientUserId = recipientUserId;
        this.recipientEmail = recipientEmail;
        this.permission = permission;
        this.grantingUserId = grantingUserId;
        this.grantingUserEmail = grantingUserEmail;
        this.createdAt = createdAt;
        this.revocationTimestamp = revocationTimestamp;
        this.revokingUserId = revokingUserId;
        this.revokingUserEmail = revokingUserEmail;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(UUID recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public GrantPermission getPermission() {
        return permission;
    }

    public void setPermission(GrantPermission permission) {
        this.permission = permission;
    }

    public UUID getGrantingUserId() {
        return grantingUserId;
    }

    public void setGrantingUserId(UUID grantingUserId) {
        this.grantingUserId = grantingUserId;
    }

    public String getGrantingUserEmail() {
        return grantingUserEmail;
    }

    public void setGrantingUserEmail(String grantingUserEmail) {
        this.grantingUserEmail = grantingUserEmail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getRevocationTimestamp() {
        return revocationTimestamp;
    }

    public void setRevocationTimestamp(Instant revocationTimestamp) {
        this.revocationTimestamp = revocationTimestamp;
    }

    public UUID getRevokingUserId() {
        return revokingUserId;
    }

    public void setRevokingUserId(UUID revokingUserId) {
        this.revokingUserId = revokingUserId;
    }

    public String getRevokingUserEmail() {
        return revokingUserEmail;
    }

    public void setRevokingUserEmail(String revokingUserEmail) {
        this.revokingUserEmail = revokingUserEmail;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
