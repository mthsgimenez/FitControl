package com.mthsgimenez.fitcontrol.entity.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles", schema = "public", uniqueConstraints = {@UniqueConstraint(name = "roles_name_key",
        columnNames = {"name"})})
public class Role {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

}