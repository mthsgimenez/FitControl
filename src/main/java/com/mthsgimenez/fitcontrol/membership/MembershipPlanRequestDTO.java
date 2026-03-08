package com.mthsgimenez.fitcontrol.membership;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MembershipPlanRequestDTO(
        @NotBlank @Size(max = 50) String name,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @NotNull @Min(1) Integer durationValue,
        @Min(1) Integer maxBeneficiaries
) {}
