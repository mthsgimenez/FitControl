package com.mthsgimenez.fitcontrol.membership;

import java.math.BigDecimal;

public record MembershipPlanResponseDTO(
        Integer id,
        String name,
        BigDecimal price,
        Integer durationValue,
        Integer maxBeneficiaries,
        Boolean isActive
) {}