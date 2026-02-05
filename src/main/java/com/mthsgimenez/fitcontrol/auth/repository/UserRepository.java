package com.mthsgimenez.fitcontrol.auth.repository;

import com.mthsgimenez.fitcontrol.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUuid(UUID uuid);

    Optional<User> findByEmail(String email);
}
