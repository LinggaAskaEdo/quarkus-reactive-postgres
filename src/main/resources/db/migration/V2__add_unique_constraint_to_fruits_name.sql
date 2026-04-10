-- V2__add_unique_constraint_to_fruits_name.sql

-- Add unique constraint to fruit names to prevent duplicates
ALTER TABLE fruits ADD CONSTRAINT uq_fruits_name UNIQUE (name);
