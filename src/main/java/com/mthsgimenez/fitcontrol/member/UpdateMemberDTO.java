package com.mthsgimenez.fitcontrol.member;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UpdateMemberDTO(
        @NotBlank @Length(max = 50) String goal,
        @NotBlank @Length(max = 50) String trainingLevel,
        String restrictions
) {}