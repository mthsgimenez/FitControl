package com.mthsgimenez.fitcontrol.user;

import com.mthsgimenez.fitcontrol.tenant.Tenant;

import java.util.Set;

public record CreateUserDTO(
        String email,
        String password,
        Set<RoleType> roles,
        Tenant tenant
){}