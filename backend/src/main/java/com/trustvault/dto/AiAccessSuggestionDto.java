package com.trustvault.dto;

import com.trustvault.entity.GrantPermission;

import java.util.UUID;

public class AiAccessSuggestionDto {
    private UUID documentId;
    private String documentName;
    private String category;
    private String suggestedRecipientEmail;
    private GrantPermission suggestedPermission;
    private String rationale;
    private String confidence;
    private String securityNotice;

    public AiAccessSuggestionDto() {}

    public AiAccessSuggestionDto(UUID documentId, String documentName, String category,
                                 String suggestedRecipientEmail, GrantPermission suggestedPermission,
                                 String rationale, String confidence, String securityNotice) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.category = category;
        this.suggestedRecipientEmail = suggestedRecipientEmail;
        this.suggestedPermission = suggestedPermission;
        this.rationale = rationale;
        this.confidence = confidence;
        this.securityNotice = securityNotice;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSuggestedRecipientEmail() {
        return suggestedRecipientEmail;
    }

    public void setSuggestedRecipientEmail(String suggestedRecipientEmail) {
        this.suggestedRecipientEmail = suggestedRecipientEmail;
    }

    public GrantPermission getSuggestedPermission() {
        return suggestedPermission;
    }

    public void setSuggestedPermission(GrantPermission suggestedPermission) {
        this.suggestedPermission = suggestedPermission;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getSecurityNotice() {
        return securityNotice;
    }

    public void setSecurityNotice(String securityNotice) {
        this.securityNotice = securityNotice;
    }
}
