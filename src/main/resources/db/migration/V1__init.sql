-- V1__init.sql
-- Initial database schema with UUID v7 support

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create fruits table
CREATE TABLE fruits (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name TEXT NOT NULL
);

-- Create employees table
CREATE TABLE employees (
    employee_id UUID PRIMARY KEY DEFAULT uuidv7(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    job_title VARCHAR(100) NOT NULL
);

-- Insert sample data
INSERT INTO fruits (name) VALUES
    ('Orange'),
    ('Pear'),
    ('Apple');

-- Create indexes for better query performance
CREATE INDEX idx_employees_email ON employees(email);
CREATE INDEX idx_fruits_name ON fruits(name);
