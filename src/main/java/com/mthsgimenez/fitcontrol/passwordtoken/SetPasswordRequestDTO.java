package com.mthsgimenez.fitcontrol.passwordtoken;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record SetPasswordRequestDTO(
        @NotBlank String token,
        @NotBlank @Length(min = 8, max = 40) String password
) {}
