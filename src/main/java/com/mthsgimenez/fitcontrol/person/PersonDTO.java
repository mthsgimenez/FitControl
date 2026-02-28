package com.mthsgimenez.fitcontrol.person;

import com.mthsgimenez.fitcontrol.user.User;

import java.time.LocalDate;

public record PersonDTO(
        String name,
        String lastName,
        String cpf,
        LocalDate birthDate,
        User user
){}