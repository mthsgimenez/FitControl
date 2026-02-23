package com.mthsgimenez.fitcontrol.employee;

import com.mthsgimenez.fitcontrol.person.PersonResponseDTO;

import java.time.LocalDate;

public record EmployeeResponseDTO(
        Integer id,
        LocalDate admissionDate,
        PersonResponseDTO person
) {}
