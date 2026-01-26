package com.mthsgimenez.fitcontrol.service;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SchemaService {

    private final JdbcTemplate jdbcTemplate;
    private final String migrationsLocation;

    public SchemaService(
            JdbcTemplate jdbcTemplate,
            @Value("${app.migration.tenant-location}") String migrationsLocation
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.migrationsLocation = migrationsLocation;
    }

    private void createSchema(String schemaName) {
        String sql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
        jdbcTemplate.execute(sql);
    }

    private void runMigrations(String schemaName) {
        Flyway.configure()
                .dataSource(jdbcTemplate.getDataSource())
                .schemas(schemaName)
                .locations(migrationsLocation)
                .load()
                .migrate();
    }

    public void createSchemaAndMigrate(String schemaName) {
        createSchema(schemaName);
        runMigrations(schemaName);
    }
}
