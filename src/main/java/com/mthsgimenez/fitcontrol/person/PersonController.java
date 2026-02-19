package com.mthsgimenez.fitcontrol.person;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> getPersonById(@PathVariable Integer id) {
        Person person = personService.findPersonById(id);

        PersonResponseDTO responseDTO = new PersonResponseDTO(
                person.getId(),
                person.getName(),
                person.getLastName(),
                person.getCpf(),
                person.getBirthDate(),
                person.getUser().getUuid().toString()
        );

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PersonResponseDTO> createPerson(@Valid @RequestBody PersonRequestDTO person) {
        Person newPerson = personService.createPerson(person);

        PersonResponseDTO responseDTO = new PersonResponseDTO(
                newPerson.getId(),
                newPerson.getName(),
                newPerson.getLastName(),
                newPerson.getCpf(),
                newPerson.getBirthDate(),
                newPerson.getUser().getUuid().toString()
        );

        return ResponseEntity.ok(responseDTO);
    }
}
