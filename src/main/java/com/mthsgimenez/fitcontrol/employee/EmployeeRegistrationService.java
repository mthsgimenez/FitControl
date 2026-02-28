package com.mthsgimenez.fitcontrol.employee;

import com.mthsgimenez.fitcontrol.person.Person;
import com.mthsgimenez.fitcontrol.person.PersonDTO;
import com.mthsgimenez.fitcontrol.person.PersonRequestDTO;
import com.mthsgimenez.fitcontrol.person.PersonService;
import com.mthsgimenez.fitcontrol.user.*;
import com.mthsgimenez.fitcontrol.util.RandomStringUtil;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Service
public class EmployeeRegistrationService {

    private final EmployeeService employeeService;
    private final UserService userService;
    private final PersonService personService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RandomStringUtil randomStringUtil;

    public EmployeeRegistrationService(
            EmployeeService employeeService,
            UserService userService,
            PersonService personService,
            ApplicationEventPublisher applicationEventPublisher,
            RandomStringUtil randomStringUtil
    ) {
        this.employeeService = employeeService;
        this.userService = userService;
        this.personService = personService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.randomStringUtil = randomStringUtil;
    }

    @Transactional
    public Employee registerNewEmployee(EmployeeRegistrationRequestDTO data, User authUser) {
        if (data.person() == null) {
            return createEmployeeFromExistingPerson(data.personId(), data.admissionDate());
        }

        User newUser = createUserForEmployee(data, authUser);
        Person newPerson = createPersonForEmployee(data.person(), newUser, authUser);
        return createEmployeeFromExistingPerson(newPerson.getId(), data.admissionDate());
    }

    private Employee createEmployeeFromExistingPerson(Integer personId, LocalDate admissionDate) {
        Person person = personService.findPersonById(personId);
        EmployeeDTO newEmployeeData = new EmployeeDTO(
                admissionDate,
                person
        );
        return employeeService.createEmployee(newEmployeeData);
    }

    private User createUserForEmployee(EmployeeRegistrationRequestDTO data, User authUser) {
        Set<RoleType> filteredRoles = EnumSet.copyOf(data.roles());
        filteredRoles.retainAll(RoleType.EMPLOYEE_ROLES);

        CreateUserDTO newUserData = new CreateUserDTO(
                data.email(),
                randomStringUtil.getRandomString(),
                filteredRoles,
                authUser.getTenant()
        );

        User newUser = userService.createUser(newUserData);
        applicationEventPublisher.publishEvent(new UserCreatedEvent(newUser));
        return newUser;
    }

    private Person createPersonForEmployee(PersonRequestDTO data, User user, User authUser) {
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
