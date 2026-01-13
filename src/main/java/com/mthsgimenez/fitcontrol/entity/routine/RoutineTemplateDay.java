package com.mthsgimenez.fitcontrol.entity.routine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "routine_template_days", uniqueConstraints = {@UniqueConstraint(name = "routine_template_days_day_order_routine_template_id_key",
        columnNames = {
                "day_order",
                "routine_template_id"})})
public class RoutineTemplateDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "routine_template_id", nullable = false)
    private RoutineTemplate routineTemplate;

    @Column(name = "day_order", nullable = false)
    private Integer dayOrder;


}