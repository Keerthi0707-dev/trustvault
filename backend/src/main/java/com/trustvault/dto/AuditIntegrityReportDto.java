package com.trustvault.dto;

import java.time.Instant;

public class AuditIntegrityReportDto {

    private boolean valid;
    private int totalEventsChecked;
    private String firstCorruptedEventId;
    private String statusMessage;
    private Instant verifiedAt;

    public AuditIntegrityReportDto() {
    }

    public AuditIntegrityReportDto(boolean valid, int totalEventsChecked, String firstCorruptedEventId,
                                   String statusMessage, Instant verifiedAt) {
        this.valid = valid;
        this.totalEventsChecked = totalEventsChecked;
        this.firstCorruptedEventId = firstCorruptedEventId;
        this.statusMessage = statusMessage;
        this.verifiedAt = verifiedAt;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getTotalEventsChecked() {
        return totalEventsChecked;
    }

    public void setTotalEventsChecked(int totalEventsChecked) {
        this.totalEventsChecked = totalEventsChecked;
    }

    public String getFirstCorruptedEventId() {
        return firstCorruptedEventId;
    }

    public void setFirstCorruptedEventId(String firstCorruptedEventId) {
        this.firstCorruptedEventId = firstCorruptedEventId;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
