package com.mthsgimenez.fitcontrol.infra.multitenancy;

import com.mthsgimenez.fitcontrol.auth.model.User;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {
    private final String defaultSchema = "public";

    @Override
    public Object resolveCurrentTenantIdentifier() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return defaultSchema;
        }

        if (auth.getPrincipal() instanceof User user) {
            return user.getTenant().getSchemaName();
        }

        return defaultSchema;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
