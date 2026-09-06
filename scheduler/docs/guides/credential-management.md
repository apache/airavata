# Credential Management Guide

## Overview

The Airavata Scheduler implements a secure, scalable credential management system that separates authorization logic from storage, using best-in-class open-source tools for each concern. This guide covers architecture, quick start examples, deployment, and best practices.

## Architecture

### Three-Layer Design

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│   (Experiments, Resources, Users, Groups)                   │
└────────────┬────────────────────────────────────────────────┘
             │
             ├──────────────────┬──────────────────┐
             │                  │                  │
┌────────────▼─────┐  ┌────────▼────────┐  ┌─────▼──────────┐
│   PostgreSQL     │  │    SpiceDB      │  │   OpenBao      │
│                  │  │                 │  │                │
│  Domain Data     │  │  Authorization  │  │  Secrets       │
│  - Users         │  │  - Permissions  │  │  - SSH Keys    │
│  - Groups        │  │  - Ownership    │  │  - Passwords   │
│  - Experiments   │  │  - Sharing      │  │  - Tokens      │
│  - Resources     │  │  - Hierarchies  │  │  (Encrypted)   │
└──────────────────┘  └─────────────────┘  └────────────────┘
```

### Component Responsibilities

#### 1. **PostgreSQL** - Domain Entity Storage
**Purpose:** Stores non-sensitive business domain entities.

**Data Stored:**
- User profiles (name, email, UID, GID)
- Group definitions (name, description)
- Compute resources (name, type, endpoint)
- Storage resources (bucket, endpoint, type)
- Experiments and tasks (state, config, results)

**What it DOES NOT store:**
- ❌ Credentials (SSH keys, passwords, tokens)
- ❌ Permission relationships
- ❌ Access control lists

#### 2. **SpiceDB** - Fine-Grained Authorization
**Purpose:** Manages all permission relationships and access control.

**Capabilities:**
- Owner/reader/writer relationships for credentials
- Hierarchical group memberships with transitive inheritance
- Resource-to-credential bindings
- Permission checks using Zanzibar model
- Real-time relationship updates

**Schema:**
```zed
definition user {}

definition group {
  relation member: user | group
  relation parent: group
  
  // Recursive permission inheritance through group hierarchy
  permission is_member = member + parent->is_member
}

definition credential {
  relation owner: user
  relation reader: user | group#is_member
  relation writer: user | group#is_member
  
  // Permissions with inheritance
  permission read = owner + reader + writer
  permission write = owner + writer
  permission delete = owner
}

definition compute_resource {
  relation bound_credential: credential
}

definition storage_resource {
  relation bound_credential: credential
}
```

#### 3. **OpenBao** - Secure Credential Storage
**Purpose:** Encrypts and stores sensitive credential data.

**Features:**
- KV v2 secrets engine for credential storage
- AES-256-GCM encryption at rest
- Transit engine for encryption key management
- Audit logging for all operations
- Secret versioning and rotation support

**Storage Structure:**
```
secret/data/credentials/{credential_id}
├── data/
│   ├── name: "cluster-ssh-key"
│   ├── type: "ssh_key"
│   ├── data: "-----BEGIN OPENSSH PRIVATE KEY-----..."
│   ├── owner_id: "user-123"
│   └── created_at: "2024-01-15T10:30:00Z"
├── metadata/
│   ├── created_time: "2024-01-15T10:30:00Z"
│   ├── current_version: 1
│   └── versions: {...}
```

### Credential Resolution Flow

When an experiment is submitted, the system follows this flow:

```
1. User submits experiment
   ↓
2. System identifies required resources (compute, storage)
   ↓
3. SpiceDB: Find credentials bound to each resource
   ↓
4. SpiceDB: Check user has read permission on each credential
   ↓
5. OpenBao: Decrypt and retrieve credential data
   ↓
6. System: Provide credentials to workers for execution
```

### Permission Model

```
credential owner    → Full control (read/write/delete/share)
credential reader   → Read-only access (can be user or group)
credential writer   → Read + write (can be user or group)
```

**Hierarchical groups**: If Group B is a member of Group A, and a credential is shared with Group A, members of Group B automatically inherit access through the `is_member` permission.

## Quick Start

### Prerequisites

```bash
# Start services
make docker-up
make wait-services
make spicedb-schema-upload

