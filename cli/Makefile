# Airavata CLI Makefile

# Variables
THRIFT_DIR := ../thrift-interface-descriptions
THRIFT_FILE := $(THRIFT_DIR)/airavata-service/airavata_service.thrift
GEN_GO_DIR := gen-go
BINARY_NAME := airavata
BUILD_DIR := bin

# Default target
.PHONY: all
all: generate-thrift build

# Generate Go client from Thrift definitions
.PHONY: generate-thrift
generate-thrift:
	@echo "Generating Go client from Thrift definitions..."
	@if [ ! -f "$(THRIFT_FILE)" ]; then \
		echo "Error: Thrift file not found at $(THRIFT_FILE)"; \
		echo "Please ensure you're running from the cli/ directory"; \
		exit 1; \
	fi
	@mkdir -p $(GEN_GO_DIR)
	thrift --gen go:package_prefix=github.com/apache/airavata/cli/gen-go/ \
		-r $(THRIFT_FILE)
	@echo "Thrift client generated successfully"

# Build the CLI binary
.PHONY: build
build: generate-thrift
	@echo "Building $(BINARY_NAME)..."
	@mkdir -p $(BUILD_DIR)
	go build -o $(BUILD_DIR)/$(BINARY_NAME) ./cmd/airavata
	@echo "Build completed: $(BUILD_DIR)/$(BINARY_NAME)"

# Install to GOPATH/bin
.PHONY: install
install: build
	@echo "Installing $(BINARY_NAME) to $(GOPATH)/bin..."
	@cp $(BUILD_DIR)/$(BINARY_NAME) $(GOPATH)/bin/
	@echo "Installation completed"

# Run tests
.PHONY: test
test:
	@echo "Running tests..."
	go test ./...

# Clean generated files and build artifacts
.PHONY: clean
clean:
	@echo "Cleaning generated code and build artifacts..."
	@rm -rf $(GEN_GO_DIR)
	@rm -rf $(BUILD_DIR)
	@go clean
	@echo "Clean completed"

# Format code
.PHONY: fmt
fmt:
	@echo "Formatting code..."
	go fmt ./...

# Lint code
.PHONY: lint
lint:
	@echo "Linting code..."
	@if command -v golangci-lint >/dev/null 2>&1; then \
		golangci-lint run; \
	else \
		echo "golangci-lint not found, skipping linting"; \
	fi

# Run the CLI (for testing)
.PHONY: run
run: build
	@echo "Running $(BINARY_NAME)..."
	@./$(BUILD_DIR)/$(BINARY_NAME) --help

# Development setup
.PHONY: dev-setup
dev-setup:
	@echo "Setting up development environment..."
	@go mod download
	@go mod tidy
	@echo "Development setup completed"

# Check if thrift is installed
.PHONY: check-thrift
check-thrift:
	@if ! command -v thrift >/dev/null 2>&1; then \
		echo "Error: Apache Thrift compiler not found"; \
		echo "Please install thrift: https://thrift.apache.org/download"; \
		exit 1; \
	fi
	@echo "Thrift compiler found: $$(thrift --version)"

# Help
.PHONY: help
help:
	@echo "Available targets:"
	@echo "  generate-thrift  - Generate Go client from Thrift definitions"
	@echo "  build           - Build the CLI binary"
	@echo "  install         - Install to GOPATH/bin"
	@echo "  test            - Run tests"
	@echo "  clean           - Clean generated code and build artifacts"
	@echo "  fmt             - Format code"
	@echo "  lint            - Lint code"
	@echo "  run             - Run the CLI (for testing)"
	@echo "  dev-setup       - Set up development environment"
	@echo "  check-thrift    - Check if Thrift compiler is installed"
	@echo "  help            - Show this help message"
