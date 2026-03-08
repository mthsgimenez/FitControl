package com.mthsgimenez.fitcontrol.membership;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    boolean existsByGatewayAndGatewayPaymentId(String gateway, String gatewayPaymentId);
}
