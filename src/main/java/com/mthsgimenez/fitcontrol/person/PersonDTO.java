package com.mthsgimenez.fitcontrol.person;

import java.time.LocalDate;
import java.util.UUID;

public record PersonDTO(
        String name,
        String lastName,
        String cpf,
        LocalDate birthDate,
        UUID userUUID
){}