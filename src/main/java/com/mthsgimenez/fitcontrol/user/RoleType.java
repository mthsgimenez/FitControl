package com.mthsgimenez.fitcontrol.user;

import java.util.EnumSet;

public enum RoleType {
    OWNER,
    MANAGER,
    FINANCE,
    INSTRUCTOR,
    MEMBER;

    public static final EnumSet<RoleType> EMPLOYEE_ROLES = EnumSet.of(FINANCE, MANAGER, INSTRUCTOR);
}
