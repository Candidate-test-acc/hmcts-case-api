package uk.gov.hmcts.dts_dev_challenge.model;

import uk.gov.hmcts.dts_dev_challenge.dto.CaseOutcome;
import uk.gov.hmcts.dts_dev_challenge.dto.CaseStatus;

import java.time.Instant;

public class CourtCase {
    private final long id;
    private final String reference;

    private String title;
    private String description;

    private CaseStatus status;
    private CaseOutcome outcome;

    private final Instant createdAt;
    private Instant updatedAt;

    private String lastChangeReason;

    private Instant deletedAt;
    private String deleteReason;

    public CourtCase(long id, String reference, String title, String description, String createReason) {
        this.id = id;
        this.reference = reference;
        this.title = title;
        this.description = description;

        this.status = CaseStatus.OPEN;
        this.outcome = CaseOutcome.NONE;

        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;

        this.lastChangeReason = createReason;
    }

    public long getId() { return id; }
    public String getReference() { return reference; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public CaseStatus getStatus() { return status; }
    public CaseOutcome getOutcome() { return outcome; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getLastChangeReason() { return lastChangeReason; }
    public Instant getDeletedAt() { return deletedAt; }
    public boolean isDeleted() { return deletedAt != null; }

    public void applyUpdate(String title, String description, CaseStatus status, CaseOutcome outcome, String reason) {
        this.title = title;
        this.description = description;
        if (status != null) this.status = status;
        if (outcome != null) this.outcome = outcome;
        touch(reason);
    }

    public void applyPatch(String title, String description, CaseStatus status, CaseOutcome outcome, String reason) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (status != null) this.status = status;
        if (outcome != null) this.outcome = outcome;
        touch(reason);
    }

    public void close(String reason) {
        this.status = CaseStatus.CLOSED;
        touch(reason);
    }

    public void softDelete(String reason) {
        this.deletedAt = Instant.now();
        this.deleteReason = reason;
        touch("Soft-deleted: " + reason);
    }

    public void restore(String reason) {
        this.deletedAt = null;
        this.deleteReason = null;
        touch("Restored: " + reason);
    }

    private void touch(String reason) {
        this.updatedAt = Instant.now();
        this.lastChangeReason = reason;
    }
}