package com.mthsgimenez.fitcontrol.exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exercise_categories", uniqueConstraints = {@UniqueConstraint(name = "exercise_categories_name_key",
        columnNames = {"name"})})
public class ExerciseCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;
}