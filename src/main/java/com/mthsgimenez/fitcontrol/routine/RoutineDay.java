package com.mthsgimenez.fitcontrol.routine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "routine_days", uniqueConstraints = {
        @UniqueConstraint(name = "routine_days_day_order_routine_id_key", columnNames = {"day_order", "routine_id"})
})
public class RoutineDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Column(name = "day_order", nullable = false)
    private Integer dayOrder;

    @OneToMany(
            mappedBy = "routineDay",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("exerciseOrder ASC")
    private List<RoutineDayExercise> exercises = new ArrayList<>();

    public void addExercise(RoutineDayExercise exercise) {
        exercise.setExerciseOrder(exercises.size() + 1);
        exercises.add(exercise);
        exercise.setRoutineDay(this);
    }

    public void removeExercise(RoutineDayExercise exercise) {
        exercises.remove(exercise);
        exercise.setRoutineDay(null);
        for (int i = 0; i < exercises.size(); i++) {
            exercises.get(i).setExerciseOrder(i + 1);
        }
    }

    public void clearExercises() {
        for (RoutineDayExercise exercise : exercises) {
            exercise.setRoutineDay(null);
        }
        exercises.clear();
    }
}
