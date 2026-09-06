#!/bin/bash

# wait-for-services.sh - Wait for Docker Compose services to be healthy

set -e

COMPOSE_FILE="docker-compose.yml"
PROJECT_NAME="airavata-test"
TIMEOUT=300  # 5 minutes

echo "Waiting for services to be healthy..."

# Function to check if a service is healthy
check_service_health() {
    local service_name=$1
    
    # Check if container is running using docker compose
    if ! docker compose ps -q $service_name | grep -q .; then
        return 1
    fi
    
    # Check health status if available
    local health_status=$(docker compose ps --format "table {{.Name}}\t{{.Status}}" | grep $service_name | awk '{print $2}' || echo "no-health-check")
    
    if [[ "$health_status" == *"healthy"* ]] || [[ "$health_status" == *"Up"* ]]; then
        return 0
    elif [ "$health_status" = "no-health-check" ]; then
        # If no health check, assume healthy if running
        return 0
    else
        return 1
    fi
}

# Function to wait for a service
wait_for_service() {
    local service_name=$1
    local start_time=$(date +%s)
    
    echo "Waiting for ${service_name} to be healthy..."
    
    while [ $(($(date +%s) - start_time)) -lt $TIMEOUT ]; do
        if check_service_health "$service_name"; then
            echo "✓ ${service_name} is healthy"
            return 0
        fi
        
        echo "  ${service_name} not ready yet, waiting..."
        sleep 2
    done
    
    echo "✗ Timeout waiting for ${service_name} to be healthy"
    return 1
}

# List of services to wait for
services=(
    "postgres"
    "spicedb"
    "spicedb-postgres"
    "openbao"
    "minio"
    "sftp"
    "nfs-server"
    "slurm-cluster-01"
    "slurm-cluster-02"
    "baremetal-node-1"
    "baremetal-node-2"
)

# Wait for each service
for service in "${services[@]}"; do
    wait_for_service "$service"
done

echo "All services are healthy!"
