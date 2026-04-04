# API Documentation

## Overview

The Airavata Scheduler provides a RESTful HTTP API for managing distributed task execution across compute and storage resources.

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

## Core Endpoints

### Credential Management

The Airavata Scheduler uses a three-layer credential architecture with SpiceDB for authorization and OpenBao for secure storage.

#### Create Credential
```http
POST /api/v1/credentials
```

Store a new credential (SSH key, password, API token) in OpenBao with encrypted storage.

**Request Body:**
```json
{
  "name": "cluster-ssh-key",
  "type": "ssh_key",
  "data": "-----BEGIN OPENSSH PRIVATE KEY-----\n...",
  "description": "SSH key for cluster access"
}
```

**Response:**
- **201 Created**: Credential created successfully
- **400 Bad Request**: Invalid credential data
- **401 Unauthorized**: Authentication required
- **500 Internal Server Error**: Storage error

**Example:**
```bash
curl -X POST http://localhost:8080/api/v1/credentials \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "cluster-ssh-key",
    "type": "ssh_key", 
    "data": "-----BEGIN OPENSSH PRIVATE KEY-----\n...",
    "description": "SSH key for cluster access"
  }'
```

#### Share Credential
```http
POST /api/v1/credentials/{credential_id}/share
```

Share a credential with a user or group using SpiceDB authorization.

**Request Body:**
```json
{
  "principal_type": "user",
  "principal_id": "user-123",
  "permission": "read"
}
```

**Permissions:**
- `read`: Read-only access to credential
- `write`: Read and write access to credential
- `delete`: Full control (owner only)

**Response:**
- **200 OK**: Credential shared successfully
- **400 Bad Request**: Invalid permission or principal
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Credential not found

**Example:**
```bash
# Share with user
curl -X POST http://localhost:8080/api/v1/credentials/cred-123/share \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "principal_type": "user",
    "principal_id": "user-123", 
    "permission": "read"
  }'

# Share with group
curl -X POST http://localhost:8080/api/v1/credentials/cred-123/share \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "principal_type": "group",
    "principal_id": "team-1",
    "permission": "write"
  }'
```

#### Bind Credential to Resource
```http
POST /api/v1/credentials/{credential_id}/bind
```

Bind a credential to a compute or storage resource for automatic resolution during experiments.

**Request Body:**
```json
{
  "resource_type": "compute",
  "resource_id": "cluster-1"
}
```

**Resource Types:**
- `compute`: Compute resource (SLURM cluster, Kubernetes, bare metal)
- `storage`: Storage resource (S3, NFS, SFTP)

**Response:**
- **200 OK**: Credential bound successfully
- **400 Bad Request**: Invalid resource type or ID
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Credential or resource not found

**Example:**
```bash
# Bind to compute resource
curl -X POST http://localhost:8080/api/v1/credentials/cred-123/bind \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "resource_type": "compute",
    "resource_id": "cluster-1"
  }'

# Bind to storage resource
curl -X POST http://localhost:8080/api/v1/credentials/cred-456/bind \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "resource_type": "storage",
    "resource_id": "s3-bucket-1"
  }'
```

#### List Accessible Credentials
```http
GET /api/v1/credentials
```

List all credentials accessible to the authenticated user. This endpoint queries OpenBao for all stored credentials and returns only those the user has access to based on SpiceDB permissions.

**Query Parameters:**
- `type`: Filter by credential type (`ssh_key`, `password`, `api_key`)
- `resource_id`: Filter by bound resource ID
- `resource_type`: Filter by bound resource type

**Response:**
```json
{
  "credentials": [
    {
      "id": "cred-123",
      "name": "cluster-ssh-key",
      "type": "ssh_key",
      "description": "SSH key for cluster access",
      "created_at": "2024-01-15T10:30:00Z",
      "owner": "user-123",
      "bound_resources": [
        {
          "type": "compute",
          "id": "cluster-1"
        }
      ]
    }
  ]
}
```

**Example:**
```bash
# List all accessible credentials
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/credentials

# Filter by type
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/credentials?type=ssh_key"

# Filter by bound resource
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/credentials?resource_id=cluster-1&resource_type=compute"
```

### Worker Binary Distribution

#### Download Worker Binary
```http
GET /api/worker-binary
```

Downloads the worker binary for deployment to compute resources. This endpoint is used by compute resources to download the worker binary when spawning workers.

**Response:**
- **200 OK**: Worker binary file (application/octet-stream)
- **404 Not Found**: Worker binary not found
- **500 Internal Server Error**: Server error

**Example:**
```bash
# Download worker binary
curl -O http://localhost:8080/api/worker-binary

# Or with authentication
curl -H "Authorization: Bearer $TOKEN" -O http://localhost:8080/api/worker-binary
```

**Usage in Scripts:**
```bash
# In SLURM/Kubernetes/Bare Metal scripts
curl -L "${WORKER_BINARY_URL}" -o worker
chmod +x worker
./worker --server-address="${SERVER_ADDRESS}:${SERVER_PORT}"
```

