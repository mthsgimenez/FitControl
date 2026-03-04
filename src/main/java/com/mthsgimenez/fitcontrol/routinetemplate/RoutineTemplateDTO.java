package com.mthsgimenez.fitcontrol.routinetemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record RoutineTemplateDTO(
        @NotBlank @Length(max = 50) String name,
        @NotEmpty List<TemplateDayDTO> days
) {

    public static record TemplateDayDTO(
            @NotNull Integer dayOrder,
            @NotEmpty List<TemplateExerciseDTO> exercises
    ) {}

    public static record TemplateExerciseDTO(
            @NotNull Integer exerciseId,
            @NotNull Integer exerciseOrder
    ) {}
}
