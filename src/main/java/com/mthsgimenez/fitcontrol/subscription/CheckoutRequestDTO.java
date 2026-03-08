package com.mthsgimenez.fitcontrol.subscription;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequestDTO(
        @NotNull Integer planId
) {}