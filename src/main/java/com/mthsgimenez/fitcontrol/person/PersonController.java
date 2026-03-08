package com.mthsgimenez.fitcontrol.person;

import jakarta.validation.Valid;
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

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public ResponseEntity<List<PersonResponseDTO>> getAll() {
        return ResponseEntity.ok(personService.findAllAsDto());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(personService.findPersonByIdAsDto(id));
    }

    @Validated
    @GetMapping(params = {"cpf"})
    public ResponseEntity<PersonResponseDTO> getByCPF(@RequestParam @CPF String cpf) {
        return ResponseEntity.ok(personService.findPersonByCPF_AsDto(cpf));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        personService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> updateById(
            @PathVariable Integer id,
            @Valid @RequestBody UpdatePersonDTO data
    ) {
        return ResponseEntity.ok(personService.updateByIdAsDto(id, data));
    }
}
