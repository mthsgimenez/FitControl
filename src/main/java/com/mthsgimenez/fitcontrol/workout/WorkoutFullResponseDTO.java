package com.mthsgimenez.fitcontrol.workout;

import java.time.LocalDate;
import java.util.List;

public record WorkoutFullResponseDTO(
        Integer id,
        LocalDate workoutDate,
        Integer memberId,
        List<WorkoutExerciseDTO> exercises
) {

    public static record WorkoutExerciseDTO(
            Integer exerciseId,
            String exerciseName,
            List<WorkoutSetDTO> sets
    ) {}

    public static record WorkoutSetDTO(
            Double weight,
            Integer repetitions,
            String notes
    ) {}
}