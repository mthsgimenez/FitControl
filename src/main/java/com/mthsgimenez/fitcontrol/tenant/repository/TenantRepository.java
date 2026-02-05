package com.mthsgimenez.fitcontrol.tenant.repository;

import com.mthsgimenez.fitcontrol.tenant.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Integer> {
}
