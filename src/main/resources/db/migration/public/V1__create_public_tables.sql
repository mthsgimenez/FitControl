CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE public.tenants (
    id serial PRIMARY KEY,
    cnpj varchar(14) NOT NULL UNIQUE,
    postal_code varchar(8) NOT NULL,
    trade_name varchar(100) NULL UNIQUE,
    legal_name varchar(100) NOT NULL UNIQUE,
    schema_name varchar(50) NOT NULL UNIQUE
);

CREATE TABLE public.roles (
    id integer PRIMARY KEY,
    name varchar(30) NOT NULL UNIQUE
);

CREATE TABLE public.users (
    id serial PRIMARY KEY,
    email varchar(70) NOT NULL,
    password_hash varchar(60) NOT NULL,
    tenant_id integer NOT NULL REFERENCES tenants(id),
    UNIQUE (email, tenant_id)
);

CREATE TYPE duration_unit AS ENUM ('duration_day', 'duration_week', 'duration_month', 'duration_year');
CREATE TYPE membership_status AS ENUM ('membership_active', 'membership_pending', 'membership_expired', 'membership_canceled');