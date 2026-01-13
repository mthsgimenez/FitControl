package com.mthsgimenez.fitcontrol.entity.exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

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

    @OneToMany(mappedBy = "category")
    private Set<Exercise> exercises = new LinkedHashSet<>();


}