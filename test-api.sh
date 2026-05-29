#!/bin/bash
#
# Simple script to test the Product API
# Usage: ./test-api.sh

echo "=========================================="
echo "   Testing Product API (Port 8081)        "
echo "=========================================="
echo ""

echo "1. Creating first product..."
curl -s -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Notebook Dell XPS",
    "description": "Intel i7, 16GB RAM, 512GB SSD",
    "price": 7500.00
  }'
echo -e "\n"

echo "2. Creating second product..."
curl -s -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mouse Logitech MX Master",
    "description": "Ergonomic wireless mouse",
    "price": 299.90
  }'
echo -e "\n"

echo "3. Listing all products..."
curl -s http://localhost:8081/api/products
echo -e "\n"

echo "4. Listing with pagination (max 5)..."
curl -s "http://localhost:8081/api/products?max=5&offset=0"
echo -e "\n"

echo "=========================================="
echo "Tests completed!"
echo "Open pgAdmin to inspect the data in the database."
echo "=========================================="
