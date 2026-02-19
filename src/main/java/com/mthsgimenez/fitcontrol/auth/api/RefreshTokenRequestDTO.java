package com.mthsgimenez.fitcontrol.auth.api;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank String refreshToken
) {}
