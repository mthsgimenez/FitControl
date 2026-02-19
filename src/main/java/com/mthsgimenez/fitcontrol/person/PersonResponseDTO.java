package com.mthsgimenez.fitcontrol.person;

import java.time.LocalDate;

public record PersonResponseDTO(
        Integer id,
        String name,
        String lastName,
        String cpf,
        LocalDate birthDate,
        String userId
) {
    public PersonResponseDTO {
        cpf = cpf.replaceAll("\\D", "");
        cpf = String.format("XXX.%s.%s-XX", cpf.substring(3, 6), cpf.substring(6, 9));
    }
}
