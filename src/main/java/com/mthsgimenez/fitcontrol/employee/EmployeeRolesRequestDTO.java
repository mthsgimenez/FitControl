package com.mthsgimenez.fitcontrol.employee;

import com.mthsgimenez.fitcontrol.user.RoleType;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record EmployeeRolesRequestDTO(
        @NotNull Set<RoleType> roles
) {}