### Resource Management

#### Register Compute Resource
```http
POST /resources/compute
Content-Type: application/json

{
  "name": "slurm-cluster-1",
  "type": "SLURM",
  "endpoint": "slurm.example.com",
  "credentialId": "cred-123",
  "costPerHour": 0.50,
  "maxWorkers": 10,
  "partition": "compute",
  "account": "research"
}
```

**Response:**
```json
{
  "id": "compute-abc123",
  "name": "slurm-cluster-1",
  "type": "SLURM",
  "status": "ACTIVE",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

#### Register Storage Resource
```http
POST /resources/storage
Content-Type: application/json

{
  "name": "s3-bucket-1",
  "type": "S3",
  "endpoint": "s3.amazonaws.com",
  "credentialId": "cred-456"
}
```

#### List Compute Resources
```http
GET /resources/compute
```

#### List Storage Resources
```http
GET /resources/storage
```

### Credential Management

#### Store Credential
```http
POST /credentials
Content-Type: application/json

{
  "name": "my-ssh-key",
  "type": "SSH_KEY",
  "data": "-----BEGIN PRIVATE KEY-----\n...",
  "ownerID": "user-123"
}
```

#### List Credentials
```http
GET /credentials
```

### Experiment Management

#### Create Experiment
```http
POST /experiments
Content-Type: application/json

{
  "name": "Parameter Sweep",
  "commandTemplate": "./simulate --param={{param1}} --value={{param2}}",
  "outputPattern": "result_{{param1}}_{{param2}}.dat",
  "parameters": [
    {
      "id": "set1",
      "values": {"param1": "0.1", "param2": "10"}
    },
    {
      "id": "set2",
      "values": {"param1": "0.2", "param2": "20"}
    }
  ]
}
```

**Response:**
```json
{
  "id": "exp-xyz789",
  "name": "Parameter Sweep",
  "status": "CREATED",
  "taskCount": 2,
  "createdAt": "2025-01-15T10:35:00Z"
}
```

#### Get Experiment
```http
GET /experiments/{id}
```

#### List Experiments
```http
GET /experiments
```

#### Submit Experiment
```http
POST /experiments/{id}/submit
Content-Type: application/json

{
  "computeResourceId": "compute-abc123"
}
```

#### List Experiment Outputs
```http
GET /experiments/{experiment_id}/outputs
```

List all output files for a completed experiment, organized by task ID.

**Path Parameters:**
- `experiment_id`: The ID of the experiment

**Response:**
- **200 OK**: List of output files organized by task
- **404 Not Found**: Experiment not found
- **401 Unauthorized**: Authentication required

**Response Body:**
```json
{
  "experiment_id": "exp_123",
  "outputs": [
    {
      "task_id": "task_456",
      "files": [
        {
          "path": "task_456/output.txt",
          "size": 1024,
          "checksum": "sha256:abc123...",
          "type": "file"
        },
        {
          "path": "task_456/error.log",
          "size": 512,
          "checksum": "sha256:def456...",
          "type": "file"
        }
      ]
    }
  ]
}
```

**Example:**
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  "http://localhost:8080/api/v1/experiments/exp_123/outputs"
```

#### Download Experiment Output Archive
```http
GET /experiments/{experiment_id}/outputs/archive
```

Download all experiment outputs as a single archive file (tar.gz).

**Path Parameters:**
- `experiment_id`: The ID of the experiment

**Response:**
- **200 OK**: Archive file (application/gzip)
- **404 Not Found**: Experiment not found or no outputs
- **401 Unauthorized**: Authentication required

**Example:**
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  "http://localhost:8080/api/v1/experiments/exp_123/outputs/archive" \
  -o experiment_outputs.tar.gz
```

#### Download Individual Output File
```http
GET /experiments/{experiment_id}/outputs/{file_path}
```

Download a specific output file from an experiment.

**Path Parameters:**
- `experiment_id`: The ID of the experiment
- `file_path`: The path to the file (URL encoded)

**Response:**
- **200 OK**: File content
- **404 Not Found**: File not found
- **401 Unauthorized**: Authentication required

**Example:**
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  "http://localhost:8080/api/v1/experiments/exp_123/outputs/task_456%2Foutput.txt" \
  -o output.txt
```

### Task Management

#### Get Task
```http
GET /tasks/{id}
```

#### List Tasks
```http
GET /tasks?experimentId={experimentId}&status={status}
```

#### Update Task Status
```http
PUT /tasks/{id}/status
Content-Type: application/json

{
  "status": "COMPLETED",
  "workerId": "worker-123"
}
```

### Worker Management

#### Register Worker
```http
POST /workers
Content-Type: application/json

{
  "id": "worker-001",
  "computeId": "compute-abc123",
  "status": "IDLE"
}
```

#### Worker Heartbeat
```http
POST /workers/{id}/heartbeat
```

**Response:** 200 OK

#### Get Next Task
```http
GET /workers/{id}/next-task
```

