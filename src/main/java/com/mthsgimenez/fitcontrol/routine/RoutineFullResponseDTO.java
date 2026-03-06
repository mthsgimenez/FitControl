package com.mthsgimenez.fitcontrol.routine;

import java.util.List;

public record RoutineFullResponseDTO(
        Integer id,
        String name,
        Integer memberId,
        Integer createdByUserId,
        List<RoutineDayDTO> days
) {

    public static record RoutineDayDTO(
            List<RoutineExerciseDTO> exercises
    ) {}

    public static record RoutineExerciseDTO(
            Integer exerciseId,
            String exerciseName,
            Integer reps,
            Integer series,
            String notes
    ) {}
}