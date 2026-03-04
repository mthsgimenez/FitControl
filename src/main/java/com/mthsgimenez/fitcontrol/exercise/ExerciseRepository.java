package com.mthsgimenez.fitcontrol.exercise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {

    List<Exercise> findByCategoryId(Integer categoryId);
}
