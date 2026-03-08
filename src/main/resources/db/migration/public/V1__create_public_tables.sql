CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE public.tenants (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    cnpj varchar(14) NOT NULL UNIQUE,
    postal_code varchar(8) NOT NULL,
    trade_name varchar(100) NULL UNIQUE,
    legal_name varchar(100) NOT NULL UNIQUE,
    schema_name varchar(50) NOT NULL UNIQUE,
    gateway_account_id varchar(255),
    gateway_product_id varchar(255)
);

CREATE TABLE public.roles (
    id integer PRIMARY KEY,
    name varchar(30) NOT NULL UNIQUE
);

CREATE TABLE public.users (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    email varchar(70) NOT NULL,
    password_hash varchar(60) NOT NULL,
    tenant_id integer NOT NULL REFERENCES tenants(id),
    UNIQUE (email)
);

CREATE TABLE public.user_roles(
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id integer NOT NULL REFERENCES users(id),
    role_id integer NOT NULL REFERENCES roles(id),
    UNIQUE (user_id, role_id)
);

CREATE UNIQUE INDEX idx_users_uuid on users(uuid);