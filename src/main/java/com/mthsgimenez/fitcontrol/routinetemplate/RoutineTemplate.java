package com.mthsgimenez.fitcontrol.routinetemplate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "routine_templates",
        uniqueConstraints = @UniqueConstraint(
                name = "routine_templates_name_key",
                columnNames = "name"
        ))
public class RoutineTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @OneToMany(
            mappedBy = "routineTemplate",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("dayOrder ASC")
    private Set<RoutineTemplateDay> days = new LinkedHashSet<>();

    public void addDay(RoutineTemplateDay day) {
        days.add(day);
        day.setRoutineTemplate(this);
    }

    public void removeDay(RoutineTemplateDay day) {
        days.remove(day);
        day.setRoutineTemplate(null);
    }

    public void clearDays() {
        for (RoutineTemplateDay day : days) {
            day.setRoutineTemplate(null);
        }
        days.clear();
    }
}
