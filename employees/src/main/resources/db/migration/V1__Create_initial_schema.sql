-- V1__Create_initial_schema.sql

-- Create the main employees table based on the Employee entity
CREATE TABLE employees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(25) NOT NULL,
    last_name VARCHAR(25) NOT NULL,
    email VARCHAR(30) NOT NULL UNIQUE,
    employee_id VARCHAR(255) NOT NULL UNIQUE,
    encrypted_password VARCHAR(255) NOT NULL,
    username VARCHAR(255) UNIQUE,
    department VARCHAR(255),
    gender VARCHAR(255),
    last_logged_in DATETIME(6),
    last_password_reset_date DATETIME(6),
    status VARCHAR(255) NOT NULL,
    verification_token VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
);

-- Create roles and authorities tables
CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(23) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE authorities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(23) NOT NULL,
    PRIMARY KEY (id)
);

-- Create the join tables with corrected column names
CREATE TABLE employees_roles (
    employees_id BIGINT NOT NULL,
    roles_id BIGINT NOT NULL,
    PRIMARY KEY (employees_id, roles_id),
    FOREIGN KEY (employees_id) REFERENCES employees(id),
    FOREIGN KEY (roles_id) REFERENCES roles(id)
);

CREATE TABLE roles_authorities (
    roles_id BIGINT NOT NULL,
    authorities_id BIGINT NOT NULL,
    PRIMARY KEY (roles_id, authorities_id),
    FOREIGN KEY (roles_id) REFERENCES roles(id),
    FOREIGN KEY (authorities_id) REFERENCES authorities(id)
);

-- Seed the initial data for roles and authorities
INSERT INTO roles (name) VALUES ('ROLE_EMPLOYEE'), ('ROLE_MANAGER'), ('ROLE_ADMIN');

INSERT INTO authorities (name) VALUES ('READ'), ('WRITE'), ('DELETE');

-- Link roles to their authorities
INSERT INTO roles_authorities (roles_id, authorities_id) VALUES
    -- Employee Permissions (READ only)
    ((SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE'), (SELECT id FROM authorities WHERE name = 'READ')),

    -- Manager Permissions (READ and WRITE)
    ((SELECT id FROM roles WHERE name = 'ROLE_MANAGER'), (SELECT id FROM authorities WHERE name = 'READ')),
    ((SELECT id FROM roles WHERE name = 'ROLE_MANAGER'), (SELECT id FROM authorities WHERE name = 'WRITE')),

    -- Admin Permissions (READ, WRITE, and DELETE)
    ((SELECT id FROM roles WHERE name = 'ROLE_ADMIN'), (SELECT id FROM authorities WHERE name = 'READ')),
    ((SELECT id FROM roles WHERE name = 'ROLE_ADMIN'), (SELECT id FROM authorities WHERE name = 'WRITE')),
    ((SELECT id FROM roles WHERE name = 'ROLE_ADMIN'), (SELECT id FROM authorities WHERE name = 'DELETE'));