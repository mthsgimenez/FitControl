package com.mthsgimenez.fitcontrol.tenant;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CNPJ;

public record TenantDTO(
        @NotBlank @CNPJ String cnpj,
        @NotBlank @Length(min = 8, max = 8) String postalCode,
        @NotBlank @Length(min = 3, max = 100) String legalName,
        String tradeName,
        String ownerEmail
){}