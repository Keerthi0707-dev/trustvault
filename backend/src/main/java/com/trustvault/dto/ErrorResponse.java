package com.trustvault.dto;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String correlationId;
    private String path;
    private List<String> validationErrors;

    public ErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ErrorResponse(int status, String error, String message, String correlationId, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.correlationId = correlationId;
        this.path = path;
    }

    public ErrorResponse(int status, String error, String message, String correlationId, String path, List<String> validationErrors) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.correlationId = correlationId;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
