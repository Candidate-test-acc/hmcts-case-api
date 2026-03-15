package uk.gov.hmcts.dts_dev_challenge.dto;

import java.time.Instant;

public record CaseResponse(
        long id,
        String reference,
        String title,
        String description,
        CaseStatus status,
        CaseOutcome outcome,

        Instant createdAt,
        Instant updatedAt,

        String lastChangeReason,
        Instant deletedAt
) {}