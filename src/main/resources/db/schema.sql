-- =====================================================================
-- Product API - PL/pgSQL Study Scripts
-- =====================================================================
--
-- Este arquivo contém a estrutura do banco + Stored Procedures.
--
-- Objetivo didático:
--   Ensinar como criar e chamar procedures do PostgreSQL a partir do Java.
--
-- Como o Spring Boot carrega este arquivo:
--   Em application.yml está configurado:
--     spring.sql.init.mode=always
--     spring.sql.init.data-locations=classpath:db/schema.sql
--
-- Isso faz com que o Spring execute este script toda vez que a aplicação sobe
-- (útil durante estudos, em produção use Flyway ou Liquibase).
--
-- =====================================================================

-- Remove procedures antigas para permitir recriação limpa durante estudo
DROP PROCEDURE IF EXISTS sp_create_product(VARCHAR, VARCHAR, NUMERIC, INOUT BIGINT);
DROP PROCEDURE IF EXISTS sp_update_product(BIGINT, VARCHAR, VARCHAR, NUMERIC);
DROP PROCEDURE IF EXISTS sp_delete_product(BIGINT);

-- Cria a tabela de produtos (se não existir)
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
-- Cria um novo produto e retorna o ID gerado através do parâmetro INOUT.
--
-- Parâmetros:
--   p_name        → nome do produto (obrigatório)
--   p_description → descrição (opcional)
--   p_price       → preço (obrigatório, > 0)
--   p_id          → INOUT: o banco preenche com o ID gerado
--
-- Exemplo de chamada no psql:
--   CALL sp_create_product('Mouse', 'Sem fio', 89.90, NULL);
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
-- Atualiza um produto existente.
-- Lança exceção se o produto não for encontrado.
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
-- Remove um produto do banco.
-- Lança exceção se o ID não existir.
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
-- Dicas de estudo
-- =====================================================================
-- 1. Veja o código das procedures:
--    \sf sp_create_product
--
-- 2. Liste todas as procedures:
--    \df sp_*
--
-- 3. Teste manualmente:
--    CALL sp_create_product('Teclado', 'Mecânico', 250.00, NULL);
--    SELECT * FROM products;
--
-- 4. Tente criar uma trigger que atualiza automaticamente um campo
--    "updated_at" quando sp_update_product for chamada.
--
-- =====================================================================
