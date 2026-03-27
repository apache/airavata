# Airavata Scheduler CLI Reference

The Airavata Scheduler CLI (`airavata`) provides a comprehensive command-line interface for managing experiments, resources, projects, and data in the Airavata Scheduler system.

## Table of Contents

- [Installation](#installation)
- [Authentication](#authentication)
- [Configuration](#configuration)
- [Command Overview](#command-overview)
- [Data Management](#data-management)
- [Experiment Management](#experiment-management)
- [Project Management](#project-management)
- [Resource Management](#resource-management)
- [User Management](#user-management)
- [Common Workflows](#common-workflows)
- [Troubleshooting](#troubleshooting)

## Installation

The CLI is built as part of the Airavata Scheduler project. After building the project, the `airavata` binary will be available in the `bin/` directory.

```bash
# Build the CLI
make build

# The binary will be available at
./bin/airavata
```

## Authentication

Before using the CLI, you need to authenticate with the Airavata Scheduler server.

### Login

```bash
# Interactive login
airavata auth login

# Login with username
airavata auth login myusername

# Login with admin credentials
airavata auth login --admin
```

### Check Authentication Status

```bash
airavata auth status
```

### Logout

```bash
airavata auth logout
```

## Configuration

### Set Server URL

```bash
airavata config set server http://localhost:8080
```

### View Configuration

```bash
airavata config show
```

## Command Overview

The CLI is organized into several command groups:

- `auth` - Authentication and session management
- `user` - User profile and account management
- `project` - Project management and collaboration
- `resource` - Compute and storage resource management
- `experiment` - Experiment lifecycle management
- `data` - Data upload, download, and management
- `config` - CLI configuration management

## Data Management

The `data` commands allow you to upload, download, and manage data in storage resources.

### Upload Data

```bash
# Upload a single file
airavata data upload input.dat minio-storage:/experiments/input.dat

# Upload a directory recursively
airavata data upload-dir ./data minio-storage:/experiments/data

# Upload to S3 storage
airavata data upload results.csv s3-bucket:/data/results.csv
```

### Download Data

```bash
# Download a single file
airavata data download minio-storage:/experiments/input.dat ./input.dat

# Download a directory
airavata data download-dir minio-storage:/experiments/data ./data

# Download from S3 storage
airavata data download s3-bucket:/data/results.csv ./results.csv
```

### List Files

```bash
# List files in storage path
airavata data list minio-storage:/experiments/

# List files in specific directory
airavata data list s3-bucket:/data/experiment-123/
```

## Experiment Management

The `experiment` commands provide comprehensive experiment lifecycle management.

### Run Experiments

```bash
# Run experiment from YAML file
airavata experiment run experiment.yml

# Run with specific project and compute resource
airavata experiment run experiment.yml --project proj-123 --compute slurm-1

# Run with custom parameters
airavata experiment run experiment.yml --param nodes=4 --param walltime=2h
```

### Monitor Experiments

```bash
# Check experiment status
airavata experiment status exp-123

# Watch experiment in real-time
airavata experiment watch exp-123

# List all experiments
airavata experiment list
```

### Experiment Lifecycle

```bash
# Cancel a running experiment
airavata experiment cancel exp-123

# Pause a running experiment (if supported)
airavata experiment pause exp-123

# Resume a paused experiment
airavata experiment resume exp-123

# View experiment logs
airavata experiment logs exp-123

# View logs for specific task
airavata experiment logs exp-123 --task task-456

# Resubmit a failed experiment
airavata experiment resubmit exp-123

# Retry only failed tasks
airavata experiment retry exp-123 --failed-only
```

### Task Management

```bash
# List all tasks for an experiment
airavata experiment tasks exp-123

# Get specific task details
airavata experiment task task-456

# Get task execution logs
airavata experiment task task-456 --logs
```

### Output Management

```bash
# List experiment outputs organized by task
airavata experiment outputs exp-123

# Download all outputs as archive
airavata experiment download exp-123 --output ./results/

# Download specific task outputs
airavata experiment download exp-123 --task task-456 --output ./task-outputs/

# Download specific file
airavata experiment download exp-123 --file task-456/output.txt --output ./output.txt

# Download without extracting archive
airavata experiment download exp-123 --output ./archive.tar.gz --extract=false
```

## Project Management

The `project` commands allow you to manage projects and collaborate with team members.

### Create and Manage Projects

```bash
# Create a new project (interactive)
airavata project create

# List your projects
airavata project list

# Get project details
airavata project get proj-123

# Update project information
airavata project update proj-123

# Delete a project
airavata project delete proj-123
```

### Project Members

```bash
# List project members
airavata project members proj-123

# Add a member to project
airavata project add-member proj-123 user-456

# Add member with specific role
airavata project add-member proj-123 user-456 --role admin

# Remove member from project
airavata project remove-member proj-123 user-456
```

## Resource Management

The `resource` commands provide comprehensive management of compute and storage resources.

### Compute Resources

```bash
# List compute resources
airavata resource compute list

# Get compute resource details
airavata resource compute get compute-123

# Create new compute resource
airavata resource compute create

# Update compute resource
airavata resource compute update compute-123

# Delete compute resource
airavata resource compute delete compute-123
```

### Storage Resources

```bash
# List storage resources
airavata resource storage list

# Get storage resource details
airavata resource storage get storage-123

# Create new storage resource
airavata resource storage create

# Update storage resource
airavata resource storage update storage-123

# Delete storage resource
airavata resource storage delete storage-123
```

### Credential Management

```bash
# List credentials
airavata resource credential list

# Create new credential
airavata resource credential create

# Delete credential
airavata resource credential delete cred-123
```

### Credential Binding

```bash
# Bind credential to resource with verification
airavata resource bind-credential compute-123 cred-456

# Unbind credential from resource
airavata resource unbind-credential compute-123

# Test if bound credential works
airavata resource test-credential compute-123
```

### Resource Monitoring

```bash
# Check resource status
airavata resource status compute-123

# View resource metrics
airavata resource metrics compute-123

# Test resource connectivity
airavata resource test compute-123
```

## User Management

The `user` commands allow you to manage your user profile and account.

### Profile Management

```bash
# View your profile
airavata user profile

# Update your profile
airavata user update

# Change your password
airavata user password
```

### Groups and Projects

```bash
# List your groups
airavata user groups

# List your projects
airavata user projects
```

## Common Workflows

### Complete Experiment Workflow

```bash
# 1. Authenticate
airavata auth login

# 2. Create or select project
airavata project create
# or
airavata project list

# 3. Upload input data
airavata data upload input.dat minio-storage:/experiments/input.dat

# 4. Run experiment
airavata experiment run experiment.yml --project proj-123

# 5. Monitor experiment
airavata experiment watch exp-456

# 6. Check outputs
airavata experiment outputs exp-456

# 7. Download results
airavata experiment download exp-456 --output ./results/
```

### Resource Setup Workflow

```bash
# 1. Create compute resource
airavata resource compute create

# 2. Create storage resource
airavata resource storage create

# 3. Create credentials
airavata resource credential create

# 4. Bind credentials to resources
airavata resource bind-credential compute-123 cred-456
airavata resource bind-credential storage-789 cred-456

# 5. Test resource connectivity
airavata resource test compute-123
airavata resource test storage-789
```

### Project Collaboration Workflow

```bash
# 1. Create project
airavata project create

# 2. Add team members
airavata project add-member proj-123 user-456 --role admin
airavata project add-member proj-123 user-789 --role member

# 3. List project members
airavata project members proj-123

# 4. Run experiments in project context
airavata experiment run experiment.yml --project proj-123
```

## Troubleshooting

### Authentication Issues

```bash
# Check authentication status
airavata auth status

# If expired, login again
airavata auth login

# Clear configuration if needed
airavata config show
# Manually edit ~/.airavata/config if needed
```

### Connection Issues

```bash
# Check server URL configuration
airavata config show

# Set correct server URL
airavata config set server http://your-server:8080

# Test server connectivity
curl http://your-server:8080/health
```

### Resource Issues

```bash
# Check resource status
airavata resource status compute-123

# Test resource connectivity
airavata resource test compute-123

# Test credentials
airavata resource test-credential compute-123

# View resource metrics
airavata resource metrics compute-123
```

### Experiment Issues

```bash
# Check experiment status
airavata experiment status exp-123

# View experiment logs
airavata experiment logs exp-123

# Check task details
airavata experiment tasks exp-123
airavata experiment task task-456 --logs

# Retry failed tasks
airavata experiment retry exp-123 --failed-only
```

### Data Issues

```bash
# List files in storage
airavata data list storage-123:/path/

# Test file upload
airavata data upload test.txt storage-123:/test/test.txt

# Test file download
airavata data download storage-123:/test/test.txt ./downloaded.txt
```

## Environment Variables

The CLI respects the following environment variables:

- `AIRAVATA_SERVER_URL` - Default server URL
- `AIRAVATA_CONFIG_DIR` - Configuration directory (default: `~/.airavata`)

## Configuration File

The CLI stores configuration in `~/.airavata/config`:

```yaml
server_url: "http://localhost:8080"
username: "myusername"
token: "jwt-token-here"
```

## Exit Codes

The CLI uses standard exit codes:

- `0` - Success
- `1` - General error
- `2` - Authentication error
- `3` - Configuration error
- `4` - Network error
- `5` - Resource not found

## Examples

### Example Experiment YAML

```yaml
name: "Hello World Experiment"
description: "Simple hello world experiment"
project: "proj-123"
compute_resource: "slurm-1"
storage_resource: "minio-storage"

parameters:
  nodes:
    type: integer
    default: 1
    description: "Number of nodes"
  walltime:
    type: string
    default: "00:05:00"
    description: "Wall time limit"

scripts:
  main: |
    #!/bin/bash
    echo "Hello from node $SLURM_NODEID"
    echo "Running on $(hostname)"
    echo "Wall time: $WALLTIME"
    echo "Nodes: $NODES"
    
    # Create output file
    echo "Experiment completed successfully" > output.txt
    echo "Timestamp: $(date)" >> output.txt
```

### Example Resource Creation

```bash
# Create SLURM compute resource
airavata resource compute create
# Follow prompts:
# Name: slurm-cluster-1
# Type: slurm
# Endpoint: localhost:6817
# Max Workers: 10
# Cost Per Hour: 0.50

# Create MinIO storage resource
airavata resource storage create
# Follow prompts:
# Name: minio-storage
# Type: s3
# Endpoint: localhost:9000
# Bucket: experiments
# Access Key: minioadmin
# Secret Key: minioadmin
```

This CLI reference provides comprehensive coverage of all available commands and workflows. For additional help, use the `--help` flag with any command:

```bash
airavata --help
airavata experiment --help
airavata experiment run --help
```
