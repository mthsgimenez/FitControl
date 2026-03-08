package com.mthsgimenez.fitcontrol.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, Integer> {
    Optional<Tenant> findByUuid(UUID uuid);
    Optional<Tenant> findBySchemaName(String schemaName);
    Optional<Tenant> findByGatewayAccountId(String gatewayAccountId);
}
