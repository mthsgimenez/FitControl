package com.mthsgimenez.fitcontrol.infra.multitenancy;

public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenantSchema(String schemaName) {
        currentTenant.set(schemaName);
    }

    public static String getTenantSchema() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
