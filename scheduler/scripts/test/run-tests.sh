#!/bin/bash

# Comprehensive test script for Airavata Scheduler
# This script runs all tests including unit, integration, and e2e tests

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCKER_COMPOSE_FILE="${PROJECT_ROOT}/docker-compose.yml"
COVERAGE_FILE="${PROJECT_ROOT}/coverage.out"
HTML_COVERAGE="${PROJECT_ROOT}/coverage.html"

echo -e "${GREEN}=== Airavata Scheduler Test Suite ===${NC}"
echo "Project Root: ${PROJECT_ROOT}"

# Check prerequisites
echo -e "\n${YELLOW}Checking prerequisites...${NC}"

if ! command -v go &> /dev/null; then
    echo -e "${RED}Error: Go is not installed${NC}"
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed${NC}"
    exit 1
fi

if ! command -v docker &> /dev/null || ! docker compose version &> /dev/null; then
    echo -e "${RED}Error: Docker Compose is not installed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All prerequisites met${NC}"

# Start Docker services
echo -e "\n${YELLOW}Starting Docker services...${NC}"
cd "${PROJECT_ROOT}"
docker compose -f "${DOCKER_COMPOSE_FILE}" down -v > /dev/null 2>&1 || true
docker compose -f "${DOCKER_COMPOSE_FILE}" up -d

# Wait for services to be healthy
echo "Waiting for services to be ready..."
sleep 5

# Check PostgreSQL
echo -n "Waiting for PostgreSQL..."
for i in {1..30}; do
    if docker compose -f "${DOCKER_COMPOSE_FILE}" exec -T postgres pg_isready -U test_user -d airavata_scheduler_test &> /dev/null; then
        echo -e " ${GREEN}✓${NC}"
        break
    fi
    echo -n "."
    sleep 2
    if [ $i -eq 30 ]; then
        echo -e " ${RED}✗${NC}"
        echo "PostgreSQL failed to start"
        docker compose -f "${DOCKER_COMPOSE_FILE}" logs postgres
        exit 1
    fi
done

# Check SFTP
echo -n "Waiting for SFTP..."
for i in {1..20}; do
    if nc -z localhost 2222 &> /dev/null; then
        echo -e " ${GREEN}✓${NC}"
        break
    fi
    echo -n "."
    sleep 1
    if [ $i -eq 20 ]; then
        echo -e " ${RED}✗${NC}"
        echo "SFTP failed to start"
        exit 1
    fi
done

echo -e "${GREEN}✓ All services are ready${NC}"

# Set environment variables for tests
export TEST_DB_HOST=localhost
export TEST_DB_PORT=5433
export TEST_DB_USER=test_user
export TEST_DB_PASSWORD=test_password
export TEST_DB_NAME=airavata_scheduler_test
export DATABASE_URL="postgres://test_user:test_password@localhost:5433/airavata_scheduler_test?sslmode=disable"
export TEST_SFTP_HOST=localhost
export TEST_SFTP_PORT=2222
export TEST_SFTP_USER=test_user
export TEST_SFTP_PASSWORD=test_password
export TEST_REDIS_HOST=localhost
export TEST_REDIS_PORT=6380

# Run unit tests
echo -e "\n${YELLOW}Running unit tests...${NC}"
go test -v -race -coverprofile="${COVERAGE_FILE}" -covermode=atomic ./... 2>&1 | tee test-output.log

# Check if tests passed
if [ ${PIPESTATUS[0]} -ne 0 ]; then
    echo -e "${RED}✗ Tests failed${NC}"
    TEST_RESULT=1
else
    echo -e "${GREEN}✓ All tests passed${NC}"
    TEST_RESULT=0
fi

# Generate coverage report
echo -e "\n${YELLOW}Generating coverage report...${NC}"
if [ -f "${COVERAGE_FILE}" ]; then
    go tool cover -func="${COVERAGE_FILE}" | tee coverage-summary.txt
    go tool cover -html="${COVERAGE_FILE}" -o "${HTML_COVERAGE}"
    
    # Extract total coverage
    TOTAL_COVERAGE=$(go tool cover -func="${COVERAGE_FILE}" | grep total | awk '{print $3}')
    echo -e "\n${GREEN}Total Coverage: ${TOTAL_COVERAGE}${NC}"
    echo "Coverage report: ${HTML_COVERAGE}"
else
    echo -e "${YELLOW}No coverage file generated${NC}"
fi

# Stop Docker services
echo -e "\n${YELLOW}Stopping Docker services...${NC}"
docker compose -f "${DOCKER_COMPOSE_FILE}" down -v

echo -e "\n${GREEN}=== Test Suite Complete ===${NC}"

exit ${TEST_RESULT}

