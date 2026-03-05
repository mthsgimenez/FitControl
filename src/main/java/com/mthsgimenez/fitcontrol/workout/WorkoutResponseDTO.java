package com.mthsgimenez.fitcontrol.workout;

import java.time.LocalDate;

public record WorkoutResponseDTO(
        Integer id,
        LocalDate workoutDate,
        Integer memberId
) {}