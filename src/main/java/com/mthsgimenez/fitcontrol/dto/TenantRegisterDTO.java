package com.mthsgimenez.fitcontrol.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CNPJ;

public record TenantRegisterDTO(
        @NotEmpty @Email String email,
        // TODO: mover o token para o header
        @NotEmpty String registrationToken,
        @NotEmpty @CNPJ String cnpj,
        @NotEmpty @Length(min = 8, max = 8) String postalCode,
        String tradeName,
        @NotEmpty @Length(min = 3, max = 100) String legalName,
        @NotEmpty @Length(min = 8, max = 40) String password
){}