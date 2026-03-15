package uk.gov.hmcts.dts_dev_challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeleteCaseRequest(
        @NotBlank @Size(max = 300) String reason
) {}