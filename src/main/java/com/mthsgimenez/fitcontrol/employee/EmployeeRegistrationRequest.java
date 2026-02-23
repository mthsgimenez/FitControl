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

public record EmployeeRegistrationRequest(
        Integer personId,
        @Valid PersonRequestDTO person,
        @NotBlank @Email String email,
        @NotNull LocalDate admissionDate,
        @NotNull Set<RoleType> roles
        ) {
    public EmployeeRegistrationRequest {
        if ((personId == null && person == null) || (personId != null && person != null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Either personId or person must be provided"
            );
        }
    }
}
