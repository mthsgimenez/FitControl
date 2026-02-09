package com.mthsgimenez.fitcontrol.person;

import com.mthsgimenez.fitcontrol.auth.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record PersonDTO(
        @NotBlank @Length(min = 2, max = 50) String name,
        @NotBlank @Length(min = 2, max = 100) String lastName,
        @NotBlank @CPF String cpf,
        @NotNull @Past LocalDate birthDate,
        @NotNull User user
){}