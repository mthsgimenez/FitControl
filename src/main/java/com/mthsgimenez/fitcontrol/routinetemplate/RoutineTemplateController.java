package com.mthsgimenez.fitcontrol.routinetemplate;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routine-template")
@PreAuthorize("hasRole('INSTRUCTOR')")
public class RoutineTemplateController {

    private final RoutineTemplateService routineTemplateService;
    private final RoutineTemplateMapper routineTemplateMapper;

    public RoutineTemplateController(
            RoutineTemplateService routineTemplateService,
            RoutineTemplateMapper routineTemplateMapper
    ) {
        this.routineTemplateService = routineTemplateService;
        this.routineTemplateMapper = routineTemplateMapper;
    }

    @PostMapping
    public ResponseEntity<RoutineTemplateFullResponseDTO> createRoutineTemplate(
            @Valid @RequestBody RoutineTemplateDTO data
    ) {
        RoutineTemplate newTemplate = routineTemplateService.createRoutineTemplate(data);

        RoutineTemplateFullResponseDTO response = routineTemplateMapper.toFullDto(newTemplate);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RoutineTemplateResponseDTO>> getAllRoutineTemplates() {
        List<RoutineTemplate> templates = routineTemplateService.getAllRoutineTemplates();

        List<RoutineTemplateResponseDTO> response = templates.stream().map(
                routineTemplateMapper::toSimpleDto).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutineTemplateFullResponseDTO> getRoutineTemplate(@PathVariable Integer id) {
        RoutineTemplate template = routineTemplateService.getRoutineTemplateById(id);

        RoutineTemplateFullResponseDTO response = routineTemplateMapper.toFullDto(template);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutineTemplateFullResponseDTO> updateRoutineTemplate(
            @PathVariable Integer id,
            @Valid @RequestBody RoutineTemplateDTO dto
    ) {
        RoutineTemplate updatedTemplate = routineTemplateService.updateRoutineTemplate(id, dto);

        RoutineTemplateFullResponseDTO response = routineTemplateMapper.toFullDto(updatedTemplate);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutineTemplate(@PathVariable Integer id) {
        routineTemplateService.deleteRoutineTemplate(id);

        return ResponseEntity.noContent().build();
    }
}
