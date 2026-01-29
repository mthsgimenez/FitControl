package com.mthsgimenez.fitcontrol.repository;

import com.mthsgimenez.fitcontrol.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    public Role findByName(String name);
}
