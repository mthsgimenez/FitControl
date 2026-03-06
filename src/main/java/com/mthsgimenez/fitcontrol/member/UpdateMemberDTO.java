package com.mthsgimenez.fitcontrol.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UpdateMemberDTO(
        @NotBlank @Length(max = 50) String goal,
        @NotNull TrainingLevel trainingLevel,
        String restrictions
) {}