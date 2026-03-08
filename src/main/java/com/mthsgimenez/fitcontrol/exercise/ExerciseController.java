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

    public ExerciseController(
            ExerciseService exerciseService
    ) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEMBER', 'INSTRUCTOR')")
    public ResponseEntity<List<ExerciseCategory>> getCategories() {
        List<ExerciseCategory> categories = exerciseService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'INSTRUCTOR')")
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
    @PreAuthorize("hasAnyRole('MEMBER', 'INSTRUCTOR')")
    public ResponseEntity<List<ExerciseResponseDTO>> getExercisesByCategory(
            @PathVariable Integer categoryId
    ) {
        return ResponseEntity.ok(exerciseService.getExercisesByCategoryAsDto(categoryId));
    }

    @GetMapping("/{categoryId}/exercise/{exerciseId}")
    @PreAuthorize("hasAnyRole('MEMBER', 'INSTRUCTOR')")
    public ResponseEntity<ExerciseResponseDTO> getExerciseById(
            @PathVariable Integer categoryId,
            @PathVariable Integer exerciseId
    ) {
        exerciseService.getCategoryById(categoryId);

        return ResponseEntity.ok(exerciseService.getExerciseByIdAsDto(exerciseId));
    }

    @PostMapping("/{categoryId}/exercise")
    public ResponseEntity<ExerciseResponseDTO> createExercise(
            @PathVariable Integer categoryId,
            @Valid @RequestBody ExerciseDTO data
    ) {
        return ResponseEntity.status(201).body(
                exerciseService.createExerciseAsDto(categoryId, data)
        );
    }

    @PutMapping("/{categoryId}/exercise/{exerciseId}")
    public ResponseEntity<ExerciseResponseDTO> updateExercise(
            @PathVariable Integer categoryId,
            @PathVariable Integer exerciseId,
            @Valid @RequestBody ExerciseDTO data
    ) {
        return ResponseEntity.ok(
                exerciseService.updateExerciseAsDto(exerciseId, categoryId, data)
        );
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
