package com.mthsgimenez.fitcontrol.infra.multitenancy;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MultitenancyConfig {

    private final MultiTenantConnectionProvider multiTenantConnectionProvider;
    private final CurrentTenantIdentifierResolver currentTenantIdentifierResolver;
    private final DataSource dataSource;
    private final JpaVendorAdapter jpaVendorAdapter;

    public MultitenancyConfig(MultiTenantConnectionProvider multiTenantConnectionProvider,
                              CurrentTenantIdentifierResolver currentTenantIdentifierResolver,
                              DataSource dataSource,
                              JpaVendorAdapter jpaVendorAdapter
    ) {
        this.multiTenantConnectionProvider = multiTenantConnectionProvider;
        this.currentTenantIdentifierResolver = currentTenantIdentifierResolver;
        this.dataSource = dataSource;
        this.jpaVendorAdapter = jpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.mthsgimenez.fitcontrol");
        em.setJpaVendorAdapter(jpaVendorAdapter);
        em.setJpaPropertyMap(properties);

        return em;
    }
}
