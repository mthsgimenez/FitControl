package com.mthsgimenez.fitcontrol.membershipplan;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/membership-plan")
@PreAuthorize("hasRole('FINANCE')")
@Slf4j
public class MembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    public MembershipPlanController(
            MembershipPlanService membershipPlanService
    ) {
        this.membershipPlanService = membershipPlanService;
    }

    private UUID getTenantUuid(Authentication auth) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        return UUID.fromString(jwtAuth.getToken().getClaim("tenant"));
    }

    @PostMapping
    public ResponseEntity<MembershipPlanResponseDTO> create(
            @RequestBody @Valid MembershipPlanRequestDTO data,
            Authentication auth
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(membershipPlanService.createAsDto(data, getTenantUuid(auth)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MembershipPlanResponseDTO> update(
            @PathVariable Integer id,
            @RequestBody @Valid MembershipPlanRequestDTO data,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                membershipPlanService.updateAsDto(id, data, getTenantUuid(auth))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Integer id,
            Authentication auth
    ) {
        membershipPlanService.deactivate(id, getTenantUuid(auth));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FINANCE', 'INSTRUCTOR', 'MEMBER', 'MANAGER')")
    public ResponseEntity<List<MembershipPlanResponseDTO>> listActive() {
        return ResponseEntity.ok(
                membershipPlanService.findAllActiveAsDto()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FINANCE', 'INSTRUCTOR', 'MEMBER', 'MANAGER')")
    public ResponseEntity<MembershipPlanResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                membershipPlanService.findByIdAsDto(id)
        );
    }
}
