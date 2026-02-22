package com.mthsgimenez.fitcontrol.person;

import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final UserRepository userRepository;

    public PersonService(PersonRepository personRepository,
                         UserRepository userRepository) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
    }

    public Person createPerson(PersonDTO data, User authUser) {
        User user = userRepository.findByUuid(data.userUUID())
                .orElseThrow(() -> new NotFoundWithIdentifierException(User.class.getSimpleName(), data.userUUID()));

        if (user.getTenant().getUuid() != authUser.getTenant().getUuid()) {
            throw new NotFoundWithIdentifierException(User.class.getSimpleName(), data.userUUID());
        }

        Person person = new Person();

        person.setName(data.name());
        person.setLastName(data.lastName());
        person.setCpf(data.cpf());
        person.setBirthDate(data.birthDate());
        person.setUser(user);

        return personRepository.save(person);
    }

    public Person findPersonById(Integer id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Person.class.getSimpleName(), id));
    }

    public Person findPersonByCPF(String cpf) {
        return personRepository.findByCpf(cpf)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Person.class.getSimpleName(), cpf));
    }
}
