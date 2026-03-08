package com.mthsgimenez.fitcontrol.subscription;

import java.math.BigDecimal;

public record MembershipPlanSummaryDTO(
        Integer id,
        String name,
        BigDecimal price,
        Integer durationValue,
        Integer maxBeneficiaries
) {}
