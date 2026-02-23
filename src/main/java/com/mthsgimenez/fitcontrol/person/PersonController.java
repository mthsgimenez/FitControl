package com.mthsgimenez.fitcontrol.person;

import jakarta.validation.Valid;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
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

    @GetMapping()
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PersonResponseDTO> searchByCPF(@Valid @CPF @RequestParam String cpf) {
        Person person = personService.findPersonByCPF(cpf);

        PersonResponseDTO responseDTO = personMapper.toResponseDTO(person);

        return ResponseEntity.ok(responseDTO);
    }
}
