package com.mthsgimenez.fitcontrol.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    Optional<Subscription> findByGatewaySubscriptionId(String gatewaySubscriptionId);
    boolean existsByGatewaySubscriptionId(String gatewaySubscriptionId);
    boolean existsByPayerIdAndStatusIn(Integer payerId, List<SubscriptionStatus> statuses);
    Optional<Subscription> findTopByPayerIdAndStatusOrderByStartDateDesc(
            Integer payerId, SubscriptionStatus status);
}
