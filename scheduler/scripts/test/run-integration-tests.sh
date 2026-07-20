#!/bin/bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DOCKER_COMPOSE_FILE="${PROJECT_ROOT}/docker-compose.yml"

echo "=== Running Automated Integration Tests ==="

# Start Docker services
echo "Starting Docker services..."
cd "${PROJECT_ROOT}"
docker compose -f "${DOCKER_COMPOSE_FILE}" --profile test up -d --remove-orphans

# Wait for services to be healthy
echo "Waiting for services to become healthy (3 minutes)..."
sleep 180

# Build worker binary
echo "Building worker binary..."
make build-worker

# Set TEST_DATABASE_URL for tests (aligns with docker-compose postgres)
export TEST_DATABASE_URL="postgres://user:password@localhost:5432/airavata_scheduler_test?sslmode=disable"

# Run integration tests with extended timeout
echo "Running Go integration tests..."
go test -v -timeout 30m ./tests/integration/...

# Stop Docker services
echo "Stopping Docker services..."
docker compose -f "${DOCKER_COMPOSE_FILE}" --profile test down -v

echo "✓ Automated integration tests complete"