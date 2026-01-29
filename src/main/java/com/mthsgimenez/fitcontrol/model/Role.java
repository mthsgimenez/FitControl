package com.mthsgimenez.fitcontrol.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@Entity
@Table(name = "roles", schema = "public", uniqueConstraints = {@UniqueConstraint(name = "roles_name_key",
        columnNames = {"name"})})
public class Role implements GrantedAuthority {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Override
    public String getAuthority() {
        return this.name;
    }
}