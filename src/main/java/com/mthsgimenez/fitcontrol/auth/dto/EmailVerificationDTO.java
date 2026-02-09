package com.mthsgimenez.fitcontrol.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

public record EmailVerificationDTO(
        @NotBlank UUID verificationId,
        @NotBlank @Email String email,
        @NotBlank @Length(min = 6, max = 6) String code
) {}
