package com.mthsgimenez.fitcontrol.member;

import com.mthsgimenez.fitcontrol.person.PersonRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record MemberRegistrationRequestDTO(
        Integer personId,
        @Valid PersonRequestDTO person,
        @Email String email,
        @NotBlank @Length(max = 50) String goal,
        @NotNull TrainingLevel trainingLevel,
        String restrictions
) {
    public MemberRegistrationRequestDTO {
        if (personId == null) {
            if (person == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "person or personId must be provided"
                );
            }

            if (email == null || email.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "email must be provided if no personId"
                );
            }
        }

        if (personId != null && person != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "only person or personId must be provided"
            );
        }
    }
}