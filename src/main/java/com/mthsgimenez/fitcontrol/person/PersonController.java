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
    public ResponseEntity<Person> getPersonById(@PathVariable Integer id) {
        Person person = personService.findPersonById(id);
        // TODO: Retornar DTO
        return ResponseEntity.ok(person);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Person> createPerson(@Valid @RequestBody PersonDTO person) {
        Person newPerson = personService.createPerson(person);
        // TODO: Retornar DTO
        return ResponseEntity.ok(newPerson);
    }
}
