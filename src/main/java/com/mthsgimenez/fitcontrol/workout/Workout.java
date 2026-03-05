package com.mthsgimenez.fitcontrol.workout;

import com.mthsgimenez.fitcontrol.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "workouts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "workouts_member_id_workout_date_key",
                        columnNames = {"member_id", "workout_date"}
                )
        }
)
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(name = "workout_date", nullable = false)
    private LocalDate workoutDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(
            mappedBy = "workout",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("exerciseOrder ASC")
    private List<PerformedExercise> exercises = new ArrayList<>();

    public void addExercise(PerformedExercise exercise) {
        exercise.setExerciseOrder(exercises.size() + 1);
        exercises.add(exercise);
        exercise.setWorkout(this);
    }

    public void removeExercise(PerformedExercise exercise) {
        exercises.remove(exercise);
        exercise.setWorkout(null);
        for (int i = 0; i < exercises.size(); i++) {
            exercises.get(i).setExerciseOrder(i + 1);
        }
    }

    public void clearExercises() {
        for (PerformedExercise exercise : exercises) {
            exercise.setWorkout(null);
        }
        exercises.clear();
    }
}
