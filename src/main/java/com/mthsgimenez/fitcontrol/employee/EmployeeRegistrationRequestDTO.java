package com.mthsgimenez.fitcontrol.employee;

import com.mthsgimenez.fitcontrol.person.PersonRequestDTO;
import com.mthsgimenez.fitcontrol.user.RoleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Set;

public record EmployeeRegistrationRequestDTO(
        Integer personId,
        @Valid PersonRequestDTO person,
        @Email String email,
        @NotNull LocalDate admissionDate,
        @NotNull Set<RoleType> roles
        ) {
    public EmployeeRegistrationRequestDTO {
        if (personId == null) {
            if (person == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "person or personId must be provided");
            }

            if (email == null || email.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email must be provided if no personId");
            }
        }

        if (personId != null && person != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only person or personId must be provided");
        }
    }
}
