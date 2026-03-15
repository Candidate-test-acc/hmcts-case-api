package uk.gov.hmcts.dts_dev_challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCaseRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description
) {}