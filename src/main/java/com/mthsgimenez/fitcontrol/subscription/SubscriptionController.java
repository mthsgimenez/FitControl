package com.mthsgimenez.fitcontrol.subscription;

import com.mthsgimenez.fitcontrol.user.User;
import com.mthsgimenez.fitcontrol.user.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/subscriptions")
@PreAuthorize("hasRole('MEMBER')")
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    public SubscriptionController(
            SubscriptionService subscriptionService,
            UserRepository userRepository
    ) {
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
    }

    private User getUserFromAuth(Authentication auth) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        UUID userUuid = UUID.fromString(jwtAuth.getToken().getSubject());
        return userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UUID getTenantUuid(Authentication auth) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        return UUID.fromString(jwtAuth.getToken().getClaim("tenant"));
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDTO> initiateCheckout(
            @RequestBody @Valid CheckoutRequestDTO data,
            Authentication auth
    ) {
        String url = subscriptionService.initiateCheckout(
                data.planId(), getUserFromAuth(auth), getTenantUuid(auth));
        return ResponseEntity.ok(new CheckoutResponseDTO(url));
    }

    @GetMapping("/my")
    public ResponseEntity<SubscriptionResponseDTO> getMySubscription(Authentication auth) {
        return ResponseEntity.ok(
                subscriptionService.findActiveByUserAsDto(getUserFromAuth(auth))
        );
    }

    @PostMapping("/{subscriptionId}/members")
    public ResponseEntity<SubscriptionResponseDTO> addBeneficiary(
            @PathVariable Integer subscriptionId,
            @RequestBody @Valid BeneficiaryRequestDTO data,
            Authentication auth
    ) {
        return ResponseEntity.ok(subscriptionService.addBeneficiaryAsDto(
                subscriptionId, data.memberId(), getUserFromAuth(auth)
        ));
    }

    @DeleteMapping("/{subscriptionId}/members/{memberId}")
    public ResponseEntity<Void> removeBeneficiary(
            @PathVariable Integer subscriptionId,
            @PathVariable Integer memberId,
            Authentication auth
    ) {
        subscriptionService.removeBeneficiary(subscriptionId, memberId, getUserFromAuth(auth));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<List<SubscriptionAdminResponseDTO>> listAll() {
        return ResponseEntity.ok(subscriptionService.findAllAsDto());
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<List<SubscriptionAdminResponseDTO>> listByMember(
            @PathVariable Integer memberId
    ) {
        return ResponseEntity.ok(subscriptionService.findByMemberAsDto(memberId));
    }
}
