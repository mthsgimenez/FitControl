package com.mthsgimenez.fitcontrol.person;

import com.mthsgimenez.fitcontrol.auth.refreshtokens.RefreshTokenService;
import com.mthsgimenez.fitcontrol.infra.exception.FKConstraintViolationException;
import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.user.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PersonService {

    private final PersonRepository personRepository;
    private final RefreshTokenService refreshTokenService;
    private final PersonMapper personMapper;

    public PersonService(
            PersonRepository personRepository,
            RefreshTokenService refreshTokenService,
            PersonMapper personMapper
    ) {
        this.personRepository = personRepository;
        this.refreshTokenService = refreshTokenService;
        this.personMapper = personMapper;
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

    @Transactional(readOnly = true)
    public Person findPersonById(Integer id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Person.class.getSimpleName(), id));
    }

    @Transactional(readOnly = true)
    public Person findPersonByCPF(String cpf) {
        return personRepository.findByCpf(cpf)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Person.class.getSimpleName(), cpf));
    }

    @Transactional(readOnly = true)
    public List<Person> findAll() {
        return personRepository.findAll();
    }

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

    public Person updateById(Integer personId, UpdatePersonDTO data) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Person.class.getSimpleName(), personId));

        person.setName(data.name());
        person.setLastName(data.lastName());

        return personRepository.save(person);
    }

    public PersonResponseDTO createPersonAsDto(PersonDTO data, User authUser) {
        return personMapper.toResponseDTO(createPerson(data, authUser));
    }

    @Transactional(readOnly = true)
    public PersonResponseDTO findPersonByIdAsDto(Integer id) {
        return personMapper.toResponseDTO(findPersonById(id));
    }

    @Transactional(readOnly = true)
    public List<PersonResponseDTO> findAllAsDto() {
        return personRepository.findAll().stream()
                .map(personMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PersonResponseDTO findPersonByCPF_AsDto(String cpf) {
        return personMapper.toResponseDTO(findPersonByCPF(cpf));
    }

    public PersonResponseDTO updateByIdAsDto(Integer personId, UpdatePersonDTO data) {
        return personMapper.toResponseDTO(updateById(personId, data));
    }
}
