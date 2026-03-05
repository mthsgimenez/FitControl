package com.mthsgimenez.fitcontrol.workout;

import jakarta.validation.constraints.NotNull;

public record PerformedExerciseDTO(
        @NotNull Integer exerciseId
) {}