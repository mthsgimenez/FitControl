package com.mthsgimenez.fitcontrol.paymentgateway;

import com.mthsgimenez.fitcontrol.infra.multitenancy.TenantContext;
import com.mthsgimenez.fitcontrol.membership.*;
import com.mthsgimenez.fitcontrol.tenant.Tenant;
import com.mthsgimenez.fitcontrol.tenant.TenantRepository;
import com.stripe.model.Account;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.SubscriptionItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.function.BiConsumer;

@Service
@Slf4j
public class StripeWebhookService {

    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;

    public StripeWebhookService(
            TenantRepository tenantRepository,
            SubscriptionRepository subscriptionRepository,
            PaymentRepository paymentRepository,
            StripeService stripeService
    ) {
        this.tenantRepository = tenantRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.stripeService = stripeService;
    }

    public void handleEvent(Event event) {
        log.info("Received Stripe event: {} [{}]", event.getType(), event.getId());
        switch (event.getType()) {
            case "account.updated"                  -> handleAccountUpdated(event);
            case "customer.subscription.created"    -> handleSubscriptionCreated(event);
            case "customer.subscription.updated"    -> handleSubscriptionUpdated(event);
            case "customer.subscription.deleted"    -> handleSubscriptionDeleted(event);
            case "invoice.paid"                     -> handleInvoicePaid(event);
            case "invoice.payment_failed"           -> handleInvoicePaymentFailed(event);
            default -> log.debug("Unhandled event type: {}", event.getType());
        }
    }

