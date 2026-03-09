package com.mthsgimenez.fitcontrol.paymentgateway;

import com.mthsgimenez.fitcontrol.tenant.Tenant;
import com.mthsgimenez.fitcontrol.tenant.TenantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/stripe")
@PreAuthorize("hasRole('OWNER')")
public class StripeOnboardingController {

    private final StripeOnboardingService stripeOnboardingService;
    private final TenantRepository tenantRepository;

    public StripeOnboardingController(StripeOnboardingService stripeOnboardingService,
                                      TenantRepository tenantRepository
    ) {
        this.stripeOnboardingService = stripeOnboardingService;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping("/onboarding-link")
    public ResponseEntity<OnboardingLinkResponseDTO> getOnboardingLink(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID tenantUUID = UUID.fromString(jwt.getClaim("tenant"));

        String url = stripeOnboardingService.generateOnboardingLink(tenantUUID);
        return ResponseEntity.ok(new OnboardingLinkResponseDTO(url));
    }

    @GetMapping("/onboarding-status")
    public ResponseEntity<OnboardingStatusDTO> getOnboardingStatus(Authentication auth) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        UUID tenantUuid = UUID.fromString(jwtAuth.getToken().getClaim("tenant"));
        Tenant tenant = tenantRepository.findByUuid(tenantUuid).orElseThrow();
        return ResponseEntity.ok(new OnboardingStatusDTO(tenant.getGatewayProductId() != null));
    }

    public record OnboardingStatusDTO(boolean onboardingComplete) {}
}
