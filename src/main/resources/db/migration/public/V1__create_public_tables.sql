CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE public.tenants (
    id serial PRIMARY KEY,
    cnpj varchar(14) NOT NULL UNIQUE,
    postal_code varchar(8) NOT NULL,
    trade_name varchar(100) NULL UNIQUE,
    legal_name varchar(100) NOT NULL UNIQUE,
    schema_name varchar(50) NOT NULL UNIQUE
);

CREATE TABLE public.exercise_categories (
    id integer PRIMARY KEY,
    name varchar(50) NOT NULL UNIQUE
);

CREATE TABLE public.exercises (
    id serial PRIMARY KEY,
    name varchar(50) NOT NULL UNIQUE,
    category_id integer NOT NULL REFERENCES exercise_categories(id)
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