# Set environment
export API_BASE="http://localhost:8080"
export VAULT_ADDR="http://localhost:8200"
export VAULT_TOKEN="dev-token"

# Obtain authentication token (adjust based on your auth system)
export AUTH_TOKEN="your-jwt-token"
```

### 1. Create a User

```bash
# Create user with UID/GID
curl -X POST $API_BASE/api/v1/users \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "uid": 1001,
    "gid": 1001
  }'

# Response
{
  "id": "user-alice-123",
  "username": "alice",
  "email": "alice@example.com",
  "uid": 1001,
  "gid": 1001,
  "created_at": "2025-01-15T10:00:00Z"
}
```

### 2. Create a Group

```bash
# Create group
curl -X POST $API_BASE/api/v1/groups \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "research-team",
    "description": "Research team members"
  }'

# Add user to group
curl -X POST $API_BASE/api/v1/groups/research-team/members \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "user-alice-123"
  }'
```

### 3. Store Credentials

```bash
# Store SSH key
curl -X POST $API_BASE/api/v1/credentials \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "cluster-ssh-key",
    "type": "ssh_key",
    "data": "-----BEGIN OPENSSH PRIVATE KEY-----\nb3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAABlwAAAAdzc2gtcn\n...",
    "description": "SSH key for cluster access"
  }'

# Store API key
curl -X POST $API_BASE/api/v1/credentials \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "s3-api-key",
    "type": "api_key",
    "data": "AKIAIOSFODNN7EXAMPLE",
    "description": "S3 access key"
  }'

# Store password
curl -X POST $API_BASE/api/v1/credentials \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "database-password",
    "type": "password",
    "data": "super-secret-password",
    "description": "Database password"
  }'
```

### 4. Share Credentials

```bash
# Share with user (read access)
curl -X POST $API_BASE/api/v1/credentials/cred-123/share \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "principal_type": "user",
    "principal_id": "user-bob-456",
    "permission": "read"
  }'

# Share with group (write access)
curl -X POST $API_BASE/api/v1/credentials/cred-123/share \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "principal_type": "group",
    "principal_id": "research-team",
    "permission": "write"
  }'
```

### 5. Bind Credentials to Resources

```bash
# Bind SSH key to compute resource
curl -X POST $API_BASE/api/v1/credentials/cred-123/bind \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "resource_type": "compute",
    "resource_id": "slurm-cluster-1"
  }'

# Bind API key to storage resource
curl -X POST $API_BASE/api/v1/credentials/cred-456/bind \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "resource_type": "storage",
    "resource_id": "s3-bucket-1"
  }'
```

### 6. List Accessible Credentials

```bash
# List all accessible credentials
curl -H "Authorization: Bearer $AUTH_TOKEN" \
  $API_BASE/api/v1/credentials

# Filter by type
curl -H "Authorization: Bearer $AUTH_TOKEN" \
  "$API_BASE/api/v1/credentials?type=ssh_key"

# Filter by bound resource
curl -H "Authorization: Bearer $AUTH_TOKEN" \
  "$API_BASE/api/v1/credentials?resource_id=slurm-cluster-1&resource_type=compute"
```

### 7. Use Credentials in Experiments

```bash
# Run experiment - credentials are automatically resolved
./build/airavata-scheduler run experiment.yml \
  --project my-project \
  --compute slurm-cluster-1 \
  --storage s3-bucket-1 \
  --watch
```

## Deployment

### Development Setup

#### Using Docker Compose

```bash
# Start all services including SpiceDB and OpenBao
make docker-up

# Wait for services to be healthy
make wait-services

# Upload SpiceDB schema
make spicedb-schema-upload

# Verify services are running
docker compose ps
```

#### Service Endpoints
- **SpiceDB gRPC:** `localhost:50051`
- **SpiceDB HTTP:** `localhost:50052`
- **OpenBao:** `http://localhost:8200`
- **SpiceDB PostgreSQL:** `localhost:5433` (internal)

