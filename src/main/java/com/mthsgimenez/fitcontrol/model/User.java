package com.mthsgimenez.fitcontrol.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "public", uniqueConstraints = {@UniqueConstraint(name = "users_email_tenant_id_key",
        columnNames = {
                "email",
                "tenant_id"})})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "uuid", columnDefinition = "uuid", updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "email", nullable = false, length = 70)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles;
    }

    @Override
    @NonNull
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    @NonNull
    public String getUsername() {
        return this.email;
    }
}