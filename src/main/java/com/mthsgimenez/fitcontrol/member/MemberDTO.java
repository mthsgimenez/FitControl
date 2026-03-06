package com.mthsgimenez.fitcontrol.member;

import com.mthsgimenez.fitcontrol.person.Person;

public record MemberDTO(
        Person person,
        String goal,
        TrainingLevel trainingLevel,
        String restrictions
) {}