#### Development Credentials
- **SpiceDB Token:** `somerandomkeyhere`
- **OpenBao Token:** `dev-token`

⚠️ **WARNING:** These are insecure development credentials. Never use in production!

### Production Deployment

#### SpiceDB Production Setup

**Architecture Overview:**
```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│   SpiceDB     │────▶│   PostgreSQL  │     │   Load        │
│   (3 replicas)│     │   (Primary)   │     │   Balancer    │
└───────────────┘     └───────────────┘     └───────────────┘
         │                     │                       │
         │                     │                       │
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│   SpiceDB     │     │   PostgreSQL  │     │   Clients     │
│   (3 replicas)│     │   (Replica)   │     │   (Apps)      │
└───────────────┘     └───────────────┘     └───────────────┘
```

**Kubernetes Deployment:**

```yaml
# spicedb-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spicedb
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spicedb
  template:
    metadata:
      labels:
        app: spicedb
    spec:
      containers:
      - name: spicedb
        image: authzed/spicedb:latest
        command: ["serve"]
        args:
          - "--grpc-preshared-key=$(SPICEDB_TOKEN)"
          - "--datastore-engine=postgres"
          - "--datastore-conn-uri=$(DATABASE_URL)"
          - "--grpc-tls-cert-path=/tls/cert.pem"
          - "--grpc-tls-key-path=/tls/key.pem"
        ports:
        - containerPort: 50051
        - containerPort: 50052
        env:
        - name: SPICEDB_TOKEN
          valueFrom:
            secretKeyRef:
              name: spicedb-secrets
              key: token
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: spicedb-secrets
              key: database-url
        volumeMounts:
        - name: tls-certs
          mountPath: /tls
        livenessProbe:
          exec:
            command:
            - grpc_health_probe
            - -addr=:50051
            - -tls
            - -tls-server-name=spicedb
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          exec:
            command:
            - grpc_health_probe
            - -addr=:50051
            - -tls
            - -tls-server-name=spicedb
          initialDelaySeconds: 5
          periodSeconds: 5
      volumes:
      - name: tls-certs
        secret:
          secretName: spicedb-tls
```

#### OpenBao Production Setup

**Architecture Overview:**
```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│   OpenBao     │────▶│   Storage     │     │   Load        │
│   (3 nodes)   │     │   Backend     │     │   Balancer    │
└───────────────┘     └───────────────┘     └───────────────┘
         │                     │                       │
         │                     │                       │
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│   OpenBao     │     │   Consul      │     │   Clients     │
│   (3 nodes)   │     │   (HA)        │     │   (Apps)      │
└───────────────┘     └───────────────┘     └───────────────┘
```

**Kubernetes Deployment:**

```yaml
# openbao-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: openbao
spec:
  replicas: 3
  selector:
    matchLabels:
      app: openbao
  template:
    metadata:
      labels:
        app: openbao
    spec:
      containers:
      - name: openbao
        image: hashicorp/vault:latest
        command: ["vault", "server"]
        args:
          - "-config=/vault/config/vault.hcl"
        ports:
        - containerPort: 8200
        volumeMounts:
        - name: config
          mountPath: /vault/config
        - name: data
          mountPath: /vault/data
        env:
        - name: VAULT_ADDR
          value: "https://0.0.0.0:8200"
        - name: VAULT_CACERT
          value: "/vault/config/ca.pem"
        livenessProbe:
          httpGet:
            path: /v1/sys/health
            port: 8200
            scheme: HTTPS
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /v1/sys/health
            port: 8200
            scheme: HTTPS
          initialDelaySeconds: 5
          periodSeconds: 5
      volumes:
      - name: config
        configMap:
          name: openbao-config
      - name: data
        persistentVolumeClaim:
          claimName: openbao-data
```

**OpenBao Configuration:**

