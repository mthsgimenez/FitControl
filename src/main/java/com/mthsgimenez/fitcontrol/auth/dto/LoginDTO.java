package com.mthsgimenez.fitcontrol.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record LoginDTO(
        @NotEmpty @Email String email,
        @NotEmpty String password
) {}
