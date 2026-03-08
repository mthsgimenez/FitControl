package com.mthsgimenez.fitcontrol.membershipplan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Integer> {
    List<MembershipPlan> findAllByIsActiveTrue();
    boolean existsByName(String name);
    Optional<MembershipPlan> findByGatewayPriceId(String gatewayPriceId);
}