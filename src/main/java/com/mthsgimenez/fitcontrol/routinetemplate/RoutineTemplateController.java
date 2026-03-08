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

    public RoutineTemplateController(RoutineTemplateService routineTemplateService) {
        this.routineTemplateService = routineTemplateService;
    }

    @PostMapping
    public ResponseEntity<RoutineTemplateFullResponseDTO> createRoutineTemplate(
            @Valid @RequestBody RoutineTemplateDTO data
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(routineTemplateService.createRoutineTemplateAsDto(data));
    }

    @GetMapping
    public ResponseEntity<List<RoutineTemplateResponseDTO>> getAllRoutineTemplates() {
        return ResponseEntity.ok(routineTemplateService.getAllRoutineTemplatesAsDto());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutineTemplateFullResponseDTO> getRoutineTemplate(
            @PathVariable Integer id) {
        return ResponseEntity.ok(routineTemplateService.getRoutineTemplateByIdAsDto(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutineTemplateFullResponseDTO> updateRoutineTemplate(
            @PathVariable Integer id,
            @Valid @RequestBody RoutineTemplateDTO dto
    ) {
        return ResponseEntity.ok(routineTemplateService.updateRoutineTemplateAsDto(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutineTemplate(@PathVariable Integer id) {
        routineTemplateService.deleteRoutineTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
