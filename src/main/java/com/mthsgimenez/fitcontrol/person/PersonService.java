package com.mthsgimenez.fitcontrol.person;

import com.mthsgimenez.fitcontrol.auth.refreshtokens.RefreshTokenService;
import com.mthsgimenez.fitcontrol.infra.exception.FKConstraintViolationException;
import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final RefreshTokenService refreshTokenService;

    public PersonService(
            PersonRepository personRepository,
            RefreshTokenService refreshTokenService
    ) {
        this.personRepository = personRepository;
        this.refreshTokenService = refreshTokenService;
    }

    public Person createPerson(PersonDTO data, User authUser) {
        if (data.user().getTenant().getUuid() != authUser.getTenant().getUuid()) {
            throw new NotFoundWithIdentifierException(User.class.getSimpleName(), data.user().getUuid());
        }

        Person person = new Person();

        person.setName(data.name());
        person.setLastName(data.lastName());
        person.setCpf(data.cpf());
        person.setBirthDate(data.birthDate());
        person.setUser(data.user());

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

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    @Transactional
    public void deleteById(Integer personId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Person.class.getSimpleName(), personId));
        Integer userId = person.getUser().getId();

        try {
            personRepository.delete(person);
            personRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new FKConstraintViolationException(Person.class.getSimpleName(), personId);
        }

        refreshTokenService.revokeRefreshTokensFromUser(userId);
    }
}
