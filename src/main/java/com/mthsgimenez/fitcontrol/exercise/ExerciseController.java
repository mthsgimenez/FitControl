package com.mthsgimenez.fitcontrol.exercise;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@PreAuthorize("hasRole('INSTRUCTOR')")
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final ExerciseMapper exerciseMapper;

    public ExerciseController(
            ExerciseService exerciseService,
            ExerciseMapper exerciseMapper
    ) {
        this.exerciseService = exerciseService;
        this.exerciseMapper = exerciseMapper;
    }

    @GetMapping
    public ResponseEntity<List<ExerciseCategory>> getCategories() {
        List<ExerciseCategory> categories = exerciseService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseCategory> getCategoryById(@PathVariable Integer id) {
        ExerciseCategory category = exerciseService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<ExerciseCategory> createCategory(
            @Valid @RequestBody ExerciseCategoryDTO categoryDTO
    ) {
        ExerciseCategory created = exerciseService.createExerciseCategory(categoryDTO.name());
        return ResponseEntity.status(201).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        exerciseService.deleteExerciseCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryId}/exercise")
    public ResponseEntity<List<ExerciseResponseDTO>> getExercisesByCategory(
            @PathVariable Integer categoryId
    ) {
        List<ExerciseResponseDTO> exercises = exerciseService.getExercisesByCategory(categoryId)
                .stream()
                .map(exerciseMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/{categoryId}/exercise/{exerciseId}")
    public ResponseEntity<ExerciseResponseDTO> getExerciseById(
            @PathVariable Integer categoryId,
            @PathVariable Integer exerciseId
    ) {
        exerciseService.getCategoryById(categoryId);

        ExerciseResponseDTO response = exerciseMapper.toResponseDTO(
                exerciseService.getExerciseById(exerciseId)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{categoryId}/exercise")
    public ResponseEntity<ExerciseResponseDTO> createExercise(
            @PathVariable Integer categoryId,
            @Valid @RequestBody ExerciseDTO data
    ) {
        ExerciseResponseDTO response = exerciseMapper.toResponseDTO(
                exerciseService.createExercise(categoryId, data)
        );
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{categoryId}/exercise/{exerciseId}")
    public ResponseEntity<ExerciseResponseDTO> updateExercise(
            @PathVariable Integer categoryId,
            @PathVariable Integer exerciseId,
            @Valid @RequestBody ExerciseDTO data
    ) {
        ExerciseResponseDTO response = exerciseMapper.toResponseDTO(
                exerciseService.updateExercise(exerciseId, categoryId, data)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}/exercise/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable Integer categoryId,
            @PathVariable Integer exerciseId
    ) {
        exerciseService.getCategoryById(categoryId);

        exerciseService.deleteExerciseById(exerciseId);
        return ResponseEntity.noContent().build();
    }
}
