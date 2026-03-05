package com.mthsgimenez.fitcontrol.routine;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record RoutineDTO(
        @NotBlank @Length(max = 50) String name,
        Integer memberId,
        @Valid @NotEmpty List<RoutineDayDTO> days
) {
    public static record RoutineDayDTO(
            @NotNull Integer dayOrder,
            @Valid @NotEmpty List<RoutineExerciseDTO> exercises
    ) {}

    public static record RoutineExerciseDTO(
            @NotNull Integer exerciseId,
            @NotNull Integer exerciseOrder,
            @NotNull Integer reps,
            @NotNull Integer series,
            String notes
    ) {}
}
