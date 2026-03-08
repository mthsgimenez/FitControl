package com.mthsgimenez.fitcontrol.infra.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {
    private final String defaultSchema = "public";

    @Override
    public Object resolveCurrentTenantIdentifier() {
        String manual = TenantContext.getTenantSchema();
        if (manual != null) return manual;

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return defaultSchema;

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String tenantUUID = jwtAuth.getToken().getClaim("tenant");
            return tenantUUID == null
                    ? defaultSchema
                    : "tenant_" + tenantUUID.split("-")[0];
        }
        return defaultSchema;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
