package com.mthsgimenez.fitcontrol.routine;

import java.util.UUID;

public record RoutineResponseDTO(
        Integer id,
        String name,
        Integer memberId,
        UUID createdByUserId
) {}
