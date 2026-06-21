#!/bin/bash

# Cold-start validation script
# Validates that all prerequisites are available for running tests on a fresh clone

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
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

# Function to check Go version
check_go_version() {
    if ! command_exists go; then
        print_error "Go is not installed or not in PATH"
        print_error "Please install Go 1.21+ and ensure it's in your PATH"
        return 1
    fi
    
    local go_version=$(go version | grep -o 'go[0-9]\+\.[0-9]\+' | sed 's/go//')
    local major=$(echo $go_version | cut -d. -f1)
    local minor=$(echo $go_version | cut -d. -f2)
    
    if [ "$major" -lt 1 ] || ([ "$major" -eq 1 ] && [ "$minor" -lt 21 ]); then
        print_error "Go version $go_version is too old. Required: Go 1.21+"
        print_error "Please upgrade Go to version 1.21 or later"
        return 1
    fi
    
    print_status "Go version $go_version is compatible"
    return 0
}

# Function to check Docker
check_docker() {
    if ! command_exists docker; then
        print_error "Docker is not installed or not in PATH"
        print_error "Please install Docker and ensure it's in your PATH"
        return 1
    fi
    
    # Check if Docker daemon is running
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker daemon is not running"
        print_error "Please start Docker and ensure it's accessible"
        return 1
    fi
    
    print_status "Docker is installed and running"
    return 0
}

# Function to check Docker Compose
check_docker_compose() {
    # Check for docker compose (newer) or docker-compose (older)
    if command_exists docker && docker compose version >/dev/null 2>&1; then
        print_status "Docker Compose (docker compose) is available"
        return 0
    elif command_exists docker-compose; then
        print_status "Docker Compose (docker-compose) is available"
        return 0
    else
        print_error "Docker Compose is not available"
        print_error "Please install Docker Compose (either 'docker compose' or 'docker-compose')"
        return 1
    fi
}

# Function to check for leftover containers
check_clean_environment() {
    local project_name="airavata-scheduler"
    
    # Check for running containers from this project
    if docker ps --format "table {{.Names}}" | grep -q "$project_name"; then
        print_warning "Found running containers from previous runs:"
        docker ps --format "table {{.Names}}\t{{.Status}}" | grep "$project_name"
        print_warning "Consider running 'docker compose down' to clean up"
    else
        print_status "No leftover containers found"
    fi
    
    # Check for volumes
    if docker volume ls --format "{{.Name}}" | grep -q "$project_name"; then
        print_warning "Found volumes from previous runs:"
        docker volume ls --format "table {{.Name}}" | grep "$project_name"
        print_warning "Consider running 'docker compose down -v' to clean up volumes"
    else
        print_status "No leftover volumes found"
    fi
}

# Function to check required files
check_required_files() {
    local required_files=(
        "go.mod"
        "go.sum"
        "docker-compose.yml"
        "Makefile"
        "db/spicedb_schema.zed"
        "proto/worker.proto"
    )
    
    for file in "${required_files[@]}"; do
        if [ ! -f "$file" ]; then
            print_error "Required file missing: $file"
            print_error "This doesn't appear to be a complete clone of the repository"
            return 1
        fi
    done
    
    print_status "All required files are present"
    return 0
}

# Function to check network ports
check_network_ports() {
    local required_ports=(5432 50052 8200 9000 2222 2049 6817 2223 2224 2225 6444)
    local occupied_ports=()
    
    for port in "${required_ports[@]}"; do
        if lsof -i :$port >/dev/null 2>&1; then
            occupied_ports+=($port)
        fi
    done
    
    if [ ${#occupied_ports[@]} -gt 0 ]; then
        print_warning "The following ports are already in use:"
        for port in "${occupied_ports[@]}"; do
            echo "  - Port $port"
        done
        print_warning "This may cause conflicts when starting services"
        print_warning "Consider stopping services using these ports"
    else
        print_status "All required ports are available"
    fi
}

# Main validation function
main() {
    print_header "Validating cold-start prerequisites..."
    echo
    
    local errors=0
    
    # Check Go
    print_header "Checking Go installation..."
    if ! check_go_version; then
        errors=$((errors + 1))
    fi
    echo
    
    # Check Docker
    print_header "Checking Docker installation..."
    if ! check_docker; then
        errors=$((errors + 1))
    fi
    echo
    
    # Check Docker Compose
    print_header "Checking Docker Compose..."
    if ! check_docker_compose; then
        errors=$((errors + 1))
    fi
    echo
    
    # Check required files
    print_header "Checking required files..."
    if ! check_required_files; then
        errors=$((errors + 1))
    fi
    echo
    
    # Check environment cleanliness
    print_header "Checking environment cleanliness..."
    check_clean_environment
    echo
    
    # Check network ports
    print_header "Checking network ports..."
    check_network_ports
    echo
    
    # Summary
    if [ $errors -eq 0 ]; then
        print_status "All prerequisites validated successfully!"
        print_status "Ready for cold-start testing"
        echo
        print_status "Next steps:"
        echo "  1. Run: ./scripts/setup-cold-start.sh"
        echo "  2. Run: make cold-start-test"
        return 0
    else
        print_error "Validation failed with $errors error(s)"
        print_error "Please fix the issues above before proceeding"
        return 1
    fi
}

# Run main function
main "$@"
