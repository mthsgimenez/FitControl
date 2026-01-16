package com.mthsgimenez.fitcontrol.repository;

import com.mthsgimenez.fitcontrol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUuid(UUID uuid);
}
