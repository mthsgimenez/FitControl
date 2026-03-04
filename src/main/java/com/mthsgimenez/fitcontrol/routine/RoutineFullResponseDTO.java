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
            Integer dayOrder,
            List<RoutineExerciseDTO> exercises
    ) {}

    public static record RoutineExerciseDTO(
            Integer exerciseId,
            String exerciseName,
            Integer exerciseOrder,
            Integer reps,
            Integer series,
            String notes
    ) {}
}