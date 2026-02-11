package com.mthsgimenez.fitcontrol.auth.dto;

public record TokenDTO(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {}
