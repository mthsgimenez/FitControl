package com.mthsgimenez.fitcontrol.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

public record EmailVerificationDTO(
        @NotEmpty UUID verificationId,
        @NotEmpty @Email String email,
        @NotEmpty @Length(min = 6, max = 6) String hashedCode
) {}
