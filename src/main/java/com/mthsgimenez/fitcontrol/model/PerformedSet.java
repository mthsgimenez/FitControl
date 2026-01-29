package com.mthsgimenez.fitcontrol.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "performed_sets", schema = "public", uniqueConstraints = {@UniqueConstraint(name = "performed_sets_set_order_performed_exercise_id_key",
        columnNames = {
                "set_order",
                "performed_exercise_id"})})
public class PerformedSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "performed_exercise_id", nullable = false)
    private PerformedExercise performedExercise;

    @Column(name = "set_order", nullable = false)
    private Integer setOrder;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "repetitions", nullable = false)
    private Integer repetitions;

    @Column(name = "notes", length = Integer.MAX_VALUE)
    private String notes;


}