package com.mthsgimenez.fitcontrol.workout;

import jakarta.validation.constraints.NotNull;

public record SetDTO(
        @NotNull Double weight,
        @NotNull Integer repetitions,
        String notes
) {}