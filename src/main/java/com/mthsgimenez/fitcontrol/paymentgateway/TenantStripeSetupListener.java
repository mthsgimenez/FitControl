package com.mthsgimenez.fitcontrol.paymentgateway;

import com.mthsgimenez.fitcontrol.tenant.Tenant;
import com.mthsgimenez.fitcontrol.tenant.TenantCreatedEvent;
import com.mthsgimenez.fitcontrol.tenant.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class TenantStripeSetupListener {
    private final StripeService stripeService;
    private final TenantRepository tenantRepository;

    public TenantStripeSetupListener(
            StripeService stripeService,
            TenantRepository tenantRepository
    ) {
        this.stripeService = stripeService;
        this.tenantRepository = tenantRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTenantCreated(TenantCreatedEvent event) {
        try {
            Tenant tenant = tenantRepository.findByUuid(event.tenant().getUuid())
                    .orElseThrow(() -> new IllegalStateException(
                            "Tenant not found after commit: " + event.tenant().getUuid()));

            if (tenant.getGatewayAccountId() != null) return;

            String accountId = stripeService.createConnectedAccount(tenant, event.ownerEmail());
            tenant.setGatewayAccountId(accountId);
            tenantRepository.save(tenant);
            log.info("Stripe connected account saved for tenant {}", tenant.getUuid());
        } catch (Exception e) {
            log.error("Stripe account creation failed for tenant {}",
                    event.tenant().getUuid(), e);
        }
    }
}
