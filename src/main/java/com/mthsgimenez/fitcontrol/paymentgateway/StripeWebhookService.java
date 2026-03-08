package com.mthsgimenez.fitcontrol.paymentgateway;

import com.mthsgimenez.fitcontrol.infra.multitenancy.TenantContext;
import com.mthsgimenez.fitcontrol.subscription.Subscription;
import com.mthsgimenez.fitcontrol.subscription.SubscriptionRepository;
import com.mthsgimenez.fitcontrol.subscription.SubscriptionService;
import com.mthsgimenez.fitcontrol.subscription.SubscriptionStatus;
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

@Service
@Slf4j
public class StripeWebhookService {

    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final SubscriptionService subscriptionService;

    public StripeWebhookService(
            TenantRepository tenantRepository,
            SubscriptionRepository subscriptionRepository,
            PaymentRepository paymentRepository,
            StripeService stripeService,
            SubscriptionService subscriptionService
    ) {
        this.tenantRepository = tenantRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.stripeService = stripeService;
        this.subscriptionService = subscriptionService;
    }

    public void handleEvent(Event event) {
        log.info("Received Stripe event: {} [{}]", event.getType(), event.getId());
        switch (event.getType()) {
            case "account.updated"               -> handleAccountUpdated(event);
            case "customer.subscription.created" -> handleSubscriptionCreated(event);
            case "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            case "invoice.paid"                  -> handleInvoicePaid(event);
            case "invoice.payment_failed"        -> handleInvoicePaymentFailed(event);
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

    public void handleSubscriptionCreated(Event event) {
        Tenant tenant = resolveTenantFromAccount(event.getAccount());
        TenantContext.setTenantSchema(tenant.getSchemaName());
        try {
            com.stripe.model.Subscription stripeSubscription =
                    (com.stripe.model.Subscription) event.getDataObjectDeserializer()
                            .getObject().orElseThrow();
            handleSubscriptionCreatedTransactional(stripeSubscription, tenant);
        } finally {
            TenantContext.clear();
        }
    }

    public void handleSubscriptionUpdated(Event event) {
        Tenant tenant = resolveTenantFromAccount(event.getAccount());
        TenantContext.setTenantSchema(tenant.getSchemaName());
        try {
            com.stripe.model.Subscription stripeSubscription =
                    (com.stripe.model.Subscription) event.getDataObjectDeserializer()
                            .getObject().orElseThrow();
            handleSubscriptionUpdatedTransactional(stripeSubscription);
        } finally {
            TenantContext.clear();
        }
    }

    public void handleSubscriptionDeleted(Event event) {
        Tenant tenant = resolveTenantFromAccount(event.getAccount());
        TenantContext.setTenantSchema(tenant.getSchemaName());
        try {
            com.stripe.model.Subscription stripeSubscription =
                    (com.stripe.model.Subscription) event.getDataObjectDeserializer()
                            .getObject().orElseThrow();
            handleSubscriptionDeletedTransactional(stripeSubscription);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    protected void handleSubscriptionCreatedTransactional(
            com.stripe.model.Subscription stripeSubscription, Tenant tenant) {
        subscriptionService.createFromWebhook(stripeSubscription, tenant);
    }

    @Transactional
    protected void handleSubscriptionUpdatedTransactional(
            com.stripe.model.Subscription stripeSubscription) {
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
    }

    @Transactional
    protected void handleSubscriptionDeletedTransactional(
            com.stripe.model.Subscription stripeSubscription) {
        Subscription sub = subscriptionRepository
                .findByGatewaySubscriptionId(stripeSubscription.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Subscription not found: " + stripeSubscription.getId()));

        sub.setGatewayStatus(stripeSubscription.getStatus());
        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub.setCancelledAt(LocalDateTime.now());
        subscriptionRepository.save(sub);
        log.info("Subscription {} cancelled", sub.getId());
    }

    public void handleInvoicePaid(Event event) {
        Tenant tenant = resolveTenantFromAccount(event.getAccount());
        TenantContext.setTenantSchema(tenant.getSchemaName());
        try {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                    .getObject().orElseThrow();
            handleInvoicePaidTransactional(invoice);
        } finally {
            TenantContext.clear();
        }
    }

    public void handleInvoicePaymentFailed(Event event) {
        Tenant tenant = resolveTenantFromAccount(event.getAccount());
        TenantContext.setTenantSchema(tenant.getSchemaName());
        try {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                    .getObject().orElseThrow();
            handleInvoicePaymentFailedTransactional(invoice);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    protected void handleInvoicePaidTransactional(Invoice invoice) {
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
    }

    @Transactional
    protected void handleInvoicePaymentFailedTransactional(Invoice invoice) {
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
    }

    private Tenant resolveTenantFromAccount(String connectedAccountId) {
        if (connectedAccountId == null) {
            throw new IllegalStateException("Event has no connected account ID");
        }
        return tenantRepository.findByGatewayAccountId(connectedAccountId)
                .orElseThrow(() -> new IllegalStateException(
                        "No tenant for account: " + connectedAccountId));
    }

    private String resolveSubscriptionId(Invoice invoice) {
        if (invoice.getParent() == null) return null;
        if (!"subscription_details".equals(invoice.getParent().getType())) return null;
        if (invoice.getParent().getSubscriptionDetails() == null) return null;
        return invoice.getParent().getSubscriptionDetails().getSubscription();
    }

    private SubscriptionStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active"             -> SubscriptionStatus.ACTIVE;
            case "past_due"           -> SubscriptionStatus.PAYMENT_FAILED;
            case "canceled"           -> SubscriptionStatus.CANCELLED;
            case "unpaid"             -> SubscriptionStatus.PAYMENT_FAILED;
            case "trialing"           -> SubscriptionStatus.ACTIVE;
            case "incomplete"         -> SubscriptionStatus.PENDING;
            case "incomplete_expired" -> SubscriptionStatus.CANCELLED;
            default -> {
                log.warn("Unknown Stripe status: {}", stripeStatus);
                yield SubscriptionStatus.PENDING;
            }
        };
    }
}
