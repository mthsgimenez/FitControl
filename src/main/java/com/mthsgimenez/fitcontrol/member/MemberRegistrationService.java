package com.mthsgimenez.fitcontrol.member;

import com.mthsgimenez.fitcontrol.person.Person;
import com.mthsgimenez.fitcontrol.person.PersonDTO;
import com.mthsgimenez.fitcontrol.person.PersonRequestDTO;
import com.mthsgimenez.fitcontrol.person.PersonService;
import com.mthsgimenez.fitcontrol.user.*;
import com.mthsgimenez.fitcontrol.util.RandomStringUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class MemberRegistrationService {

    private final MemberService memberService;
    private final UserService userService;
    private final PersonService personService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RandomStringUtil randomStringUtil;

    public MemberRegistrationService(
            MemberService memberService,
            UserService userService,
            PersonService personService,
            ApplicationEventPublisher applicationEventPublisher,
            RandomStringUtil randomStringUtil
    ) {
        this.memberService = memberService;
        this.userService = userService;
        this.personService = personService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.randomStringUtil = randomStringUtil;
    }

    @Transactional
    public Member registerNewMember(MemberRegistrationRequestDTO data, User authUser) {
        if (data.person() == null) {
            Person existingPerson = personService.findPersonById(data.personId());
            return memberService.createMember(new MemberDTO(
                    existingPerson,
                    data.goal(),
                    data.trainingLevel(),
                    data.restrictions()
            ));
        }

        User newUser = createUserForMember(data, authUser);
        Person newPerson = createPersonForMember(data.person(), newUser, authUser);

        return memberService.createMember(new MemberDTO(
                newPerson,
                data.goal(),
                data.trainingLevel(),
                data.restrictions()
        ));
    }

    private User createUserForMember(MemberRegistrationRequestDTO data, User authUser) {
        CreateUserDTO newUserData = new CreateUserDTO(
                data.email(),
                randomStringUtil.getRandomString(),
                Set.of(RoleType.MEMBER),
                authUser.getTenant()
        );

        User newUser = userService.createUser(newUserData);
        applicationEventPublisher.publishEvent(new UserCreatedEvent(newUser));
        return newUser;
    }

    private Person createPersonForMember(PersonRequestDTO data, User user, User authUser) {
        PersonDTO newPersonData = new PersonDTO(
                data.name(),
                data.lastName(),
                data.cpf(),
                data.birthDate(),
                user
        );

        return personService.createPerson(newPersonData, authUser);
    }
}
