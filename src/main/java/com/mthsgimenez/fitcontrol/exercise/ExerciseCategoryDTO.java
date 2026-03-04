package com.mthsgimenez.fitcontrol.exercise;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record ExerciseCategoryDTO(
        @NotBlank @Length(max = 50) String name
) {}
