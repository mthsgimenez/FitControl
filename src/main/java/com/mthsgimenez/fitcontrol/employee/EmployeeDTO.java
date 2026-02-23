package com.mthsgimenez.fitcontrol.employee;

import com.mthsgimenez.fitcontrol.person.Person;

import java.time.LocalDate;

public record EmployeeDTO(
        LocalDate admissionDate,
        Person person
){}