# Airavata Scheduler Makefile

# Variables
DOCKER_COMPOSE_FILE = docker-compose.yml
GO_VERSION = 1.21
PROJECT_NAME = airavata-scheduler

# Colors for output
RED = \033[0;31m
GREEN = \033[0;32m
YELLOW = \033[1;33m
BLUE = \033[0;34m
NC = \033[0m # No Color

.PHONY: help build test test-unit test-integration test-all clean docker-build docker-up docker-down docker-logs setup-services wait-services

# Default target
help: ## Show this help message
	@echo "$(BLUE)Airavata Scheduler - Available Commands$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "$(GREEN)%-20s$(NC) %s\n", $$1, $$2}'

# Build targets
build: ## Build the scheduler, worker, and CLI binaries
	@echo "$(BLUE)Building binaries...$(NC)"
	mkdir -p bin
	go build -o bin/scheduler ./cmd/scheduler
	GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -o bin/worker ./cmd/worker
	GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -o bin/airavata ./cmd/cli
	@echo "$(GREEN)Build completed: bin/scheduler, bin/worker, bin/airavata$(NC)"

build-server: ## Build only the scheduler binary
	@echo "$(BLUE)Building scheduler binary...$(NC)"
	mkdir -p bin
	go build -o bin/scheduler ./cmd/scheduler
	@echo "$(GREEN)Scheduler built: bin/scheduler$(NC)"

build-worker: ## Build only the worker binary
	@echo "$(BLUE)Building worker binary...$(NC)"
	mkdir -p bin
	GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -o bin/worker ./cmd/worker
	@echo "$(GREEN)Worker built: bin/worker$(NC)"

build-cli: ## Build only the CLI binary
	@echo "$(BLUE)Building CLI binary...$(NC)"
	mkdir -p bin
	GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -o bin/airavata ./cmd/cli
	@echo "$(GREEN)CLI built: bin/airavata$(NC)"

# Test targets
test: test-unit ## Run unit tests only

test-unit: ## Run unit tests
	@echo "$(BLUE)Running unit tests...$(NC)"
	go test -v ./core/domain/... ./core/service/... ./core/port/... ./adapters/... ./core/app/...
	@echo "$(GREEN)Unit tests completed$(NC)"

test-integration: proto-check setup-test-services ## Run integration tests
	@echo "$(BLUE)Running integration tests...$(NC)"
	go test -v ./tests/integration/...
	@echo "$(GREEN)Integration tests completed$(NC)"

test-integration-setup: spicedb-schema-upload ## Integration test environment ready
	@echo "$(GREEN)Integration test environment ready$(NC)"

test-storage: setup-test-services ## Run storage adapter tests
	@echo "$(BLUE)Running storage adapter tests...$(NC)"
	go test -v ./tests/integration/storage_*.go
	@echo "$(GREEN)Storage adapter tests completed$(NC)"

test-compute: setup-test-services ## Run compute adapter tests
	@echo "$(BLUE)Running compute adapter tests...$(NC)"
	go test -v ./tests/integration/compute_*.go
	@echo "$(GREEN)Compute adapter tests completed$(NC)"

test-adapters: setup-test-services ## Run external adapter tests
	@echo "$(BLUE)Running external adapter tests...$(NC)"
	go test -v ./adapters/external/...
	@echo "$(GREEN)External adapter tests completed$(NC)"

test-e2e: setup-test-services ## Run end-to-end workflow tests
	@echo "$(BLUE)Running end-to-end workflow tests...$(NC)"
	go test -v ./tests/integration/adapter_e2e_workflow_test.go
	@echo "$(GREEN)End-to-end workflow tests completed$(NC)"

test-performance: setup-test-services ## Run performance tests
	@echo "$(BLUE)Running performance tests...$(NC)"
	go test -v ./tests/integration/adapter_performance_test.go
	@echo "$(GREEN)Performance tests completed$(NC)"

test-all: test-unit test-integration ## Run all tests

cold-start-test: clean ## Run full test suite from cold start
	@echo "$(BLUE)Running cold-start test validation...$(NC)"
	./scripts/validate-cold-start.sh
	go mod download
	make proto
	./scripts/generate-slurm-munge-key.sh
	# Master SSH keys are no longer used - each resource generates its own keys during registration
	# @echo "$(BLUE)Generating master SSH key fixtures...$(NC)"
	# mkdir -p tests/fixtures
	# rm -f tests/fixtures/master_ssh_key tests/fixtures/master_ssh_key.pub
	# ssh-keygen -t rsa -b 2048 -f tests/fixtures/master_ssh_key -N "" -C "airavata-test-master"
	docker compose down -v --remove-orphans
	docker compose up -d
	./scripts/wait-for-services.sh
	make spicedb-schema-upload
	make build
	go test ./tests/unit/... -v -timeout 5m
	go test ./tests/integration/... -v -timeout 10m
	@echo "$(GREEN)Cold-start tests completed$(NC)"

cold-start-test-csv: ## Run cold start test with CSV report generation
	@echo "$(BLUE)Running cold-start test with CSV report generation...$(NC)"
	@echo "$(YELLOW)This will destroy all containers and volumes, then recreate from scratch$(NC)"
	@echo "$(YELLOW)Estimated time: 15-20 minutes$(NC)"
	@echo ""
	@read -p "Continue? (y/N): " confirm && [ "$$confirm" = "y" ] || exit 1
	./scripts/test/run-cold-start-with-report.sh
	@echo "$(GREEN)Cold-start test with CSV report completed$(NC)"

# Docker targets
docker-build: ## Build Docker images
	@echo "$(BLUE)Building Docker images...$(NC)"
	docker compose build
	@echo "$(GREEN)Docker images built$(NC)"

docker-up: ## Start Docker Compose services (production mode)
	@echo "$(BLUE)Starting Docker Compose services (production mode)...$(NC)"
	docker compose -f $(DOCKER_COMPOSE_FILE) up -d
	@echo "$(GREEN)Docker Compose services started$(NC)"

docker-up-prod: ## Start Docker Compose services with production profile
	@echo "$(BLUE)Starting Docker Compose services (production profile)...$(NC)"
	docker compose -f $(DOCKER_COMPOSE_FILE) --profile prod up -d
	@echo "$(GREEN)Docker Compose production services started$(NC)"

docker-up-test: ## Start Docker Compose services with test profile
	@echo "$(BLUE)Starting Docker Compose services (test profile)...$(NC)"
	docker compose -f $(DOCKER_COMPOSE_FILE) --profile test up -d
	@echo "$(GREEN)Docker Compose test services started$(NC)"

docker-down: ## Stop Docker Compose services
	@echo "$(BLUE)Stopping Docker Compose services...$(NC)"
	docker compose -f $(DOCKER_COMPOSE_FILE) down
	@echo "$(GREEN)Docker Compose services stopped$(NC)"

docker-down-prod: ## Stop Docker Compose services with production profile
	@echo "$(BLUE)Stopping Docker Compose production services...$(NC)"
	docker compose -f $(DOCKER_COMPOSE_FILE) --profile prod down
	@echo "$(GREEN)Docker Compose production services stopped$(NC)"

docker-down-test: ## Stop Docker Compose services with test profile
	@echo "$(BLUE)Stopping Docker Compose test services...$(NC)"
	docker compose -f $(DOCKER_COMPOSE_FILE) --profile test down
	@echo "$(GREEN)Docker Compose test services stopped$(NC)"

docker-logs: ## Show Docker Compose logs
	@echo "$(BLUE)Showing Docker Compose logs...$(NC)"
	docker compose -f $(DOCKER_COMPOSE_FILE) logs -f

docker-logs-prod: ## Show Docker Compose logs for production services
	@echo "$(BLUE)Showing Docker Compose production logs...$(NC)"
	docker compose -f $(DOCKER_COMPOSE_FILE) --profile prod logs -f

docker-logs-test: ## Show Docker Compose logs for test services
	@echo "$(BLUE)Showing Docker Compose test logs...$(NC)"
	docker compose -f $(DOCKER_COMPOSE_FILE) --profile test logs -f

docker-clean: ## Clean up Docker resources
	@echo "$(BLUE)Cleaning up Docker resources...$(NC)"
	docker compose -f $(DOCKER_COMPOSE_FILE) down -v --remove-orphans
	docker system prune -f
	@echo "$(GREEN)Docker cleanup completed$(NC)"

# Service management
setup-services: docker-up wait-services ## Start services and wait for them to be ready

setup-test-services: docker-up-test wait-test-services ## Start test services and wait for them to be ready

wait-services: ## Wait for services to be healthy
	@echo "$(BLUE)Waiting for services to be healthy...$(NC)"
	./scripts/wait-for-services.sh

wait-test-services: ## Wait for test services to be healthy
	@echo "$(BLUE)Waiting for test services to be healthy...$(NC)"
	./scripts/dev/wait-for-services.sh

# Development targets
dev: setup-services ## Start development environment
	@echo "$(BLUE)Starting development environment...$(NC)"
	@echo "$(GREEN)Development environment ready!$(NC)"
	@echo "$(YELLOW)Services available at:$(NC)"
	@echo "  - Scheduler API: http://localhost:8080"
	@echo "  - MinIO: http://localhost:9000"
	@echo "  - MinIO Console: http://localhost:9001"

# Cleanup targets
clean: ## Clean build artifacts
	@echo "$(BLUE)Cleaning build artifacts...$(NC)"
	rm -rf bin/
	go clean
	@echo "$(GREEN)Cleanup completed$(NC)"

clean-all: clean docker-clean ## Clean everything including Docker resources

# Linting and formatting
lint: ## Run linter
	@echo "$(BLUE)Running linter...$(NC)"
	golangci-lint run
	@echo "$(GREEN)Linting completed$(NC)"

fmt: ## Format Go code
	@echo "$(BLUE)Formatting Go code...$(NC)"
	go fmt ./...
	@echo "$(GREEN)Formatting completed$(NC)"

# Database targets
db-schema: ## Apply database schema
	@echo "$(BLUE)Applying database schema...$(NC)"
	@echo "$(GREEN)Database schema applied$(NC)"

db-reset: ## Reset database
	@echo "$(BLUE)Resetting database...$(NC)"
	docker compose exec postgres psql -U user -d airavata -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
	@echo "$(GREEN)Database reset completed$(NC)"

# SpiceDB targets
spicedb-schema: ## Upload SpiceDB authorization schema
	@echo "$(BLUE)Uploading SpiceDB schema...$(NC)"
	@docker run --rm --network host \
		-v $(PWD)/db/spicedb_schema.zed:/schema.zed \
		authzed/zed:latest schema write \
		--endpoint localhost:50052 \
		--token "somerandomkeyhere" \
		--insecure \
		/schema.zed
	@echo "$(GREEN)SpiceDB schema uploaded$(NC)"

spicedb-schema-upload: ## Upload SpiceDB authorization schema (alias for spicedb-schema)
	@echo "$(BLUE)Uploading SpiceDB schema...$(NC)"
	@docker run --rm --network host \
		-v $(PWD)/db/spicedb_schema.zed:/schema.zed \
		authzed/zed:latest schema write \
		--endpoint localhost:50052 \
		--token "somerandomkeyhere" \
		--insecure \
		/schema.zed
	@echo "$(GREEN)SpiceDB schema uploaded$(NC)"

spicedb-validate: ## Validate SpiceDB schema
	@echo "$(BLUE)Validating SpiceDB schema...$(NC)"
	@docker run --rm -v $(PWD)/db/spicedb_schema.zed:/schema.zed \
		authzed/zed:latest validate /schema.zed
	@echo "$(GREEN)SpiceDB schema is valid$(NC)"

# Documentation targets
docs: ## Generate documentation
	@echo "$(BLUE)Generating documentation...$(NC)"
	godoc -http=:6060 &
	@echo "$(GREEN)Documentation available at http://localhost:6060$(NC)"

# CI/CD targets
ci-test: setup-test-services ## Run tests for CI/CD
	@echo "$(BLUE)Running CI/CD tests...$(NC)"
	go test -v -race -coverprofile=coverage.out ./...
	go tool cover -html=coverage.out -o coverage.html
	@echo "$(GREEN)CI/CD tests completed$(NC)"

# Monitoring targets
monitor: ## Start monitoring services
	@echo "$(BLUE)Starting monitoring services...$(NC)"
	# Add monitoring setup here
	@echo "$(GREEN)Monitoring services started$(NC)"

# Security targets
security-scan: ## Run security scan
	@echo "$(BLUE)Running security scan...$(NC)"
	gosec ./...
	@echo "$(GREEN)Security scan completed$(NC)"

# Backup targets
backup: ## Backup data
	@echo "$(BLUE)Backing up data...$(NC)"
	docker compose exec postgres pg_dump -U user airavata > backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "$(GREEN)Backup completed$(NC)"

# Restore targets
restore: ## Restore data from backup
	@echo "$(BLUE)Restoring data...$(NC)"
	@read -p "Enter backup file path: " backup_file; \
	docker compose exec -T postgres psql -U user -d airavata < $$backup_file
	@echo "$(GREEN)Restore completed$(NC)"

# Health check targets
health: ## Check service health
	@echo "$(BLUE)Checking service health...$(NC)"
	@curl -s http://localhost:8080/health || echo "$(RED)Scheduler API not responding$(NC)"
	@curl -s http://localhost:9000/minio/health/live || echo "$(RED)MinIO not responding$(NC)"
	@echo "$(GREEN)Health check completed$(NC)"

# Version targets
version: ## Show version information
	@echo "$(BLUE)Version Information:$(NC)"
	@echo "Go version: $(shell go version)"
	@echo "Docker version: $(shell docker --version)"
	@echo "Docker Compose version: $(shell docker compose version)"
	@echo "Project: $(PROJECT_NAME)"

# Proto generation
proto: ## Generate protobuf code
	@echo "$(BLUE)Generating protobuf code...$(NC)"
	mkdir -p core/dto
	protoc --go_out=core/dto --go-grpc_out=core/dto \
		--go_opt=paths=source_relative \
		--go-grpc_opt=paths=source_relative \
		--proto_path=proto \
		proto/*.proto
	@echo "$(GREEN)Protobuf code generated in core/dto/$(NC)"

proto-check: ## Check if protobuf files exist, generate if missing
	@if [ ! -f core/dto/worker.pb.go ]; then \
		echo "$(YELLOW)Protobuf files missing, generating...$(NC)"; \
		make proto; \
	else \
		echo "$(GREEN)Protobuf files present$(NC)"; \
	fi

# Install dependencies
install-deps: ## Install Go dependencies
	@echo "$(BLUE)Installing Go dependencies...$(NC)"
	go mod download
	go mod tidy
	@echo "$(GREEN)Dependencies installed$(NC)"

# Update dependencies
update-deps: ## Update Go dependencies
	@echo "$(BLUE)Updating Go dependencies...$(NC)"
	go get -u ./...
	go mod tidy
	@echo "$(GREEN)Dependencies updated$(NC)"