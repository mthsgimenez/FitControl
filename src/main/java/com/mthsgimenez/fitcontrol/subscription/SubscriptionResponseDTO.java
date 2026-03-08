package com.mthsgimenez.fitcontrol.subscription;

import java.time.LocalDate;
import java.util.Set;

public record SubscriptionResponseDTO(
        Integer id,
        SubscriptionStatus status,
        String gatewayStatus,
        LocalDate startDate,
        LocalDate endDate,
        MembershipPlanSummaryDTO membershipPlan,
        Set<SubscriptionMemberDTO> members
) {}
