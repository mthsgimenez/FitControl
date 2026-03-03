package com.mthsgimenez.fitcontrol.member;

import com.mthsgimenez.fitcontrol.person.PersonResponseDTO;

public record MemberResponseDTO(
        Integer id,
        PersonResponseDTO person,
        String goal,
        String restrictions,
        String trainingLevel
){}
