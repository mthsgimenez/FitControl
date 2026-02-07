package com.mthsgimenez.fitcontrol.auth.dto;

import com.mthsgimenez.fitcontrol.auth.enums.RoleType;
import com.mthsgimenez.fitcontrol.tenant.model.Tenant;

import java.util.Set;

public record CreateUserDTO(
        String email,
        String password,
        Set<RoleType> roles,
        Tenant tenant
){}