    @Transactional
    public void handleAccountUpdated(Event event) {
        Account account = (Account) event.getDataObjectDeserializer()
                .getObject().orElseThrow();

        boolean fullyOnboarded = Boolean.TRUE.equals(account.getDetailsSubmitted())
                && Boolean.TRUE.equals(account.getChargesEnabled());

        if (!fullyOnboarded) return;

        String tenantUuid = account.getMetadata().get("tenant_uuid");
        if (tenantUuid == null) {
            log.warn("account.updated missing tenant_uuid metadata: {}", account.getId());
            return;
        }

        Tenant tenant = tenantRepository.findByUuid(UUID.fromString(tenantUuid))
                .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantUuid));

        if (tenant.getGatewayProductId() != null) return;

        try {
            String productId = stripeService.createProduct(tenant);
            tenant.setGatewayProductId(productId);
            tenantRepository.save(tenant);
            log.info("Product created for tenant {}: {}", tenantUuid, productId);
        } catch (Exception e) {
            log.error("Failed to create product for tenant {}", tenantUuid, e);
        }
    }

    @Transactional
    public void handleSubscriptionCreated(Event event) {
        withSubscriptionContext(event, (stripeSubscription, schemaName) -> {
            subscriptionRepository.findByGatewaySubscriptionId(
                    stripeSubscription.getId()
            ).ifPresent(sub -> {
                sub.setGatewayStatus(stripeSubscription.getStatus());
                subscriptionRepository.save(sub);
            });
        });
    }

    @Transactional
    public void handleSubscriptionUpdated(Event event) {
        withSubscriptionContext(event, (stripeSubscription, schemaName) -> {
            Subscription sub = subscriptionRepository
                    .findByGatewaySubscriptionId(stripeSubscription.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Subscription not found: " + stripeSubscription.getId()));

            sub.setGatewayStatus(stripeSubscription.getStatus());
            sub.setStatus(mapStripeStatus(stripeSubscription.getStatus()));

            if (stripeSubscription.getItems() != null
                    && stripeSubscription.getItems().getData() != null
                    && !stripeSubscription.getItems().getData().isEmpty()) {

                SubscriptionItem item = stripeSubscription.getItems().getData().getFirst();

                if (item.getCurrentPeriodStart() != null) {
                    sub.setGatewayCurrentPeriodStart(
                            Instant.ofEpochSecond(item.getCurrentPeriodStart())
                                    .atZone(ZoneId.systemDefault()).toLocalDate());
                }
                if (item.getCurrentPeriodEnd() != null) {
                    sub.setGatewayCurrentPeriodEnd(
                            Instant.ofEpochSecond(item.getCurrentPeriodEnd())
                                    .atZone(ZoneId.systemDefault()).toLocalDate());
                }
            }

            subscriptionRepository.save(sub);
            log.info("Subscription {} updated → status: {}", sub.getId(), sub.getStatus());
        });
    }

    @Transactional
    public void handleSubscriptionDeleted(Event event) {
        withSubscriptionContext(event, (stripeSubscription, schemaName) -> {
            Subscription sub = subscriptionRepository
                    .findByGatewaySubscriptionId(stripeSubscription.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Subscription not found: " + stripeSubscription.getId()));

            sub.setGatewayStatus(stripeSubscription.getStatus());
            sub.setStatus(SubscriptionStatus.CANCELLED);
            sub.setCancelledAt(LocalDateTime.now());
            subscriptionRepository.save(sub);
            log.info("Subscription {} cancelled", sub.getId());
        });
    }

    @Transactional
    public void handleInvoicePaid(Event event) {
        withInvoiceContext(event, (invoice, schemaName) -> {
            if (paymentRepository.existsByGatewayAndGatewayPaymentId("STRIPE", invoice.getId()))
                return;

            String stripeSubscriptionId = resolveSubscriptionId(invoice);
            if (stripeSubscriptionId == null) {
                log.warn("invoice.paid has no subscription reference: {}", invoice.getId());
                return;
            }

            Subscription sub = subscriptionRepository
                    .findByGatewaySubscriptionId(stripeSubscriptionId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Subscription not found for invoice: " + invoice.getId()));

            Payment payment = new Payment();
            payment.setSubscription(sub);
            payment.setGateway("STRIPE");
            payment.setGatewayPaymentId(invoice.getId());
            payment.setGatewayInvoiceId(invoice.getId());
            payment.setAmount(BigDecimal.valueOf(invoice.getAmountPaid()).movePointLeft(2));
            payment.setCurrency(invoice.getCurrency().toUpperCase());
            payment.setStatus("PAID");
            payment.setPaidAt(
                    invoice.getStatusTransitions() != null
                            && invoice.getStatusTransitions().getPaidAt() != null
                            ? Instant.ofEpochSecond(invoice.getStatusTransitions().getPaidAt())
                            .atZone(ZoneId.systemDefault()).toLocalDateTime()
                            : LocalDateTime.now()
            );

            paymentRepository.save(payment);
            log.info("Payment recorded for subscription {}: {}", sub.getId(), invoice.getId());
        });
    }

    @Transactional
    public void handleInvoicePaymentFailed(Event event) {
        withInvoiceContext(event, (invoice, schemaName) -> {
            String stripeSubscriptionId = resolveSubscriptionId(invoice);
            if (stripeSubscriptionId == null) {
                log.warn("invoice.payment_failed has no subscription reference: {}", invoice.getId());
                return;
            }

            Subscription sub = subscriptionRepository
                    .findByGatewaySubscriptionId(stripeSubscriptionId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Subscription not found for invoice: " + invoice.getId()));

            sub.setStatus(SubscriptionStatus.PAYMENT_FAILED);
            sub.setGatewayStatus("past_due");
            subscriptionRepository.save(sub);
            log.warn("Payment failed for subscription {}", sub.getId());
        });
    }

    private String resolveSubscriptionId(Invoice invoice) {
        if (invoice.getParent() == null) return null;
        if (!"subscription_details".equals(invoice.getParent().getType())) return null;
        if (invoice.getParent().getSubscriptionDetails() == null) return null;
        return invoice.getParent().getSubscriptionDetails().getSubscription();
    }

    private void withSubscriptionContext(Event event,
                                         BiConsumer<com.stripe.model.Subscription, String> handler) {

        String connectedAccountId = event.getAccount();
        if (connectedAccountId == null) {
            log.warn("Event {} has no connected account ID", event.getId());
            return;
        }

        Tenant tenant = tenantRepository.findByGatewayAccountId(connectedAccountId)
                .orElseThrow(() -> new IllegalStateException(
                        "No tenant for account: " + connectedAccountId));

        TenantContext.setTenantSchema(tenant.getSchemaName());
        try {
            com.stripe.model.Subscription stripeSubscription =
                    (com.stripe.model.Subscription) event.getDataObjectDeserializer()
                            .getObject().orElseThrow();
            handler.accept(stripeSubscription, tenant.getSchemaName());
        } finally {
            TenantContext.clear();
        }
    }

    private void withInvoiceContext(Event event,
                                    BiConsumer<Invoice, String> handler) {

        String connectedAccountId = event.getAccount();
        if (connectedAccountId == null) {
            log.warn("Event {} has no connected account ID", event.getId());
            return;
        }

        Tenant tenant = tenantRepository.findByGatewayAccountId(connectedAccountId)
                .orElseThrow(() -> new IllegalStateException(
                        "No tenant for account: " + connectedAccountId));

        TenantContext.setTenantSchema(tenant.getSchemaName());
        try {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                    .getObject().orElseThrow();
            handler.accept(invoice, tenant.getSchemaName());
        } finally {
            TenantContext.clear();
        }
    }

    private SubscriptionStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active"            -> SubscriptionStatus.ACTIVE;
            case "past_due"          -> SubscriptionStatus.PAYMENT_FAILED;
            case "canceled"          -> SubscriptionStatus.CANCELLED;
            case "unpaid"            -> SubscriptionStatus.PAYMENT_FAILED;
            case "trialing"          -> SubscriptionStatus.ACTIVE;
            case "incomplete"        -> SubscriptionStatus.PENDING;
            case "incomplete_expired"-> SubscriptionStatus.CANCELLED;
            default -> {
                log.warn("Unknown Stripe status: {}", stripeStatus);
                yield SubscriptionStatus.PENDING;
            }
        };
    }
}