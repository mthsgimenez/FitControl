package com.mthsgimenez.fitcontrol.tenant;

import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TenantService(
            TenantRepository tenantRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.tenantRepository = tenantRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public Tenant createTenant(TenantDTO data) {
        UUID tenantUUID = UUID.randomUUID();
        String schemaName = "tenant_" + tenantUUID.toString().split("-")[0];

        Tenant newTenant =  new Tenant();
        newTenant.setUuid(tenantUUID);
        newTenant.setCnpj(data.cnpj());
        newTenant.setLegalName(data.legalName());
        newTenant.setTradeName(data.tradeName());
        newTenant.setPostalCode(data.postalCode());
        newTenant.setSchemaName(schemaName);
        tenantRepository.save(newTenant);

        applicationEventPublisher.publishEvent(
                new TenantCreatedEvent(schemaName)
        );

        return newTenant;
    }
}
