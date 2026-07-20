#!/bin/bash

# Wait for services to be healthy
# This script waits for all required services to be available before running tests

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# Function to check if a service is healthy
check_service_health() {
    local service_name=$1
    local max_attempts=${2:-30}
    local attempt=1
    
    print_status "Checking health of $service_name..."
    
    while [ $attempt -le $max_attempts ]; do
        if docker compose ps -q $service_name | xargs docker inspect --format='{{.State.Health.Status}}' 2>/dev/null | grep -q "healthy"; then
            print_status "$service_name is healthy"
            return 0
        fi
        
        if [ $attempt -eq 1 ]; then
            print_warning "$service_name is not yet healthy, waiting..."
        fi
        
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "$service_name failed to become healthy after $max_attempts attempts"
    return 1
}

# Function to check if a port is open
check_port() {
    local host=$1
    local port=$2
    local max_attempts=${3:-30}
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if nc -z $host $port 2>/dev/null; then
            return 0
        fi
        
        sleep 2
        attempt=$((attempt + 1))
    done
    
    return 1
}

# Function to check Kubernetes cluster
check_kubernetes() {
    print_status "Checking Kubernetes cluster availability..."
    
    if ! command -v kubectl &> /dev/null; then
        print_warning "kubectl not found, skipping Kubernetes checks"
        return 0
    fi
    
    if ! kubectl cluster-info &> /dev/null; then
        print_warning "Kubernetes cluster not accessible, skipping Kubernetes tests"
        return 0
    fi
    
    print_status "Kubernetes cluster is accessible"
    return 0
}

# Main function
main() {
    print_status "Starting service health checks..."
    
    # Check if Docker Compose is running
    if ! docker compose ps | grep -q "Up"; then
        print_error "Docker Compose services are not running. Please run 'docker compose up -d' first."
        exit 1
    fi
    
    # Check required services
    services=("postgres" "spicedb" "spicedb-postgres" "openbao" "minio" "sftp" "nfs-server" "slurm-cluster-01" "slurm-cluster-02" "baremetal-node-1" "baremetal-node-2")
    
    for service in "${services[@]}"; do
        if ! check_service_health $service; then
            print_error "Service $service is not healthy. Please check the logs with 'docker compose logs $service'"
            exit 1
        fi
    done
    
    # Check specific ports
    print_status "Checking service ports..."
    
    # PostgreSQL
    if ! check_port localhost 5432; then
        print_error "PostgreSQL port 5432 is not accessible"
        exit 1
    fi
    
    # SpiceDB
    if ! check_port localhost 50052; then
        print_error "SpiceDB port 50052 is not accessible"
        exit 1
    fi
    
    # OpenBao
    if ! check_port localhost 8200; then
        print_error "OpenBao port 8200 is not accessible"
        exit 1
    fi
    
    # MinIO
    if ! check_port localhost 9000; then
        print_error "MinIO port 9000 is not accessible"
        exit 1
    fi
    
    # SFTP
    if ! check_port localhost 2222; then
        print_error "SFTP port 2222 is not accessible"
        exit 1
    fi
    
    # NFS
    if ! check_port localhost 2049; then
        print_error "NFS port 2049 is not accessible"
        exit 1
    fi
    
    # SLURM Cluster 1
    if ! check_port localhost 6817; then
        print_error "SLURM Cluster 1 port 6817 is not accessible"
        exit 1
    fi
    
    # SLURM Cluster 2
    if ! check_port localhost 6819; then
        print_error "SLURM Cluster 2 port 6819 is not accessible"
        exit 1
    fi
    
    # Bare Metal Nodes
    if ! check_port localhost 2223; then
        print_error "Bare Metal Node 1 port 2223 is not accessible"
        exit 1
    fi
    
    if ! check_port localhost 2225; then
        print_error "Bare Metal Node 2 port 2225 is not accessible"
        exit 1
    fi
    
    
    # Check Kubernetes (optional)
    check_kubernetes
    
    print_status "All services are healthy and ready for testing!"
    
    # Display service information
    echo
    print_status "Service endpoints:"
    echo "  PostgreSQL: localhost:5432 (user:password)"
    echo "  SpiceDB:    localhost:50052"
    echo "  OpenBao:    localhost:8200"
    echo "  MinIO:      http://localhost:9000 (minioadmin:minioadmin)"
    echo "  SFTP:       localhost:2222 (testuser:testpass)"
    echo "  NFS:        localhost:2049 (/nfsshare)"
    echo "  SLURM:      localhost:6817, localhost:6819"
    echo "  Bare Metal: localhost:2223, localhost:2225 (testuser:testpass)"
    echo
}

# Run main function
main "$@"
