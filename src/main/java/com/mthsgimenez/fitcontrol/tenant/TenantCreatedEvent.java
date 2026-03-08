package com.mthsgimenez.fitcontrol.tenant;

public record TenantCreatedEvent(
        Tenant tenant,
        String ownerEmail
) {}
