package com.trustvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DocumentUpdateRequest {

    @NotBlank(message = "Document name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String originalDisplayName;

    @NotBlank(message = "Category is required")
    @Size(max = 64, message = "Category cannot exceed 64 characters")
    private String category;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    public DocumentUpdateRequest() {
    }

    public DocumentUpdateRequest(String originalDisplayName, String category, String description) {
        this.originalDisplayName = originalDisplayName;
        this.category = category;
        this.description = description;
    }

    public String getOriginalDisplayName() {
        return originalDisplayName;
    }

    public void setOriginalDisplayName(String originalDisplayName) {
        this.originalDisplayName = originalDisplayName;
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
}
