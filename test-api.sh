#!/bin/bash
#
# Script para testar a API de Produtos
# Rode com: ./test-api.sh
#

echo "=========================================="
echo "   Testando API de Produtos (Porta 8081)  "
echo "=========================================="
echo ""

echo "1. Criando primeiro produto..."
curl -s -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Notebook Dell XPS",
    "description": "Intel i7, 16GB RAM, 512GB SSD",
    "price": 7500.00
  }'
echo -e "\n"

echo "2. Criando segundo produto..."
curl -s -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mouse Logitech MX Master",
    "description": "Mouse sem fio ergonômico",
    "price": 299.90
  }'
echo -e "\n"

echo "3. Listando todos os produtos..."
curl -s http://localhost:8081/api/products
echo -e "\n"

echo "4. Listando com paginação (máx 5)..."
curl -s "http://localhost:8081/api/products?max=5&offset=0"
echo -e "\n"

echo "=========================================="
echo "Testes finalizados!"
echo "Abra o pgAdmin para ver os dados no banco."
echo "=========================================="
