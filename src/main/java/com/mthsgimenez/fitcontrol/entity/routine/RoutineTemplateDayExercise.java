package com.mthsgimenez.fitcontrol.entity.routine;

import com.mthsgimenez.fitcontrol.entity.exercise.Exercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "routine_template_day_exercises", schema = "public", uniqueConstraints = {@UniqueConstraint(name = "routine_template_day_exercise_exercise_order_routine_templa_key",
        columnNames = {
                "exercise_order",
                "routine_template_day_id"})})
public class RoutineTemplateDayExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "routine_template_day_id", nullable = false)
    private RoutineTemplateDay routineTemplateDay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "exercise_order", nullable = false)
    private Integer exerciseOrder;


}