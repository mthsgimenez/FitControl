package com.mthsgimenez.fitcontrol.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUuid(UUID uuid);
    Optional<User> findByEmail(String email);
    @Query("SELECT u FROM User u JOIN FETCH u.tenant WHERE u.email = :email")
    Optional<User> findByEmailWithTenant(String email);
}
