#!/bin/bash
set -e

echo "🚀 Starting Airavata Scheduler Full Test Environment"
echo "=================================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if docker compose is available
if ! command -v docker > /dev/null 2>&1 || ! docker compose version > /dev/null 2>&1; then
    echo "❌ docker compose is not available. Please install Docker with Compose support."
    exit 1
fi

# Use docker compose (v2)
COMPOSE_CMD="docker compose"

echo "📦 Building SLURM Docker image..."
$COMPOSE_CMD build slurm-controller

echo "🔄 Starting all services..."
$COMPOSE_CMD up -d

echo "⏳ Waiting for services to be ready..."

# Wait for PostgreSQL
echo "  - PostgreSQL..."
until $COMPOSE_CMD exec postgres pg_isready -U user > /dev/null 2>&1; do
    sleep 2
done

# Wait for SpiceDB
echo "  - SpiceDB..."
until $COMPOSE_CMD exec spicedb grpc_health_probe -addr=localhost:50051 > /dev/null 2>&1; do
    sleep 2
done

# Initialize SpiceDB schema
echo "  - Initializing SpiceDB schema..."
$COMPOSE_CMD exec spicedb /init-spicedb.sh

# Wait for OpenBao
echo "  - OpenBao..."
until $COMPOSE_CMD exec openbao vault status > /dev/null 2>&1; do
    sleep 2
done

# Wait for MinIO
echo "  - MinIO..."
until curl -f http://localhost:9000/minio/health/live > /dev/null 2>&1; do
    sleep 2
done

# Wait for SLURM controller
echo "  - SLURM Controller..."
until $COMPOSE_CMD exec slurm-controller scontrol ping > /dev/null 2>&1; do
    sleep 2
done

# Wait for SLURM nodes
echo "  - SLURM Compute Nodes..."
for node in slurm-node-1 slurm-node-2 slurm-node-3; do
    echo "    - $node..."
    until $COMPOSE_CMD exec $node scontrol ping > /dev/null 2>&1; do
        sleep 2
    done
done

# Wait for bare metal nodes
echo "  - Bare Metal Nodes..."
for node in baremetal-node-1 baremetal-node-2 baremetal-node-3; do
    echo "    - $node..."
    until $COMPOSE_CMD exec $node nc -z localhost 2222 > /dev/null 2>&1; do
        sleep 2
    done
done

# Wait for Kubernetes cluster
echo "  - Kubernetes Cluster..."
until $COMPOSE_CMD exec kind-cluster kubectl get nodes --no-headers | grep Ready | wc -l | grep -q 3; do
    sleep 5
done

echo ""
echo "✅ All services are ready!"
echo ""
echo "📊 Service Status:"
echo "=================="
echo "PostgreSQL:     localhost:5432"
echo "Scheduler:      localhost:8080 (HTTP), localhost:50051 (gRPC)"
echo "SpiceDB:        localhost:50052"
echo "OpenBao:        localhost:8200"
echo "MinIO:          localhost:9000 (API), localhost:9001 (Console)"
echo "SFTP:           localhost:2222"
echo "NFS:            localhost:2049"
echo "SLURM:          localhost:6817 (Controller)"
echo "Bare Metal 1:   localhost:2223"
echo "Bare Metal 2:   localhost:2224"
echo "Bare Metal 3:   localhost:2225"
echo "Kubernetes:     localhost:6443"
echo "Redis:          localhost:6379"
echo ""
echo "🧪 Running tests..."
echo "=================="

# Run a quick connectivity test
echo "Testing service connectivity..."

# Test PostgreSQL
if $COMPOSE_CMD exec postgres pg_isready -U user > /dev/null 2>&1; then
    echo "✅ PostgreSQL: Ready"
else
    echo "❌ PostgreSQL: Not ready"
fi

# Test SpiceDB
if $COMPOSE_CMD exec spicedb grpc_health_probe -addr=localhost:50051 > /dev/null 2>&1; then
    echo "✅ SpiceDB: Ready"
else
    echo "❌ SpiceDB: Not ready"
fi

# Test SLURM
if $COMPOSE_CMD exec slurm-controller scontrol ping > /dev/null 2>&1; then
    echo "✅ SLURM: Ready"
    echo "   Nodes: $($COMPOSE_CMD exec slurm-controller scontrol show nodes | grep NodeName | wc -l)"
else
    echo "❌ SLURM: Not ready"
fi

# Test Kubernetes
if $COMPOSE_CMD exec kind-cluster kubectl get nodes --no-headers | grep Ready | wc -l | grep -q 3; then
    echo "✅ Kubernetes: Ready (3 nodes)"
else
    echo "❌ Kubernetes: Not ready"
fi

echo ""
echo "🎉 Full test environment is ready!"
echo ""
echo "To run tests:"
echo "  go test ./tests/integration/... -v"
echo ""
echo "To stop all services:"
echo "  $COMPOSE_CMD down"
echo ""
echo "To view logs:"
echo "  $COMPOSE_CMD logs -f [service-name]"
