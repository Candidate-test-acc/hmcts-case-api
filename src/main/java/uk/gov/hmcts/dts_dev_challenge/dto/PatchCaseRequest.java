package uk.gov.hmcts.dts_dev_challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PatchCaseRequest(
        @Size(max = 200) String title,
        @Size(max = 2000) String description,
        CaseStatus status,
        CaseOutcome outcome,

        @NotBlank @Size(max = 300) String changeReason
) {}