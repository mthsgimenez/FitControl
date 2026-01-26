package com.mthsgimenez.fitcontrol.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CNPJ;

import java.util.UUID;

public record TenantRegisterDTO(
        @NotNull UUID verificationId,
        @NotEmpty @Email String email,
        @NotEmpty @Length(min = 6, max = 6) String verificationCode,
        @NotEmpty @CNPJ String cnpj,
        @NotEmpty @Length(min = 8, max = 8) String postalCode,
        @NotEmpty @Length(min = 3, max = 100) String legalName,
        String tradeName,
        @NotEmpty @Length(min = 8, max = 40) String password
){}