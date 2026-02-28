package com.mthsgimenez.fitcontrol.infra.multitenancy;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider {
    private final DataSource dataSource;

    public MultiTenantConnectionProviderImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        if (tenantIdentifier != null) {
            connection.setSchema(tenantIdentifier.toString());
        }

        return connection;
    }

    @Override
    public void releaseConnection(
            Object tenantIdentifier,
            Connection connection
    ) throws SQLException {
        connection.setSchema("public");
        releaseAnyConnection(connection);
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> aClass) {
        return aClass.isInstance(this);
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        if (aClass.isInstance(this)) {
            return aClass.cast(this);
        }
        throw new UnsupportedOperationException("Cannot unwrap to " + aClass);
    }
}
