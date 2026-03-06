package com.mthsgimenez.fitcontrol.routinetemplate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    private List<RoutineTemplateDay> days = new ArrayList<>();

    public void addDay(RoutineTemplateDay day) {
        day.setDayOrder(days.size() + 1);
        days.add(day);
        day.setRoutineTemplate(this);
    }

    public void removeDay(RoutineTemplateDay day) {
        days.remove(day);
        day.setRoutineTemplate(null);
        for (int i = 0; i < days.size(); i++) {
            days.get(i).setDayOrder(i + 1);
        }
    }

    public void clearDays() {
        for (RoutineTemplateDay day : days) {
            day.setRoutineTemplate(null);
        }
        days.clear();
    }
}
