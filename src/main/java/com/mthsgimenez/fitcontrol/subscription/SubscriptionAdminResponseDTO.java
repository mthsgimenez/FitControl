package com.mthsgimenez.fitcontrol.subscription;

import com.mthsgimenez.fitcontrol.payment.PaymentResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record SubscriptionAdminResponseDTO(
        Integer id,
        SubscriptionStatus status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate gatewayCurrentPeriodStart,
        LocalDate gatewayCurrentPeriodEnd,
        MembershipPlanSummaryDTO membershipPlan,
        Set<SubscriptionMemberDTO> members,
        List<PaymentResponseDTO> payments
) {}
