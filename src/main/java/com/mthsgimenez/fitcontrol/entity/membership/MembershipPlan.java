package com.mthsgimenez.fitcontrol.entity.membership;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "membership_plans", uniqueConstraints = {@UniqueConstraint(name = "membership_plans_name_key",
        columnNames = {"name"})})
public class MembershipPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_duration_unit", columnDefinition = "duration_unit not null")
    private DurationUnit membershipDurationUnit;

    @Column(name = "membership_duration_value", nullable = false)
    private Integer membershipDurationValue;


}