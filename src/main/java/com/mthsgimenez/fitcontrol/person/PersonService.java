package com.mthsgimenez.fitcontrol.person;

import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdException;
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

    public Person createPerson(PersonRequestDTO data) {
        // TODO: usuário tem que ser do mesmo tenant que o usuário que fez a request
        User user = userRepository.findById(data.userId())
                .orElseThrow(() -> new NotFoundWithIdException(User.class, data.userId()));

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
                .orElseThrow(() -> new NotFoundWithIdException(Person.class, id));
    }
}
