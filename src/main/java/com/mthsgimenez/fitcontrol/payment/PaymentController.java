package com.mthsgimenez.fitcontrol.payment;

import com.mthsgimenez.fitcontrol.subscription.SubscriptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payments")
@PreAuthorize("hasRole('FINANCE')")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final SubscriptionRepository subscriptionRepository;

    public PaymentController(
            PaymentRepository paymentRepository,
            PaymentMapper paymentMapper,
            SubscriptionRepository subscriptionRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.subscriptionRepository = subscriptionRepository;
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> listAll() {
        return ResponseEntity.ok(
                paymentRepository.findAll().stream()
                        .map(paymentMapper::toDto)
                        .toList()
        );
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<PaymentResponseDTO>> listBySubscription(
            @PathVariable Integer subscriptionId
    ) {
        return ResponseEntity.ok(
                paymentRepository.findBySubscriptionId(subscriptionId).stream()
                        .map(paymentMapper::toDto)
                        .toList()
        );
    }
}
