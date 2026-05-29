# Product API - Spring Boot + Kafka + PL/pgSQL

API RESTful para gerenciamento de produtos com **Spring Boot 3**, **PostgreSQL + PL/pgSQL**, **Apache Kafka** e **Docker Compose**.

Este projeto foi migrado de Grails para **Spring Boot Java puro** com o objetivo de estudar:

- Integração Spring Boot + Kafka (event-driven)
- Chamadas de **Stored Procedures PL/pgSQL** a partir do Java
- Separação clara entre comandos (via Kafka) e consultas (via JPA)

## Tecnologias Utilizadas

- **Spring Boot 3.4.5** (Java 17)
- **Spring Data JPA** + Hibernate
- **PostgreSQL 16** + **PL/pgSQL** (Stored Procedures)
- **Spring Kafka**
- **Docker Compose**
- **JdbcTemplate + SimpleJdbcCall** (para chamar procedures)

## Conceito de Arquitetura (Importante para Estudo)

```
REST Controller
      │
      ▼
ProductService
      │
      ├─► CUD (Create/Update/Delete) → ProductEventProducer → Kafka
      │
      └─► Reads (GET) → ProductJpaRepository (JPA)
                              │
                              ▼
                    ProductEventConsumer
                              │
                              ▼
                    ProductProcedureRepository
                              │
                              ▼
                    sp_create_product / sp_update_product / sp_delete_product
                              │
                              ▼
                         PostgreSQL (PL/pgSQL)
```

**Fluxo de estudo recomendado:**
- Todo Create/Update/Delete passa por **Kafka** e depois chama **Stored Procedure**.
- Leitura é feita diretamente via JPA (padrão comum em sistemas reais).

## Como Executar

### 1. Subir infraestrutura

```bash
docker compose up -d
```

### 2. Rodar a aplicação

```bash
./gradlew bootRun
```

A aplicação sobe na porta **8081**.

## Endpoints

| Método | Endpoint                  | Descrição                              |
|--------|---------------------------|----------------------------------------|
| POST   | `/api/products`           | Cria produto (via Kafka + Procedure)   |
| PUT    | `/api/products/{id}`      | Atualiza produto (via Kafka + Procedure)|
| DELETE | `/api/products/{id}`      | Remove produto (via Kafka + Procedure) |
| GET    | `/api/products`           | Lista todos (via JPA)                  |
| GET    | `/api/products/{id}`      | Busca por ID (via JPA)                 |

Exemplo de criação:

```json
POST /api/products
{
  "name": "Notebook Dell",
  "description": "16GB RAM",
  "price": 4500.00
}
```

## PL/pgSQL - Stored Procedures (Principal para Estudo)

Os procedimentos estão em:

```
src/main/resources/db/schema.sql
```

Principais procedures:

- `sp_create_product(name, description, price, INOUT id)`
- `sp_update_product(id, name, description, price)`
- `sp_delete_product(id)`

### Como estudar as procedures

1. Após subir o projeto, acesse o PostgreSQL:
   ```bash
   docker exec -it product-db psql -U postgres -d productdb
   ```

2. Liste as procedures:
   ```sql
   \df sp_*
   ```

3. Veja o código de uma procedure:
   ```sql
   \sf sp_create_product
   ```

## Kafka

- Tópico: `product-events`
- Eventos: `PRODUCT_CREATED`, `PRODUCT_UPDATED`, `PRODUCT_DELETED`

O consumer processa o evento e chama a procedure correspondente.

## Guia de Estudo - O que você deve aprender com este projeto

### 1. Arquitetura Event-Driven + Stored Procedures

Este projeto implementa um padrão poderoso:
- Operações de escrita (CUD) **nunca** vão direto no banco a partir do Service.
- Elas publicam um evento no Kafka.
- O Consumer recebe o evento e chama a Stored Procedure.

**Vantagens didáticas:**
- Você vê claramente a separação entre "intenção" e "execução".
- Facilita adicionar validações complexas dentro da procedure (banco).
- Simula cenários reais de sistemas distribuídos.

### 2. Principais arquivos para estudar

| Arquivo | O que estudar |
|---------|---------------|
| `db/schema.sql` | Como escrever procedures PL/pgSQL |
| `ProductProcedureRepository.java` | Como chamar procedures do Java com `SimpleJdbcCall` |
| `ProductEventConsumer.java` | Como rotear eventos para diferentes procedures |
| `ProductEventProducer.java` | Como publicar eventos de forma limpa |
| `ProductService.java` | Como o Service só publica eventos (sem tocar no banco) |

### 3. Exercícios Recomendados

1. Adicione um campo `updated_at` na tabela e atualize as procedures.
2. Crie uma trigger que popula automaticamente `updated_at` no UPDATE.
3. Adicione validação de preço mínimo dentro da procedure `sp_update_product`.
4. Crie um novo evento `PRODUCT_PRICE_CHANGED` e uma procedure específica.
5. Tente trocar `SimpleJdbcCall` por `JdbcTemplate` + `CallableStatement` manualmente.

### 4. Como inspecionar o que está acontecendo

```bash
# Ver logs da aplicação (eventos Kafka + chamadas de procedure)
./gradlew bootRun

# Acessar o PostgreSQL
docker exec -it product-db psql -U postgres -d productdb

# Dentro do psql:
\df sp_*                    -- lista procedures
\sf sp_create_product       -- mostra o código da procedure
TABLE products;             -- vê os dados
```

## Dicas de Estudo

- Experimente adicionar novos parâmetros nas procedures (ex: `updated_at`)
- Adicione validações dentro das procedures (ex: preço mínimo)
- Tente criar uma trigger que popula `created_at` automaticamente
- Adicione um novo evento (ex: `PRODUCT_PRICE_CHANGED`)

## Comandos Úteis

```bash
# Build
./gradlew build

# Rodar testes
./gradlew test

# Parar containers
docker compose down
```

---

Projeto migrado para fins de estudo de **Spring Boot + Kafka + PL/pgSQL**.
