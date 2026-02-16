package com.mthsgimenez.fitcontrol.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CNPJ;

import java.util.UUID;

public record TenantRegisterDTO(
        @NotBlank @Email String email,
        @NotBlank @Length(min = 6, max = 6) String verificationCode,
        @NotBlank @CNPJ String cnpj,
        @NotBlank @Length(min = 8, max = 8) String postalCode,
        @NotBlank @Length(min = 3, max = 100) String legalName,
        String tradeName,
        @NotBlank @Length(min = 8, max = 40) String password
){}