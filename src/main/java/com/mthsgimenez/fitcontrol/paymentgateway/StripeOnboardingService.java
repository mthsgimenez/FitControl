package com.mthsgimenez.fitcontrol.paymentgateway;

import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import com.mthsgimenez.fitcontrol.tenant.Tenant;
import com.mthsgimenez.fitcontrol.tenant.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class StripeOnboardingService {

    private final StripeService stripeService;
    private final TenantRepository tenantRepository;

    @Value("${app.gateway.onboarding.refresh-url}")
    private String refreshUrl;

    @Value("${app.gateway.onboarding.return-url}")
    private String returnUrl;

    public StripeOnboardingService(
            StripeService stripeService,
            TenantRepository tenantRepository
    ) {
        this.stripeService = stripeService;
        this.tenantRepository = tenantRepository;
    }

    public String generateOnboardingLink(UUID tenantUUID) {
        Tenant tenant = tenantRepository.findByUuid(tenantUUID)
                .orElseThrow(() -> new NotFoundWithIdentifierException(
                        Tenant.class.getSimpleName(), tenantUUID
                ));

        if (tenant.getGatewayAccountId() == null) {
            throw new IllegalStateException("No connected account found for tenant " + tenantUUID);
        }

        return stripeService.createAccountOnboardingLink(
                tenant.getGatewayAccountId(),
                refreshUrl,
                returnUrl
        );
    }
}
