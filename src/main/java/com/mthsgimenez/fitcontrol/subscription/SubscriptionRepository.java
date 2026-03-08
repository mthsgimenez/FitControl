package com.mthsgimenez.fitcontrol.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    Optional<Subscription> findByGatewaySubscriptionId(String gatewaySubscriptionId);
}
