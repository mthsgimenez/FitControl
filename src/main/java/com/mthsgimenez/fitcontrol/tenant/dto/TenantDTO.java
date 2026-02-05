package com.mthsgimenez.fitcontrol.tenant.dto;

import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CNPJ;

public record TenantDTO(
        @NotEmpty @CNPJ String cnpj,
        @NotEmpty @Length(min = 8, max = 8) String postalCode,
        @NotEmpty @Length(min = 3, max = 100) String legalName,
        String tradeName
){}