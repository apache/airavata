#!/bin/bash
# Start all required test services

set -e

echo "Starting test services..."

# Start PostgreSQL first
echo "Starting PostgreSQL..."
docker compose up -d postgres
echo "Waiting for PostgreSQL to be ready..."
sleep 5

# Check if PostgreSQL is ready
until docker compose exec postgres pg_isready -U user; do
  echo "Waiting for PostgreSQL to be ready..."
  sleep 2
done

echo "PostgreSQL is ready!"

# For integration tests, start additional services
if [ "$1" == "integration" ]; then
    echo "Starting integration test services..."
    docker compose up -d minio sftp nfs-server slurm-cluster-01 slurm-node-01-01 slurm-cluster-02 slurm-node-02-01 spicedb spicedb-postgres openbao
    echo "Waiting for all services to be ready..."
    sleep 10
    
    # Check service health
    echo "Checking service health..."
    docker compose ps
fi

echo "Test services started successfully!"
