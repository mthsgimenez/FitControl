CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE public.tenants (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
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
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    uuid UUID DEFAULT gen_random_uuid() UNIQUE,
    email varchar(70) NOT NULL,
    password_hash varchar(60) NOT NULL,
    tenant_id integer NOT NULL REFERENCES tenants(id),
    UNIQUE (email)
);

CREATE UNIQUE INDEX idx_users_uuid on users(uuid);

CREATE TYPE duration_unit AS ENUM ('DURATION_DAY', 'DURATION_WEEK', 'DURATION_MONTH', 'DURATION_YEAR');
CREATE TYPE membership_status AS ENUM ('MEMBERSHIP_ACTIVE', 'MEMBERSHIP_PENDING', 'MEMBERSHIP_EXPIRED', 'MEMBERSHIP_CANCELED');