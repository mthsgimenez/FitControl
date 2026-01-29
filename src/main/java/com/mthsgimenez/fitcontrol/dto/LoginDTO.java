package com.mthsgimenez.fitcontrol.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;

public record LoginDTO(
        @NotEmpty @Email String email,
        @NotEmpty String password
) {}