```hcl
# vault.hcl
storage "consul" {
  address = "consul:8500"
  path    = "vault/"
  service = "vault"
}

listener "tcp" {
  address = "0.0.0.0:8200"
  tls_cert_file = "/vault/config/cert.pem"
  tls_key_file  = "/vault/config/key.pem"
}

api_addr = "https://openbao:8200"
cluster_addr = "https://openbao:8201"
ui = true

# Enable audit logging
audit {
  enabled = true
  path = "file"
  file_path = "/vault/logs/audit.log"
}
```

#### Initialize OpenBao

```bash
# Initialize OpenBao (production)
vault operator init

# Enable KV v2 secrets engine
vault secrets enable -path=secret kv-v2

# Enable transit engine for encryption
vault secrets enable transit

# Create encryption key
vault write -f transit/keys/credentials

# Enable audit logging
vault audit enable file file_path=/vault/logs/audit.log
```

## Best Practices

### Security

1. **Use TLS in Production**
   - Enable TLS for all SpiceDB and OpenBao endpoints
   - Use proper certificates from a trusted CA
   - Rotate certificates regularly

2. **Secure Token Management**
   - Use strong, randomly generated tokens
   - Store tokens in Kubernetes secrets or environment variables
   - Rotate tokens regularly
   - Never commit tokens to version control

3. **Network Security**
   - Use network policies to restrict access
   - Deploy in private networks when possible
   - Use VPN or bastion hosts for access

4. **Audit Logging**
   - Enable audit logging for all operations
   - Monitor for suspicious activity
   - Retain logs for compliance requirements

### Performance

1. **SpiceDB Optimization**
   - Use connection pooling
   - Implement caching for frequently accessed permissions
   - Monitor query performance
   - Use read replicas for scaling

2. **OpenBao Optimization**
   - Use appropriate storage backends
   - Enable caching for frequently accessed secrets
   - Monitor storage usage
   - Implement backup strategies

### Operations

1. **Monitoring**
   - Set up health checks and monitoring
   - Monitor resource usage and performance
   - Set up alerting for failures
   - Track audit logs

2. **Backup and Recovery**
   - Regular backups of SpiceDB data
   - Backup OpenBao storage backend
   - Test recovery procedures
   - Document disaster recovery plans

3. **Updates and Maintenance**
   - Plan for regular updates
   - Test updates in staging environments
   - Have rollback procedures ready
   - Monitor for security updates

## Troubleshooting

### Common Issues

**SpiceDB Connection Issues:**
```bash
# Check SpiceDB health
curl -s http://localhost:50052/healthz

# Check gRPC connectivity
grpc_health_probe -addr=localhost:50051

# Check logs
docker compose logs spicedb
```

**OpenBao Connection Issues:**
```bash
# Check OpenBao health
curl -s http://localhost:8200/v1/sys/health | jq

# Check authentication
vault auth -method=token token=dev-token

# Check logs
docker compose logs openbao
```

**Schema Upload Issues:**
```bash
# Wait for SpiceDB to be ready
sleep 10

# Upload schema manually
docker run --rm --network host \
  -v $(pwd)/db/spicedb_schema.zed:/schema.zed \
  authzed/zed:latest schema write \
  --endpoint localhost:50051 \
  --token "somerandomkeyhere" \
  --insecure \
  /schema.zed
```

**Permission Check Issues:**
```bash
# Test permission check
docker run --rm --network host \
  authzed/zed:latest permission check \
  --endpoint localhost:50051 \
  --token "somerandomkeyhere" \
  --insecure \
  credential:cred-123 read user:user-123
```

### Debugging

1. **Enable Debug Logging**
   - Set log levels to debug
   - Monitor application logs
   - Check service logs

2. **Test Connectivity**
   - Use health check endpoints
   - Test gRPC connectivity
   - Verify network access

3. **Validate Configuration**
   - Check environment variables
   - Verify configuration files
   - Test with minimal configuration

### Getting Help

- Check the [API Reference](../reference/api.md) for endpoint details
- Review [Architecture Overview](../reference/architecture.md) for system design
- Open an issue on [GitHub](https://github.com/apache/airavata/scheduler/issues)
- Check service-specific documentation for SpiceDB and OpenBao
