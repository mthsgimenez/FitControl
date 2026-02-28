package com.mthsgimenez.fitcontrol.person;

import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasRole('MANAGER')")
@RequestMapping("/person")
public class PersonController {

    private final PersonService personService;
    private final PersonMapper personMapper;

    public PersonController(PersonService personService,
                            PersonMapper personMapper
    ) {
        this.personService = personService;
        this.personMapper = personMapper;
    }

    @GetMapping
    public ResponseEntity<List<PersonResponseDTO>> getAll() {
        List<Person> people = personService.findAll();
        List<PersonResponseDTO> response = people.stream().map(personMapper::toResponseDTO).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> getById(@PathVariable Integer id) {
        Person person = personService.findPersonById(id);
        PersonResponseDTO response = personMapper.toResponseDTO(person);

        return ResponseEntity.ok(response);
    }

    @Validated
    @GetMapping(params = {"cpf"})
    public ResponseEntity<PersonResponseDTO> getByCPF(@RequestParam @CPF String cpf) {
        Person person = personService.findPersonByCPF(cpf);
        PersonResponseDTO responseDTO = personMapper.toResponseDTO(person);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        personService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
