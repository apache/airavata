#!/bin/bash

# Complete cold-start setup from fresh clone
# This script sets up the entire environment from scratch

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[SETUP]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}[COLD-START]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to get docker compose command
get_docker_compose_cmd() {
    if command_exists docker && docker compose version >/dev/null 2>&1; then
        echo "docker compose"
    elif command_exists docker-compose; then
        echo "docker-compose"
    else
        print_error "Neither 'docker compose' nor 'docker-compose' is available"
        exit 1
    fi
}

# Main setup function
main() {
    print_header "Starting cold-start setup..."
    echo
    
    # Step 1: Validate prerequisites
    print_header "Step 1: Validating prerequisites..."
    if ! ./scripts/validate-cold-start.sh; then
        print_error "Prerequisites validation failed"
        exit 1
    fi
    echo
    
    # Step 2: Download Go dependencies
    print_header "Step 2: Downloading Go dependencies..."
    print_status "Running: go mod download"
    go mod download
    print_status "Go dependencies downloaded successfully"
    echo
    
    # Step 3: Generate protobuf files
    print_header "Step 3: Generating protobuf files..."
    print_status "Running: make proto"
    make proto
    print_status "Protobuf files generated successfully"
    echo
    
    # Step 4: Generate SLURM munge key
    print_header "Step 4: Generating SLURM munge key..."
    print_status "Running: ./scripts/generate-slurm-munge-key.sh"
    ./scripts/generate-slurm-munge-key.sh
    print_status "SLURM munge key generated"
    echo
    
    # Step 4.5: Generate master SSH key fixtures
    print_header "Step 4.5: Generating master SSH key fixtures..."
    print_status "Creating tests/fixtures directory..."
    mkdir -p tests/fixtures
    
    print_status "Generating master SSH key pair..."
    # Remove existing keys if they exist to avoid interactive prompts
    rm -f tests/fixtures/master_ssh_key tests/fixtures/master_ssh_key.pub
    ssh-keygen -t rsa -b 2048 -f tests/fixtures/master_ssh_key -N "" -C "airavata-test-master"
    print_status "Master SSH key generated"
    echo
    
    # Step 5: Stop any existing services
    print_header "Step 5: Cleaning up existing services..."
    local compose_cmd=$(get_docker_compose_cmd)
    print_status "Running: $compose_cmd down -v --remove-orphans"
    $compose_cmd down -v --remove-orphans || true
    print_status "Existing services cleaned up"
    echo
    
    # Step 6: Start all services
    print_header "Step 6: Starting all services..."
    print_status "Running: $compose_cmd --profile test up -d"
    $compose_cmd --profile test up -d
    print_status "Services started successfully"
    echo
    
    # Step 7: Wait for services
    print_header "Step 7: Waiting for services to be ready..."
    print_status "Running: ./scripts/wait-for-services.sh"
    ./scripts/wait-for-services.sh
    print_status "All services are ready"
    echo
    
    # Step 8: Upload SpiceDB schema
    print_header "Step 8: Uploading SpiceDB schema..."
    print_status "Running: make spicedb-schema-upload"
    make spicedb-schema-upload
    print_status "SpiceDB schema uploaded successfully"
    echo
    
    # Step 9: Build binaries
    print_header "Step 9: Building binaries..."
    print_status "Running: make build"
    make build
    print_status "Binaries built successfully"
    echo
    
    # Step 10: Verify setup
    print_header "Step 10: Verifying setup..."
    
    # Check if binaries exist
    if [ -f "bin/scheduler" ] && [ -f "bin/worker" ] && [ -f "bin/airavata" ]; then
        print_status "All binaries built successfully"
    else
        print_error "Some binaries are missing"
        exit 1
    fi
    
    # Check if protobuf files exist
    if [ -f "core/dto/worker.pb.go" ]; then
        print_status "Protobuf files generated successfully"
    else
        print_error "Protobuf files are missing"
        exit 1
    fi
    
    # Check if services are running
    local running_services=$($compose_cmd ps --format "table {{.Name}}" | grep -c "airavata-scheduler" || true)
    if [ "$running_services" -gt 0 ]; then
        print_status "$running_services services are running"
    else
        print_error "No services are running"
        exit 1
    fi
    
    echo
    print_status "Cold-start setup completed successfully!"
    echo
    print_status "Environment ready for testing:"
    echo "  - PostgreSQL: localhost:5432"
    echo "  - SpiceDB: localhost:50052"
    echo "  - OpenBao: localhost:8200"
    echo "  - MinIO: localhost:9000"
    echo "  - SFTP: localhost:2222"
    echo "  - NFS: localhost:2049"
    echo "  - SLURM: localhost:6817"
    echo "  - Bare Metal Nodes: localhost:2223-2225"
    echo "  - Kubernetes: localhost:6444"
    echo
    print_status "Next steps:"
    echo "  - Run unit tests: make test-unit"
    echo "  - Run integration tests: make test-integration"
    echo "  - Run all tests: make cold-start-test"
    echo
}

# Run main function
main "$@"
