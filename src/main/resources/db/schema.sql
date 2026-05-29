-- =====================================================================
-- Product API - PL/pgSQL Study Scripts
-- =====================================================================
--
-- This file contains the database structure + Stored Procedures.
--
-- Educational objective:
--   Teach how to create and call PostgreSQL stored procedures from Java.
--
-- How Spring Boot loads this file:
--   In application.yml the following is configured:
--     spring.sql.init.mode=always
--     spring.sql.init.data-locations=classpath:db/schema.sql
--
-- This makes Spring execute this script every time the application starts
-- (useful during studies; in production use Flyway or Liquibase).
--
-- =====================================================================

-- Remove old procedures to allow clean recreation during study
DROP PROCEDURE IF EXISTS sp_create_product(VARCHAR, VARCHAR, NUMERIC, INOUT BIGINT);
DROP PROCEDURE IF EXISTS sp_update_product(BIGINT, VARCHAR, VARCHAR, NUMERIC);
DROP PROCEDURE IF EXISTS sp_delete_product(BIGINT);

-- Create the products table (if it does not exist)
CREATE TABLE IF NOT EXISTS products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    price       NUMERIC(19,2) NOT NULL CHECK (price > 0),
    created_at  TIMESTAMP DEFAULT NOW()
);

-- =====================================================================
-- PROCEDURE: sp_create_product
-- =====================================================================
-- Creates a new product and returns the generated ID through the INOUT parameter.
--
-- Parameters:
--   p_name        → product name (required)
--   p_description → description (optional)
--   p_price       → price (required, > 0)
--   p_id          → INOUT: the database fills this with the generated ID
--
-- Example call in psql:
--   CALL sp_create_product('Mouse', 'Wireless', 89.90, NULL);
-- =====================================================================
CREATE OR REPLACE PROCEDURE sp_create_product(
    IN  p_name        VARCHAR,
    IN  p_description VARCHAR,
    IN  p_price       NUMERIC,
    INOUT p_id        BIGINT DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO products (name, description, price, created_at)
    VALUES (p_name, p_description, p_price, NOW())
    RETURNING id INTO p_id;

    RAISE NOTICE 'Procedure sp_create_product: Product created with id=%', p_id;
END;
$$;

-- =====================================================================
-- PROCEDURE: sp_update_product
-- =====================================================================
-- Updates an existing product.
-- Raises an exception if the product is not found.
-- =====================================================================
CREATE OR REPLACE PROCEDURE sp_update_product(
    IN p_id          BIGINT,
    IN p_name        VARCHAR,
    IN p_description VARCHAR,
    IN p_price       NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE products
    SET name        = p_name,
        description = p_description,
        price       = p_price
    WHERE id = p_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Product with id % not found', p_id
            USING HINT = 'Verify if the ID exists in the products table';
    END IF;

    RAISE NOTICE 'Procedure sp_update_product: Product % updated successfully', p_id;
END;
$$;

-- =====================================================================
-- PROCEDURE: sp_delete_product
-- =====================================================================
-- Deletes a product from the database.
-- Raises an exception if the ID does not exist.
-- =====================================================================
CREATE OR REPLACE PROCEDURE sp_delete_product(
    IN p_id BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM products WHERE id = p_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Product with id % not found', p_id;
    END IF;

    RAISE NOTICE 'Procedure sp_delete_product: Product % deleted successfully', p_id;
END;
$$;

-- =====================================================================
-- Study Tips
-- =====================================================================
-- 1. View the source code of the procedures:
--    \sf sp_create_product
--
-- 2. List all procedures:
--    \df sp_*
--
-- 3. Test manually:
--    CALL sp_create_product('Keyboard', 'Mechanical', 250.00, NULL);
--    SELECT * FROM products;
--
-- 4. Try creating a trigger that automatically updates an "updated_at" field
--    when sp_update_product is called.
--
-- =====================================================================
