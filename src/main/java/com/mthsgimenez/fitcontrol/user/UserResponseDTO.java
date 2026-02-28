package com.mthsgimenez.fitcontrol.user;

import java.util.Set;

public record UserResponseDTO(
        String userId,
        String email,
        Set<RoleType> roles
) {}
