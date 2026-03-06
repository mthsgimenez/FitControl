package com.mthsgimenez.fitcontrol.routinetemplate;

import java.util.List;

public record RoutineTemplateFullResponseDTO(
        Integer id,
        String name,
        List<TemplateDayDTO> days
) {

    public static record TemplateDayDTO(
            List<TemplateExerciseDTO> exercises
    ) {}

    public static record TemplateExerciseDTO(
            Integer exerciseId,
            String exerciseName
    ) {}
}