**Response (when task available):**
```json
{
  "task_id": "task-456",
  "command": "./simulate --param=0.1 --value=10",
  "output_path": "result_0.1_10.dat",
  "experiment_id": "exp-xyz789"
}
```

**Response (when no tasks):** 204 No Content

#### Claim Task
```http
POST /workers/{id}/claim
Content-Type: application/json

{
  "task_id": "task-456"
}
```

**Response:**
```json
{
  "claimed": true,
  "task_id": "task-456",
  "worker_id": "worker-001",
  "claimed_at": "2025-01-15T10:40:00Z",
  "command": "./simulate --param=0.1 --value=10",
  "output_path": "result_0.1_10.dat"
}
```

### Authentication

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "researcher",
  "password": "secret"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 3600
}
```

#### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Project Management

#### Create Project
```http
POST /api/v1/projects
Content-Type: application/json

{
  "name": "My Research Project",
  "description": "A research project for parameter sweeps",
  "tags": ["simulation", "research"]
}
```

**Response:**
```json
{
  "id": "proj-123",
  "name": "My Research Project",
  "description": "A research project for parameter sweeps",
  "tags": ["simulation", "research"],
  "created_at": "2025-01-15T10:30:00Z",
  "owner": "user-123"
}
```

#### List Projects
```http
GET /api/v1/projects
```

#### Get Project
```http
GET /api/v1/projects/{project_id}
```

#### Update Project
```http
PUT /api/v1/projects/{project_id}
Content-Type: application/json

{
  "name": "Updated Project Name",
  "description": "Updated description"
}
```

#### Delete Project
```http
DELETE /api/v1/projects/{project_id}
```

#### List Project Members
```http
GET /api/v1/projects/{project_id}/members
```

#### Add Project Member
```http
POST /api/v1/projects/{project_id}/members
Content-Type: application/json

{
  "user_id": "user-456",
  "role": "member"
}
```

#### Remove Project Member
```http
DELETE /api/v1/projects/{project_id}/members/{user_id}
```

### Data Management

#### Upload File
```http
POST /api/v1/data/upload
Content-Type: multipart/form-data

file: <file_content>
path: storage:/path/to/file
```

#### Upload Directory
```http
POST /api/v1/data/upload-dir
Content-Type: multipart/form-data

archive: <tar.gz_content>
path: storage:/path/to/directory
```

#### List Files
```http
GET /api/v1/data/list?path=storage:/path/to/directory
```

#### Download File
```http
GET /api/v1/data/download?path=storage:/path/to/file
```

#### Download Directory
```http
GET /api/v1/data/download-dir?path=storage:/path/to/directory
```

### Resource Testing and Management

#### Test Resource Credential
```http
POST /api/v1/resources/{resource_id}/test-credential
```

#### Get Resource Status
```http
GET /api/v1/resources/{resource_id}/status
```

#### Get Resource Metrics
```http
GET /api/v1/resources/{resource_id}/metrics
```

#### Test Resource Connectivity
```http
POST /api/v1/resources/{resource_id}/test
```

#### Bind Credential to Resource
```http
POST /api/v1/resources/{resource_id}/bind-credential
Content-Type: application/json

{
  "credential_id": "cred-123"
}
```

#### Unbind Credential from Resource
```http
DELETE /api/v1/resources/{resource_id}/bind-credential
```

### Experiment Lifecycle Management

#### Cancel Experiment
```http
POST /api/v1/experiments/{experiment_id}/cancel
```

#### Pause Experiment
```http
POST /api/v1/experiments/{experiment_id}/pause
```

#### Resume Experiment
```http
POST /api/v1/experiments/{experiment_id}/resume
```

#### Get Experiment Logs
```http
GET /api/v1/experiments/{experiment_id}/logs?task_id={task_id}
```

#### Resubmit Experiment
```http
POST /api/v1/experiments/{experiment_id}/resubmit
Content-Type: application/json

{
  "failed_only": true
}
```

#### Retry Experiment
```http
POST /api/v1/experiments/{experiment_id}/retry
Content-Type: application/json

{
  "failed_only": true
}
```

### Health Check

#### Check API Health
```http
GET /health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

## Error Responses

All error responses follow this format:

```json
{
  "error": "Error message description",
  "code": "ERROR_CODE",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

### Common HTTP Status Codes

- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `204 No Content` - Request successful, no content to return
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., task already claimed)
- `500 Internal Server Error` - Server error

## Rate Limiting

API requests are rate-limited to prevent abuse. Default limits:
- 100 requests per minute per IP
- 1000 requests per hour per user

Rate limit headers are included in responses:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642256400
```

## Pagination

List endpoints support pagination:

```http
GET /experiments?page=1&pageSize=20
```

**Response:**
```json
{
  "experiments": [...],
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "totalPages": 5,
    "totalItems": 100
  }
}
```

## WebSocket Support (Future)

Real-time task status updates will be available via WebSocket:

```
ws://localhost:8080/ws/tasks/{experimentId}
```

