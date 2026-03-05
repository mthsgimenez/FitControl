package com.mthsgimenez.fitcontrol.workout;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "performed_sets")
public class PerformedSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_exercise_id", nullable = false)
    private PerformedExercise performedExercise;

    @Column(name = "set_order", nullable = false)
    private Integer setOrder;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Integer repetitions;

    @Column(length = Integer.MAX_VALUE)
    private String notes;
}