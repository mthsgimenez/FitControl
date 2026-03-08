package com.mthsgimenez.fitcontrol.membershipplan;

import java.math.BigDecimal;

public record MembershipPlanResponseDTO(
        Integer id,
        String name,
        BigDecimal price,
        Integer durationValue,
        Integer maxBeneficiaries,
        Boolean isActive
) {}