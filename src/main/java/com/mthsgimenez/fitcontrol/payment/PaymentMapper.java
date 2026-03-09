package com.mthsgimenez.fitcontrol.payment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "gateway", source = "gateway")
    @Mapping(target = "gatewayPaymentId", source = "gatewayPaymentId")
    @Mapping(target = "gatewayInvoiceId", source = "gatewayInvoiceId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "paidAt", source = "paidAt")
    PaymentResponseDTO toDto(Payment payment);
}
