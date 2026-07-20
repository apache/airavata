#!/bin/bash
set -e

echo "Starting test environment..."
docker compose -f docker-compose.yml --profile test up -d

echo "Waiting for services to be healthy..."
sleep 10

echo "Checking service health..."
docker compose -f docker-compose.yml --profile test ps

echo "Setting up toxiproxy proxies..."
# Wait for toxiproxy to be ready
sleep 5

# Create proxies for failure injection
echo "Creating SFTP proxy..."
curl -X POST http://localhost:8474/proxies \
  -H "Content-Type: application/json" \
  -d '{"name":"sftp","listen":"0.0.0.0:20000","upstream":"central-storage:22"}' || echo "SFTP proxy creation failed"

echo "Creating MinIO proxy..."
curl -X POST http://localhost:8474/proxies \
  -H "Content-Type: application/json" \
  -d '{"name":"minio","listen":"0.0.0.0:20001","upstream":"minio:9000"}' || echo "MinIO proxy creation failed"

echo "Creating PostgreSQL proxy..."
curl -X POST http://localhost:8474/proxies \
  -H "Content-Type: application/json" \
  -d '{"name":"postgres","listen":"0.0.0.0:20002","upstream":"postgres:5432"}' || echo "PostgreSQL proxy creation failed"

echo "Creating Redis proxy..."
curl -X POST http://localhost:8474/proxies \
  -H "Content-Type: application/json" \
  -d '{"name":"redis","listen":"0.0.0.0:20003","upstream":"redis:6379"}' || echo "Redis proxy creation failed"

echo "Setting up MinIO bucket..."
# Create test bucket in MinIO
docker exec airavata-scheduler-minio-1 mc alias set myminio http://localhost:9000 minioadmin minioadmin123 || echo "MinIO alias setup failed"
docker exec airavata-scheduler-minio-1 mc mb myminio/test-bucket || echo "Bucket creation failed (may already exist)"

echo "Test environment ready!"
echo "Services available:"
echo "  - PostgreSQL: localhost:5433"
echo "  - MinIO: localhost:9000"
echo "  - SFTP: localhost:2200"
echo "  - Redis: localhost:6379"
echo "  - SLURM Mock: localhost:6817"
echo "  - Toxiproxy: localhost:8474"
echo "  - MockServer: localhost:1080"
echo "  - API Server: localhost:8080"
