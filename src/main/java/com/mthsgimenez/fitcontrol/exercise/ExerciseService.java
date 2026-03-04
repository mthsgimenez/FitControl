package com.mthsgimenez.fitcontrol.exercise;

import com.mthsgimenez.fitcontrol.infra.exception.FKConstraintViolationException;
import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseCategoryRepository exerciseCategoryRepository;

    public ExerciseService(
            ExerciseRepository exerciseRepository,
            ExerciseCategoryRepository exerciseCategoryRepository
    ) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseCategoryRepository = exerciseCategoryRepository;
    }

    public ExerciseCategory createExerciseCategory(String name) {
        ExerciseCategory newCategory = new ExerciseCategory();
        newCategory.setName(name);
        return exerciseCategoryRepository.save(newCategory);
    }

    public void deleteExerciseCategoryById(Integer categoryId){
        ExerciseCategory category = exerciseCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundWithIdentifierException("Category", categoryId));

        try {
            exerciseCategoryRepository.delete(category);
        } catch (DataIntegrityViolationException e) {
            throw new FKConstraintViolationException("Category", categoryId);
        }
    }

    public List<ExerciseCategory> getCategories() {
        return exerciseCategoryRepository.findAll();
    }

    public ExerciseCategory getCategoryById(Integer categoryId) {
        return exerciseCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundWithIdentifierException("Category", categoryId));
    }

    public List<Exercise> getExercisesByCategory(Integer categoryId) {
        if (!exerciseCategoryRepository.existsById(categoryId)) {
            throw new NotFoundWithIdentifierException("Category", categoryId);
        }
        return exerciseRepository.findByCategoryId(categoryId);
    }

    public Exercise getExerciseById(Integer exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Exercise.class.getSimpleName(), exerciseId));
    }

    public Exercise createExercise(Integer categoryId, ExerciseDTO data) {
        ExerciseCategory category = exerciseCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundWithIdentifierException("Category", categoryId));

        Exercise newExercise = new Exercise();
        newExercise.setName(data.name());
        newExercise.setCategory(category);

        return exerciseRepository.save(newExercise);
    }

    public Exercise updateExercise(Integer exerciseId, Integer categoryId, ExerciseDTO data) {
        Exercise existing = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Exercise.class.getSimpleName(), exerciseId));

        ExerciseCategory category = exerciseCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundWithIdentifierException("Category", categoryId));

        existing.setName(data.name());
        existing.setCategory(category);

        return exerciseRepository.save(existing);
    }

    public void deleteExerciseById(Integer exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new NotFoundWithIdentifierException(Exercise.class.getSimpleName(), exerciseId));

        exerciseRepository.delete(exercise);
    }
}
