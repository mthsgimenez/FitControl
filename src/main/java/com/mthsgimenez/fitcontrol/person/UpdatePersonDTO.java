package com.mthsgimenez.fitcontrol.person;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UpdatePersonDTO(
        @NotBlank @Length(min = 2, max = 50) String name,
        @NotBlank @Length(min = 2, max = 100) String lastName
) {}
