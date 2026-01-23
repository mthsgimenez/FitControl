package com.mthsgimenez.fitcontrol.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;

public record VerifyEmailDTO(
        @NotEmpty @Email String email,
        @NotEmpty @Length(min = 6, max = 6) String code
){}
