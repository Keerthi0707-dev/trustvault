package com.trustvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "original_display_name", nullable = false)
    private String originalDisplayName;

    @Column(name = "safe_storage_object_key", nullable = false, unique = true)
    private String safeStorageObjectKey;

    @Column(name = "mime_type", nullable = false, length = 128)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 64)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sha256_checksum", nullable = false, length = 64)
    private String sha256Checksum;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Document() {
    }

    public Document(UUID id, User owner, String originalDisplayName, String safeStorageObjectKey,
                    String mimeType, Long fileSize, String category, String description, String sha256Checksum) {
        this.id = id != null ? id : UUID.randomUUID();
        this.owner = owner;
        this.originalDisplayName = originalDisplayName;
        this.safeStorageObjectKey = safeStorageObjectKey;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.category = category;
        this.description = description;
        this.sha256Checksum = sha256Checksum;
        this.isDeleted = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getOriginalDisplayName() {
        return originalDisplayName;
    }

    public void setOriginalDisplayName(String originalDisplayName) {
        this.originalDisplayName = originalDisplayName;
    }

    public String getSafeStorageObjectKey() {
        return safeStorageObjectKey;
    }

    public void setSafeStorageObjectKey(String safeStorageObjectKey) {
        this.safeStorageObjectKey = safeStorageObjectKey;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSha256Checksum() {
        return sha256Checksum;
    }

    public void setSha256Checksum(String sha256Checksum) {
        this.sha256Checksum = sha256Checksum;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
