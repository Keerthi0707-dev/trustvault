package com.trustvault.dto;

import com.trustvault.entity.GrantPermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateGrantRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid recipient email")
    private String recipientEmail;

    @NotNull(message = "Permission is required (READ or EDIT)")
    private GrantPermission permission;

    private Boolean aiAssisted;
    private String aiRationale;

    public CreateGrantRequest() {
    }

    public CreateGrantRequest(String recipientEmail, GrantPermission permission) {
        this.recipientEmail = recipientEmail;
        this.permission = permission;
    }

    public CreateGrantRequest(String recipientEmail, GrantPermission permission, Boolean aiAssisted, String aiRationale) {
        this.recipientEmail = recipientEmail;
        this.permission = permission;
        this.aiAssisted = aiAssisted;
        this.aiRationale = aiRationale;
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

    public Boolean getAiAssisted() {
        return aiAssisted;
    }

    public void setAiAssisted(Boolean aiAssisted) {
        this.aiAssisted = aiAssisted;
    }

    public String getAiRationale() {
        return aiRationale;
    }

    public void setAiRationale(String aiRationale) {
        this.aiRationale = aiRationale;
    }
}
