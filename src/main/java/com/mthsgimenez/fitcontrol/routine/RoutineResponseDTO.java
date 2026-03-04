package com.mthsgimenez.fitcontrol.routine;

public record RoutineResponseDTO(
        Integer id,
        String name,
        Integer memberId,
        Integer createdByUserId
) {}
