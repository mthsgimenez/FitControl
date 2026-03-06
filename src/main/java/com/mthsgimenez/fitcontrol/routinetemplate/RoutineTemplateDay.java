package com.mthsgimenez.fitcontrol.routinetemplate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "routine_template_days",
        uniqueConstraints = @UniqueConstraint(
                name = "routine_template_days_day_order_routine_template_id_key",
                columnNames = {"day_order", "routine_template_id"}
        ))
public class RoutineTemplateDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "routine_template_id", nullable = false)
    private RoutineTemplate routineTemplate;

    @Column(name = "day_order", nullable = false)
    private Integer dayOrder;

    @OneToMany(
            mappedBy = "routineTemplateDay",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("exerciseOrder ASC")
    private List<RoutineTemplateDayExercise> exercises = new ArrayList<>();

    public void addExercise(RoutineTemplateDayExercise exercise) {
        exercise.setExerciseOrder(exercises.size() + 1);
        exercises.add(exercise);
        exercise.setRoutineTemplateDay(this);
    }

    public void removeExercise(RoutineTemplateDayExercise exercise) {
        exercises.remove(exercise);
        exercise.setRoutineTemplateDay(null);
        for (int i = 0; i < exercises.size(); i++) {
            exercises.get(i).setExerciseOrder(i + 1);
        }
    }

    public void clearExercises() {
        for (RoutineTemplateDayExercise ex : exercises) {
            ex.setRoutineTemplateDay(null);
        }
        exercises.clear();
    }
}