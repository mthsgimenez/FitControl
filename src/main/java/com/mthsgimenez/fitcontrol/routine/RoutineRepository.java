package com.mthsgimenez.fitcontrol.routine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Integer> {
    List<Routine> findByMemberId(Integer memberId);
    Optional<Routine> findByIdAndMemberId(Integer routineId, Integer memberId);
}
