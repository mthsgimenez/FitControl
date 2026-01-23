package com.mthsgimenez.fitcontrol.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SchemaService {

    private final JdbcTemplate jdbcTemplate;

    public SchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String createSchema() {
        String schemaName = "schema_" + UUID.randomUUID().toString().replace("-", "").toLowerCase();
        String sql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
        jdbcTemplate.execute(sql);

        // TODO: gerar as tabelas do schema com o flyway

        return schemaName;
    }
}
