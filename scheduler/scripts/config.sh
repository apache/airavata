#!/bin/bash
# Airavata Scheduler Scripts Configuration
# This file contains all centralized configuration for scripts
# Environment variables override these defaults

# Service endpoints
export POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
export POSTGRES_PORT="${POSTGRES_PORT:-5432}"
export POSTGRES_USER="${POSTGRES_USER:-user}"
export POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-password}"
export POSTGRES_DB="${POSTGRES_DB:-airavata}"

export SPICEDB_HOST="${SPICEDB_HOST:-localhost}"
export SPICEDB_PORT="${SPICEDB_PORT:-50052}"
export SPICEDB_TOKEN="${SPICEDB_TOKEN:-somerandomkeyhere}"

export VAULT_HOST="${VAULT_HOST:-localhost}"
export VAULT_PORT="${VAULT_PORT:-8200}"
export VAULT_TOKEN="${VAULT_TOKEN:-dev-token}"

export MINIO_HOST="${MINIO_HOST:-localhost}"
export MINIO_PORT="${MINIO_PORT:-9000}"
export MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-minioadmin}"
export MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-minioadmin}"

# Compute resource ports
export SLURM_CLUSTER1_SSH_PORT="${SLURM_CLUSTER1_SSH_PORT:-2223}"
export SLURM_CLUSTER1_SLURM_PORT="${SLURM_CLUSTER1_SLURM_PORT:-6817}"
export SLURM_CLUSTER2_SSH_PORT="${SLURM_CLUSTER2_SSH_PORT:-2224}"
export SLURM_CLUSTER2_SLURM_PORT="${SLURM_CLUSTER2_SLURM_PORT:-6818}"

export BAREMETAL_NODE1_PORT="${BAREMETAL_NODE1_PORT:-2225}"
export BAREMETAL_NODE2_PORT="${BAREMETAL_NODE2_PORT:-2226}"

# Storage resource ports
export SFTP_PORT="${SFTP_PORT:-2222}"
export NFS_PORT="${NFS_PORT:-2049}"

# Application ports
export SCHEDULER_HTTP_PORT="${SCHEDULER_HTTP_PORT:-8080}"
export SCHEDULER_GRPC_PORT="${SCHEDULER_GRPC_PORT:-50051}"

# Timeouts and retries
export DEFAULT_TIMEOUT="${DEFAULT_TIMEOUT:-30}"
export DEFAULT_RETRIES="${DEFAULT_RETRIES:-3}"
export HEALTH_CHECK_TIMEOUT="${HEALTH_CHECK_TIMEOUT:-60}"
export SERVICE_START_TIMEOUT="${SERVICE_START_TIMEOUT:-120}"

# Paths
export PROJECT_ROOT="${PROJECT_ROOT:-$(dirname "$(dirname "$(realpath "$0")")")}"
export LOGS_DIR="${LOGS_DIR:-$PROJECT_ROOT/logs}"
export BIN_DIR="${BIN_DIR:-$PROJECT_ROOT/bin}"
export TESTS_DIR="${TESTS_DIR:-$PROJECT_ROOT/tests}"
export FIXTURES_DIR="${FIXTURES_DIR:-$TESTS_DIR/fixtures}"

# Docker configuration
export DOCKER_COMPOSE_FILE="${DOCKER_COMPOSE_FILE:-$PROJECT_ROOT/docker-compose.yml}"
export DOCKER_NETWORK="${DOCKER_NETWORK:-airavata-scheduler_default}"

# Test configuration
export TEST_USER_NAME="${TEST_USER_NAME:-testuser}"
export TEST_USER_EMAIL="${TEST_USER_EMAIL:-test@example.com}"
export TEST_USER_PASSWORD="${TEST_USER_PASSWORD:-testpass123}"

# Kubernetes configuration
export KUBERNETES_CLUSTER_NAME="${KUBERNETES_CLUSTER_NAME:-docker-desktop}"
export KUBERNETES_CONTEXT="${KUBERNETES_CONTEXT:-docker-desktop}"
export KUBERNETES_NAMESPACE="${KUBERNETES_NAMESPACE:-default}"
export KUBECONFIG="${KUBECONFIG:-$HOME/.kube/config}"

# Helper functions
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local timeout=${4:-$DEFAULT_TIMEOUT}
    
    echo "Waiting for $service_name at $host:$port..."
    for i in $(seq 1 $timeout); do
        if nc -z "$host" "$port" 2>/dev/null; then
            echo "$service_name is ready"
            return 0
        fi
        sleep 1
    done
    
    echo "Timeout waiting for $service_name"
    return 1
}

wait_for_http() {
    local url=$1
    local service_name=$2
    local timeout=${3:-$DEFAULT_TIMEOUT}
    
    echo "Waiting for $service_name at $url..."
    for i in $(seq 1 $timeout); do
        if curl -s -f "$url" >/dev/null 2>&1; then
            echo "$service_name is ready"
            return 0
        fi
        sleep 1
    done
    
    echo "Timeout waiting for $service_name"
    return 1
}

check_service_health() {
    local service_name=$1
    local check_command=$2
    
    echo "Checking $service_name health..."
    if eval "$check_command"; then
        echo "$service_name is healthy"
        return 0
    else
        echo "$service_name is not healthy"
        return 1
    fi
}

# Export helper functions
export -f wait_for_service
export -f wait_for_http
export -f check_service_health
