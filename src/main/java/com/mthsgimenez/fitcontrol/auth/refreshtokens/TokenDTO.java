package com.mthsgimenez.fitcontrol.auth.refreshtokens;

public record TokenDTO(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {}
