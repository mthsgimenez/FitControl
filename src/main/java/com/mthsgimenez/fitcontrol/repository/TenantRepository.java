package com.mthsgimenez.fitcontrol.repository;

import com.mthsgimenez.fitcontrol.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Integer> {
}
