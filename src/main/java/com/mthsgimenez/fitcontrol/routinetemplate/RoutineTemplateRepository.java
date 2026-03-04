package com.mthsgimenez.fitcontrol.routinetemplate;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoutineTemplateRepository extends JpaRepository<RoutineTemplate, Integer> {
    @EntityGraph(attributePaths = {
            "days",
            "days.exercises"
    })
    Optional<RoutineTemplate> findCompleteTemplateById(Integer id);
}
