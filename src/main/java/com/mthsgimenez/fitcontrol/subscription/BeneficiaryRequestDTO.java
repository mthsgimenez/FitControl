package com.mthsgimenez.fitcontrol.subscription;

import jakarta.validation.constraints.NotNull;

public record BeneficiaryRequestDTO(
        @NotNull Integer memberId
) {}