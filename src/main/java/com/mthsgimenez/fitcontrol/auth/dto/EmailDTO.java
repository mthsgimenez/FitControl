package com.mthsgimenez.fitcontrol.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailDTO(
        @NotBlank @Email String email
){}
