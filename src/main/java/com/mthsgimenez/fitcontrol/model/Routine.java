package com.mthsgimenez.fitcontrol.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "routines", uniqueConstraints = {@UniqueConstraint(name = "routines_name_member_id_key",
        columnNames = {
                "name",
                "member_id"})})
public class Routine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;


}