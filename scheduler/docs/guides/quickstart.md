# Quick Start Guide

This guide will get you up and running with the Airavata Scheduler in minutes.

## Prerequisites

- Go 1.21 or higher
- Docker and Docker Compose
- PostgreSQL 13+ (or use Docker)
- Access to compute resources (SLURM, Kubernetes, or bare metal)
- Access to storage resources (S3, NFS, or SFTP)

## 1. Build Binaries

```bash
# Clone the repository
git clone https://github.com/apache/airavata/scheduler.git
cd airavata-scheduler

# Build all binaries (scheduler, worker, CLI)
make build

# Or build individually
make build-server  # Builds bin/scheduler
make build-worker  # Builds bin/worker
make build-cli     # Builds bin/airavata
```

The CLI binary will be available at `./bin/airavata` and provides complete system management capabilities.

## 2. Start Services

### Cold Start (Recommended for Testing)

For a complete cold start from scratch (no existing containers or volumes):

```bash
# Complete cold start setup - builds everything from scratch
./scripts/setup-cold-start.sh

# This script automatically:
# 1. Validates prerequisites (Go, Docker, ports)
# 2. Downloads Go dependencies
# 3. Generates protobuf files
# 4. Creates deterministic SLURM munge key
# 5. Starts all services with test profile
# 6. Waits for services to be healthy
# 7. Uploads SpiceDB schema
# 8. Builds all binaries
```

### Manual Service Start

```bash
# Start all required services (PostgreSQL, SpiceDB, OpenBao)
docker compose up -d postgres spicedb spicedb-postgres openbao

# Wait for services to be healthy
make wait-services

# Upload SpiceDB authorization schema
make spicedb-schema-upload

# Verify services are running
curl -s http://localhost:8200/v1/sys/health | jq  # OpenBao
curl -s http://localhost:50052/healthz            # SpiceDB
```

### Test Environment (Full Stack)

For integration testing with compute and storage resources:

```bash
# Start all services including SLURM clusters, bare metal nodes, and storage
docker compose --profile test up -d

# Wait for all services to be healthy
./scripts/wait-for-services.sh

# Run integration tests
./scripts/test/run-integration-tests.sh
```

## 3. Bootstrap Application

```go
package main

import (
    "log"
    "github.com/apache/airavata/scheduler/core/app"
)

func main() {
    config := &app.Config{
        Database: struct {
            DSN string `json:"dsn"`
        }{
            DSN: "postgres://user:password@localhost:5432/airavata?sslmode=disable",
        },
        Server: struct {
            Host string `json:"host"`
            Port int    `json:"port"`
        }{
            Host: "0.0.0.0",
            Port: 8080,
        },
        Worker: struct {
            BinaryPath        string `json:"binary_path"`
            BinaryURL         string `json:"binary_url"`
            DefaultWorkingDir string `json:"default_working_dir"`
        }{
            BinaryPath:        "./build/worker",
            BinaryURL:         "http://localhost:8080/api/worker-binary",
            DefaultWorkingDir: "/tmp/worker",
        },
    }

    application, err := app.Bootstrap(config)
    if err != nil {
        log.Fatal(err)
    }

    application.Start()
}
```

## 4. Register Resources

### Register Compute Resource

```go
// Register SLURM cluster
computeReq := &domain.RegisterComputeResourceRequest{
    Name:        "SLURM Cluster",
    Type:        "slurm",
    Endpoint:    "slurm.example.com:22",
    Credentials: "credential_id",
}

response, err := resourceRegistry.RegisterComputeResource(ctx, computeReq)
```

### Register Storage Resource

```go
// Register S3 bucket
storageReq := &domain.RegisterStorageResourceRequest{
    Name:        "S3 Bucket",
    Type:        "s3",
    Endpoint:    "s3://my-bucket",
    Credentials: "credential_id",
}

response, err := resourceRegistry.RegisterStorageResource(ctx, storageReq)
```

## 5. Using the Command Line Interface

The Airavata Scheduler includes a comprehensive CLI (`airavata`) for complete system management.

### Authentication

```bash
# Login to the scheduler
./bin/airavata auth login

# Check authentication status
./bin/airavata auth status

# Set server URL if needed
./bin/airavata config set server http://localhost:8080
```

### Project Management

```bash
# Create a new project
./bin/airavata project create

# List your projects
./bin/airavata project list

# Get project details
./bin/airavata project get proj-123
```

### Resource Management

```bash
# List compute resources
./bin/airavata resource compute list

# List storage resources
./bin/airavata resource storage list

# Create new compute resource
./bin/airavata resource compute create

# Create new storage resource
./bin/airavata resource storage create

# Create credentials
./bin/airavata resource credential create

# Bind credential to resource (with verification)
./bin/airavata resource bind-credential compute-123 cred-456

# Test resource connectivity
./bin/airavata resource test compute-123
```

### Data Management

```bash
# Upload input data
./bin/airavata data upload input.dat minio-storage:/experiments/input.dat

# Upload directory
./bin/airavata data upload-dir ./data minio-storage:/experiments/data

# List files in storage
./bin/airavata data list minio-storage:/experiments/

# Download files
./bin/airavata data download minio-storage:/experiments/output.txt ./output.txt
```

### Experiment Management

```bash
# Run experiment
./bin/airavata experiment run experiment.yml --project proj-123 --compute slurm-1

# Monitor experiment
./bin/airavata experiment watch exp-456

# Check experiment status
./bin/airavata experiment status exp-456

# List all experiments
./bin/airavata experiment list

# View experiment logs
./bin/airavata experiment logs exp-456

# Cancel running experiment
./bin/airavata experiment cancel exp-456

# Retry failed tasks
./bin/airavata experiment retry exp-456 --failed-only
```

