package com.mthsgimenez.fitcontrol.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    boolean existsByGatewayAndGatewayPaymentId(String gateway, String gatewayPaymentId);
    List<Payment> findBySubscriptionId(Integer subscriptionId);
    List<Payment> findBySubscriptionPayerId(Integer memberId);
}
