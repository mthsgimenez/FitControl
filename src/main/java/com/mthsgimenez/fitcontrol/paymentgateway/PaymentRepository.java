package com.mthsgimenez.fitcontrol.paymentgateway;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    boolean existsByGatewayAndGatewayPaymentId(String gateway, String gatewayPaymentId);
}