### Output Management

```bash
# List experiment outputs
./bin/airavata experiment outputs exp-456

# Download all outputs as archive
./bin/airavata experiment download exp-456 --output ./results/

# Download specific task outputs
./bin/airavata experiment download exp-456 --task task-789 --output ./task-outputs/

# Download specific file
./bin/airavata experiment download exp-456 --file task-789/output.txt --output ./output.txt
```

### Complete Workflow Example

```bash
# 1. Authenticate
./bin/airavata auth login

# 2. Create project
./bin/airavata project create

# 3. Upload input data
./bin/airavata data upload input.dat minio-storage:/experiments/input.dat

# 4. Run experiment
./bin/airavata experiment run experiment.yml --project proj-123 --compute slurm-1

# 5. Monitor experiment
./bin/airavata experiment watch exp-456

# 6. Check outputs
./bin/airavata experiment outputs exp-456

# 7. Download results
./bin/airavata experiment download exp-456 --output ./results/
```

For complete CLI documentation, see [CLI Reference](../reference/cli.md).

## 6. Run Your First Experiment

### Using the CLI (Recommended)

```bash
# Run experiment from YAML file with automatic credential resolution
./build/airavata-scheduler run tests/sample_experiment.yml \
  --project my-project \
  --compute cluster-1 \
  --storage s3-bucket-1 \
  --watch

# The CLI automatically:
# 1. Resolves credentials bound to compute/storage resources
# 2. Checks user permissions via SpiceDB
# 3. Retrieves secrets from OpenBao
# 4. Executes experiment with proper credentials
# 5. Shows real-time progress
```

### Using the API

```go
// Create experiment with dynamic template
experimentReq := &domain.CreateExperimentRequest{
    Name:        "Parameter Sweep",
    Description: "Testing different parameters",
    Template:    "python script.py --param {{.param_value}}",
    Parameters: []domain.ParameterSet{
        {Values: map[string]interface{}{"param_value": "1"}},
        {Values: map[string]interface{}{"param_value": "2"}},
        {Values: map[string]interface{}{"param_value": "3"}},
    },
}

experiment, err := experimentOrch.CreateExperiment(ctx, experimentReq)

// Submit for execution (credentials resolved automatically)
resp, err := experimentOrch.SubmitExperiment(ctx, &domain.SubmitExperimentRequest{
    ExperimentID: experiment.ID,
})
```

## 6. Credential Management

### Create and Share Credentials

```bash
# Create SSH key credential
curl -X POST http://localhost:8080/api/v1/credentials \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "cluster-ssh-key",
    "type": "ssh_key",
    "data": "-----BEGIN OPENSSH PRIVATE KEY-----\n...",
    "description": "SSH key for cluster access"
  }'

# Share credential with group
curl -X POST http://localhost:8080/api/v1/credentials/cred-123/share \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "principal_type": "group",
    "principal_id": "team-1",
    "permission": "read"
  }'

# Bind credential to resource
curl -X POST http://localhost:8080/api/v1/credentials/cred-123/bind \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "resource_type": "compute",
    "resource_id": "cluster-1"
  }'
```

## 7. Monitor Progress

### CLI Real-time Monitoring

```bash
# Watch experiment progress in real-time
./build/airavata-scheduler run experiment.yml --watch

# Check experiment status
./build/airavata-scheduler experiment status <experiment-id>
```

### API Monitoring

```go
// Get experiment status
resp, err := orchestrator.GetExperiment(ctx, &domain.GetExperimentRequest{
    ExperimentID: experimentID,
    IncludeTasks: true,
})

// Monitor task progress
for _, task := range resp.Experiment.Tasks {
    fmt.Printf("Task %s: %s\n", task.ID, task.Status)
}
```

## Next Steps

- [Deployment Guide](deployment.md) - Deploy in production
- [Credential Management](credential-management.md) - Advanced credential setup
- [API Reference](../reference/api.md) - Complete API documentation
- [Architecture Overview](../reference/architecture.md) - System design

## Troubleshooting

### Common Issues

**Services not starting:**
```bash
# Check service health
docker compose ps
docker compose logs spicedb
docker compose logs openbao
```

**SLURM nodes not connecting:**
```bash
# Check munge key consistency
docker exec airavata-scheduler-slurm-cluster-01-1 sha256sum /etc/munge/munge.key
docker exec airavata-scheduler-slurm-node-01-01-1 sha256sum /etc/munge/munge.key
# Both should show identical hashes

# Check SLURM status
docker exec airavata-scheduler-slurm-cluster-01-1 scontrol ping
docker exec airavata-scheduler-slurm-cluster-01-1 sinfo
```

**Schema upload fails:**
```bash
# Wait for SpiceDB to be ready
sleep 10
make spicedb-schema-upload
```

**CLI build fails:**
```bash
# Ensure Go modules are up to date
go mod tidy
go mod download
make build-cli
```

**Cold start issues:**
```bash
# Clean everything and start fresh
docker compose down -v --remove-orphans
./scripts/setup-cold-start.sh
```

### Getting Help

- Check the [troubleshooting section](deployment.md#troubleshooting) in the deployment guide
- Review [API documentation](../reference/api.md) for endpoint details
- Open an issue on [GitHub](https://github.com/apache/airavata/scheduler/issues)
