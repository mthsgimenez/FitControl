package com.mthsgimenez.fitcontrol.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDTO(
        Integer id,
        String gateway,
        String gatewayPaymentId,
        String gatewayInvoiceId,
        BigDecimal amount,
        String currency,
        String status,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {}
