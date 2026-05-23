# Product API - Grails 7

API RESTful para gerenciamento de produtos com **Grails 7**, **GORM/Hibernate**, **PostgreSQL**, **Apache Kafka** e **Docker Compose**.

Este projeto foi criado seguindo as melhores práticas do mercado para fins de estudo.

## Tecnologias Utilizadas

- **Grails 7.1.1** (baseado em Spring Boot 3)
- **Java 17**
- **PostgreSQL 16** (via Docker)
- **Apache Kafka** + Zookeeper (via Docker)
- **GORM** (Hibernate)
- **Spring Kafka**
- **Docker Compose**

## Requisitos

- Java 17 ou superior
- Docker + Docker Compose
- IntelliJ IDEA (recomendado) ou qualquer IDE com suporte a Gradle

## Segurança (Importante!)

Este projeto foi preparado para ser publicado no GitHub sem expor credenciais.

### Como funciona

- As senhas e credenciais estão **externalizadas** usando variáveis de ambiente.
- O arquivo `.env` **não deve nunca** ser commitado (já está no `.gitignore`).
- O arquivo `.env.example` serve como modelo.

### Configuração inicial (recomendado)

1. Copie o arquivo de exemplo:

   ```bash
   cp .env.example .env
   ```

2. Edite o `.env` e troque as senhas (principalmente em produção).

3. O `docker-compose.yml` e o `application.yml` vão ler automaticamente as variáveis do `.env`.

### O que NUNCA deve ser commitado

- `.env`
- `.env.local`
- Qualquer arquivo com senhas reais

Se você já subiu algo sensível por acidente, use `git rm --cached` + force push ou rotacione as credenciais.

## Como Executar o Projeto

### 1. Subir os Containers (PostgreSQL + Kafka + pgAdmin)

Na raiz do projeto, execute:

```bash
docker compose up -d
```

Isso vai subir:
- PostgreSQL na porta **5432**
- Kafka na porta **9092**
- pgAdmin na porta **8080**

### 2. Rodar a Aplicação

Você tem duas formas:

**Usando Gradle (recomendado):**

```bash
./gradlew bootRun
```

**Ou usando o Grails Wrapper:**

```bash
./grailsw run-app
```

A aplicação vai subir na porta **8080**.

### 3. Acessar a API

Base URL: `http://localhost:8080/api/products`

### Endpoints Disponíveis

| Método | Endpoint                  | Descrição                     |
|--------|---------------------------|-------------------------------|
| GET    | `/api/products`           | Lista produtos (com paginação) |
| GET    | `/api/products/{id}`      | Busca produto por ID          |
| POST   | `/api/products`           | Cria novo produto             |
| PUT    | `/api/products/{id}`      | Atualiza produto              |
| DELETE | `/api/products/{id}`      | Remove produto                |

#### Exemplo de Paginação

```
GET /api/products?max=5&offset=0
```

Resposta:

```json
{
  "content": [...],
  "totalElements": 27,
  "page": 0,
  "size": 5,
  "totalPages": 6
}
```

#### Exemplo de Criação de Produto

```json
POST /api/products
{
  "name": "Notebook Dell",
  "description": "Notebook com 16GB de RAM",
  "price": 4500.00
}
```

Ao criar um produto, um evento é publicado automaticamente no Kafka.

## Kafka

- **Tópico:** `product-events`
- Todo produto criado publica um evento `PRODUCT_CREATED`

Você pode consumir as mensagens usando o consumer já implementado (ele apenas loga no console da aplicação).

## Acesso ao Banco de Dados

### PostgreSQL

- Host: `localhost:5432`
- Banco: `productdb`
- Usuário: `postgres`
- Senha: `postgres`

### pgAdmin (Interface Web)

- URL: http://localhost:8080
- Email: `admin@admin.com`
- Senha: `admin`

## Estrutura do Projeto

```
src/main/groovy/product/api/
├── command/                 # Command Objects (validação de entrada)
│   ├── ProductCreateCommand.groovy
│   └── ProductUpdateCommand.groovy
├── dto/                     # Objetos de resposta
│   └── PagedResult.groovy
├── kafka/                   # Producer e Consumer
│   ├── ProductEventProducer.groovy
│   └── ProductEventConsumer.groovy
├── Product.groovy           # Domain (entidade)
├── ProductController.groovy # REST Controller
└── ProductService.groovy    # Service com regras de negócio
```

## Como Abrir no IntelliJ

1. Abra o IntelliJ
2. `File → Open`
3. Selecione a pasta `product-api`
4. Aguarde o Gradle sincronizar (primeira vez pode demorar)
5. O projeto será reconhecido automaticamente

## Comandos Úteis

```bash
# Compilar o projeto
./gradlew build

# Rodar testes (quando houver)
./gradlew test

# Limpar e recompilar
./gradlew clean build

# Parar containers
docker compose down
```

## Observações para Estudo

- O projeto usa **Command Objects** para validação de entrada (melhor prática)
- O **Producer** está separado do Service (boa separação de responsabilidades)
- A paginação retorna um objeto estruturado (`PagedResult`)
- A integração com Kafka é feita de forma limpa e testável

---

Desenvolvido para fins de estudo em Grails 7 + Kafka + PostgreSQL.
```

Obrigado por estudar com este projeto! Se quiser evoluir (adicionar testes, mais eventos no Kafka, autenticação, etc.), é só pedir.