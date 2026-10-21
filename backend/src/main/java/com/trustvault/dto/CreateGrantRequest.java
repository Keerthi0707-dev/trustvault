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

    public CreateGrantRequest() {
    }

    public CreateGrantRequest(String recipientEmail, GrantPermission permission) {
        this.recipientEmail = recipientEmail;
        this.permission = permission;
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
}
