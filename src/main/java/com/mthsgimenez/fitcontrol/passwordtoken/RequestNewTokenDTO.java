package com.mthsgimenez.fitcontrol.passwordtoken;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestNewTokenDTO(
        @NotBlank @Email String email
) {}
