#!/bin/bash

# validate-test-environment.sh
# Validates that all required services are healthy and functional before running tests

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}[INFO]${NC} Validating test environment..."

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check required tools
echo -e "${BLUE}[INFO]${NC} Checking required tools..."
required_tools=("docker" "curl" "sshpass" "grpcurl")
for tool in "${required_tools[@]}"; do
    if ! command_exists "$tool"; then
        echo -e "${RED}[ERROR]${NC} Required tool '$tool' is not installed"
        exit 1
    fi
done
echo -e "${GREEN}[INFO]${NC} All required tools are available"

# Check Docker services are running
echo -e "${BLUE}[INFO]${NC} Checking Docker services..."
required_services=(
    "airavata-scheduler-postgres-1"
    "airavata-scheduler-spicedb-1"
    "airavata-scheduler-openbao-1"
    "airavata-scheduler-minio-1"
    "airavata-scheduler-sftp-1"
    "airavata-scheduler-nfs-server-1"
    "airavata-scheduler-slurm-controller-1"
    "airavata-scheduler-slurm-node-1-1"
    "airavata-scheduler-slurm-node-2-1"
    "airavata-scheduler-slurm-node-3-1"
    "airavata-scheduler-baremetal-node-1-1"
    "airavata-scheduler-baremetal-node-2-1"
    "airavata-scheduler-baremetal-node-3-1"
    "airavata-scheduler-redis-1"
)

for service in "${required_services[@]}"; do
    if ! docker ps --format "table {{.Names}}" | grep -q "^${service}$"; then
        echo -e "${RED}[ERROR]${NC} Service '$service' is not running"
        exit 1
    fi
done
echo -e "${GREEN}[INFO]${NC} All Docker services are running"

# Check service health
echo -e "${BLUE}[INFO]${NC} Checking service health..."

# PostgreSQL
echo -e "${BLUE}[INFO]${NC} Checking PostgreSQL..."
if ! docker exec airavata-scheduler-postgres-1 pg_isready -U user -d airavata >/dev/null 2>&1; then
    echo -e "${RED}[ERROR]${NC} PostgreSQL is not ready"
    exit 1
fi
echo -e "${GREEN}[INFO]${NC} PostgreSQL is healthy"

# SpiceDB
echo -e "${BLUE}[INFO]${NC} Checking SpiceDB..."
if ! grpcurl -plaintext -H "authorization: Bearer somerandomkeyhere" localhost:50052 list >/dev/null 2>&1; then
    echo -e "${RED}[ERROR]${NC} SpiceDB is not accessible"
    exit 1
fi
echo -e "${GREEN}[INFO]${NC} SpiceDB is healthy"

# OpenBao
echo -e "${BLUE}[INFO]${NC} Checking OpenBao..."
if ! curl -s -H "X-Vault-Token: dev-token" http://localhost:8200/v1/sys/health >/dev/null 2>&1; then
    echo -e "${RED}[ERROR]${NC} OpenBao is not accessible"
    exit 1
fi
echo -e "${GREEN}[INFO]${NC} OpenBao is healthy"

# MinIO
echo -e "${BLUE}[INFO]${NC} Checking MinIO..."
if ! curl -s -f http://localhost:9000/minio/health/live >/dev/null 2>&1; then
    echo -e "${RED}[ERROR]${NC} MinIO is not accessible"
    exit 1
fi
echo -e "${GREEN}[INFO]${NC} MinIO is healthy"

# SFTP
echo -e "${BLUE}[INFO]${NC} Checking SFTP..."
if ! nc -z localhost 2222 >/dev/null 2>&1; then
    echo -e "${RED}[ERROR]${NC} SFTP server is not accessible"
    exit 1
fi
echo -e "${GREEN}[INFO]${NC} SFTP is healthy"

# SLURM
echo -e "${BLUE}[INFO]${NC} Checking SLURM cluster..."
if ! docker exec airavata-scheduler-slurm-controller-1 sinfo >/dev/null 2>&1; then
    echo -e "${RED}[ERROR]${NC} SLURM cluster is not functional"
    exit 1
fi
echo -e "${GREEN}[INFO]${NC} SLURM cluster is healthy"

# Kubernetes
echo -e "${BLUE}[INFO]${NC} Checking Kubernetes cluster..."
if ! kubectl get nodes >/dev/null 2>&1; then
    echo -e "${RED}[ERROR]${NC} Kubernetes cluster is not accessible via kubectl"
    exit 1
fi
echo -e "${GREEN}[INFO]${NC} Kubernetes cluster is accessible"

# Redis
echo -e "${BLUE}[INFO]${NC} Checking Redis..."
if ! docker exec airavata-scheduler-redis-1 redis-cli ping >/dev/null 2>&1; then
    echo -e "${RED}[ERROR]${NC} Redis is not accessible"
    exit 1
fi
echo -e "${GREEN}[INFO]${NC} Redis is healthy"

# Verify SpiceDB schema is loaded
echo -e "${BLUE}[INFO]${NC} Verifying SpiceDB schema..."
if ! grpcurl -plaintext -H "authorization: Bearer somerandomkeyhere" localhost:50052 list | grep -q "authzed.api.v1.PermissionsService"; then
    echo -e "${RED}[ERROR]${NC} SpiceDB schema is not loaded"
    exit 1
fi
echo -e "${GREEN}[INFO]${NC} SpiceDB schema is loaded"

# Verify SLURM cluster has nodes
echo -e "${BLUE}[INFO]${NC} Verifying SLURM cluster nodes..."
if ! docker exec airavata-scheduler-slurm-controller-1 sinfo | grep -q "PARTITION"; then
    echo -e "${RED}[ERROR]${NC} SLURM cluster has no nodes available"
    exit 1
fi
echo -e "${GREEN}[INFO]${NC} SLURM cluster has nodes available"

# Verify Kubernetes nodes are ready
echo -e "${BLUE}[INFO]${NC} Verifying Kubernetes cluster has ready nodes..."
ready_nodes=$(kubectl get nodes --no-headers | grep -c "Ready")
if [ "$ready_nodes" -eq 0 ]; then
    echo -e "${RED}[ERROR]${NC} No Ready nodes in Kubernetes cluster"
    exit 1
fi
echo -e "${GREEN}[INFO]${NC} Kubernetes cluster has $ready_nodes Ready nodes"

echo -e "${GREEN}[INFO]${NC} All services are healthy and functional!"
echo -e "${GREEN}[INFO]${NC} Test environment validation completed successfully"

# Display service endpoints
echo -e "${BLUE}[INFO]${NC} Service endpoints:"
echo "  PostgreSQL: localhost:5432 (user:password)"
echo "  SpiceDB:    localhost:50052"
echo "  OpenBao:    localhost:8200"
echo "  MinIO:      http://localhost:9000 (minioadmin:minioadmin)"
echo "  SFTP:       localhost:2222 (testuser:testpass)"
echo "  NFS:        localhost:2049 (/nfsshare)"
echo "  SLURM:      localhost:6817"
echo "  Bare Metal: localhost:2223-2225 (testuser:testpass)"
echo "  Kubernetes: kubectl (Docker Desktop cluster)"
echo "  Redis:      localhost:6379"
