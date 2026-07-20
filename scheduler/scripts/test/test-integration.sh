#!/bin/bash

# Run integration tests with Docker services

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCKER_COMPOSE_FILE="${PROJECT_ROOT}/docker-compose.yml"

echo "=== Running Integration Tests ==="

# Start Docker services
echo "Starting Docker services..."
cd "${PROJECT_ROOT}"
docker compose -f "${DOCKER_COMPOSE_FILE}" up -d

# Wait for services
echo "Waiting for services..."
sleep 10

# Set environment variables
export TEST_DB_HOST=localhost
export TEST_DB_PORT=5433
export TEST_DB_USER=test_user
export TEST_DB_PASSWORD=test_password
export TEST_DB_NAME=airavata_scheduler_test
export DATABASE_URL="postgres://test_user:test_password@localhost:5433/airavata_scheduler_test?sslmode=disable"

# Run integration tests
go test -v -timeout 30m ./tests/integration/...

# Stop Docker services
docker compose -f "${DOCKER_COMPOSE_FILE}" down -v

echo "✓ Integration tests complete"

