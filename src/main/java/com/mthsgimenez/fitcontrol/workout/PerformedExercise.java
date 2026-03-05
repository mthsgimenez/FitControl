package com.mthsgimenez.fitcontrol.workout;

import com.mthsgimenez.fitcontrol.exercise.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "performed_exercises",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "performed_exercises_exercise_id_workout_id_key",
                        columnNames = {"exercise_id", "workout_id"}
                )
        }
)
public class PerformedExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "exercise_order", nullable = false)
    private Integer exerciseOrder;

    @OneToMany(
            mappedBy = "performedExercise",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("setOrder ASC")
    private List<PerformedSet> sets = new ArrayList<>();

    public void addSet(PerformedSet set) {
        set.setSetOrder(sets.size() + 1);
        sets.add(set);
        set.setPerformedExercise(this);
    }

    public void removeSet(PerformedSet set) {
        sets.remove(set);
        set.setPerformedExercise(null);
        for (int i = 0; i < sets.size(); i++) {
            sets.get(i).setSetOrder(i + 1);
        }
    }

    public void clearSets() {
        for (PerformedSet set : sets) {
            set.setPerformedExercise(null);
        }
        sets.clear();
    }
}