package com.mthsgimenez.fitcontrol.workout;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkoutRepository extends JpaRepository<Workout, Integer> {
    Page<Workout> findByMemberId(Integer memberId, Pageable pageable);
    Optional<Workout> findByIdAndMemberId(Integer workoutId, Integer memberId);
}