# Como Testar a API de Produtos

## 1. A aplicação está rodando?

A aplicação deve estar em: `http://localhost:8081`

Se não estiver rodando, execute:

```bash
./gradlew bootRun
```

---

## 2. Acessar o pgAdmin (Banco de Dados)

- **URL**: http://localhost:8080
- **Email**: `admin@admin.com`
- **Senha**: `admin`

### Como conectar corretamente no banco:

1. No pgAdmin clique em **Servers → Create → Server**
2. Na aba **General**:
   - Name: `Product DB (Docker)`
3. Na aba **Connection**:
   - Host name/address: `localhost`
   - Port: `5432`
   - Maintenance database: `postgres`
   - Username: `postgres`
   - Password: `postgres`
4. Clique em **Save**

Depois de conectar, procure o banco chamado **`productdb`** (não o `postgres`).

Dentro de `productdb` → Schemas → public → Tables você deve ver a tabela `product` depois de criar alguns registros.

---

## 3. Testar o CRUD de Produtos (usando curl)

### Criar um produto
```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Notebook Dell XPS",
    "description": "Intel i7, 16GB RAM, 512GB SSD",
    "price": 7500.00
  }'
```

### Listar todos os produtos
```bash
curl http://localhost:8081/api/products
```

### Criar outro produto
```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mouse Logitech MX Master",
    "description": "Mouse sem fio ergonômico",
    "price": 299.90
  }'
```

### Listar com paginação
```bash
curl "http://localhost:8081/api/products?max=5&offset=0"
```

### Buscar produto por ID (substitua o 1 pelo ID real)
```bash
curl http://localhost:8081/api/products/1
```

---

## 4. Ver os dados no Banco (pgAdmin)

Depois de criar produtos via API:

1. Atualize o pgAdmin (botão direito no banco `productdb` → Refresh)
2. Vá em:
   - `productdb` → Schemas → public → Tables → `product`
3. Botão direito em `product` → **View/Edit Data** → **All Rows**

---

## 5. Dicas

- A aplicação está na porta **8081**
- O pgAdmin está na porta **8080**
- Se quiser mudar a porta da aplicação, edite o arquivo:
  `grails-app/conf/application.yml`

Procure por:

```yaml
server:
    port: 8081
```

---

## 6. Comandos úteis

```bash
# Subir a aplicação
./gradlew bootRun

# Subir os containers do banco e kafka
docker compose up -d

# Parar os containers
docker compose down
```

Boa sorte nos testes!
