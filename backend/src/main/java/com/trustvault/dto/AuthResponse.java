package com.trustvault.dto;

import com.trustvault.entity.Role;
import com.trustvault.entity.UserStatus;

import java.util.UUID;

public class AuthResponse {

    private UUID id;
    private String email;
    private Role role;
    private UserStatus status;

    public AuthResponse() {
    }

    public AuthResponse(UUID id, String email, Role role, UserStatus status) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
