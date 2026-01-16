package com.mthsgimenez.fitcontrol.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tenants", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "tenants_cnpj_key",
                columnNames = {"cnpj"}),
        @UniqueConstraint(name = "tenants_trade_name_key",
                columnNames = {"trade_name"}),
        @UniqueConstraint(name = "tenants_legal_name_key",
                columnNames = {"legal_name"}),
        @UniqueConstraint(name = "tenants_schema_name_key",
                columnNames = {"schema_name"})})
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "cnpj", nullable = false, length = 14)
    private String cnpj;

    @Column(name = "postal_code", nullable = false, length = 8)
    private String postalCode;

    @Column(name = "trade_name", length = 100)
    private String tradeName;

    @Column(name = "legal_name", nullable = false, length = 100)
    private String legalName;

    @Column(name = "schema_name", nullable = false, length = 50)
    private String schemaName;
}