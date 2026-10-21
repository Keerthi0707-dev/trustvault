package com.trustvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_grants")
public class DocumentGrant {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GrantPermission permission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "granting_user_id", nullable = false)
    private User grantingUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "revocation_timestamp")
    private Instant revocationTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revoking_user_id")
    private User revokingUser;

    public DocumentGrant() {
    }

    public DocumentGrant(UUID id, Document document, User recipientUser, GrantPermission permission, User grantingUser) {
        this.id = id != null ? id : UUID.randomUUID();
        this.document = document;
        this.recipientUser = recipientUser;
        this.permission = permission;
        this.grantingUser = grantingUser;
        this.createdAt = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public boolean isActive() {
        return revocationTimestamp == null;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public User getRecipientUser() {
        return recipientUser;
    }

    public void setRecipientUser(User recipientUser) {
        this.recipientUser = recipientUser;
    }

    public GrantPermission getPermission() {
        return permission;
    }

    public void setPermission(GrantPermission permission) {
        this.permission = permission;
    }

    public User getGrantingUser() {
        return grantingUser;
    }

    public void setGrantingUser(User grantingUser) {
        this.grantingUser = grantingUser;
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

    public User getRevokingUser() {
        return revokingUser;
    }

    public void setRevokingUser(User revokingUser) {
        this.revokingUser = revokingUser;
    }
}
