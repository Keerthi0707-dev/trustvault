package com.trustvault.dto;

import java.time.Instant;
import java.util.UUID;

public class DocumentDto {
    private UUID id;
    private String originalDisplayName;
    private String mimeType;
    private Long fileSize;
    private String category;
    private String description;
    private String sha256Checksum;
    private UUID ownerId;
    private String ownerEmail;
    private Instant createdAt;
    private Instant updatedAt;
    private String currentUserPermission; // "OWNER", "EDIT", "READ"

    public DocumentDto() {
    }

    public DocumentDto(UUID id, String originalDisplayName, String mimeType, Long fileSize,
                       String category, String description, String sha256Checksum,
                       UUID ownerId, String ownerEmail, Instant createdAt, Instant updatedAt,
                       String currentUserPermission) {
        this.id = id;
        this.originalDisplayName = originalDisplayName;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.category = category;
        this.description = description;
        this.sha256Checksum = sha256Checksum;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.currentUserPermission = currentUserPermission;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOriginalDisplayName() {
        return originalDisplayName;
    }

    public void setOriginalDisplayName(String originalDisplayName) {
        this.originalDisplayName = originalDisplayName;
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

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
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

    public String getCurrentUserPermission() {
        return currentUserPermission;
    }

    public void setCurrentUserPermission(String currentUserPermission) {
        this.currentUserPermission = currentUserPermission;
    }
}
