package com.mthsgimenez.fitcontrol.auth.repository;

import com.mthsgimenez.fitcontrol.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    public Optional<Role> findByName(String name);
}
