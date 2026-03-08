package com.mthsgimenez.fitcontrol.paymentgateway;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/stripe")
public class StripeOnboardingController {

    private final StripeOnboardingService stripeOnboardingService;

    public StripeOnboardingController(StripeOnboardingService stripeOnboardingService) {
        this.stripeOnboardingService = stripeOnboardingService;
    }

    @PostMapping("/onboarding-link")
    public ResponseEntity<OnboardingLinkResponseDTO> getOnboardingLink(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID tenantUUID = UUID.fromString(jwt.getClaim("tenant"));

        String url = stripeOnboardingService.generateOnboardingLink(tenantUUID);
        return ResponseEntity.ok(new OnboardingLinkResponseDTO(url));
    }
}
