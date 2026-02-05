package com.mthsgimenez.fitcontrol.auth.dto;

import com.mthsgimenez.fitcontrol.auth.model.Role;
import com.mthsgimenez.fitcontrol.tenant.model.Tenant;

import java.util.Set;

public record UserRegisterDTO(
        String email,
        String password,
        Set<Role> roles,
        Tenant tenant
){}