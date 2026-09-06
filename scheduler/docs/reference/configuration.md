# Configuration Reference

This document describes all configuration options available in the Airavata Scheduler.

## Configuration Sources

Configuration is loaded in the following order of precedence (later sources override earlier ones):

1. **Default values** (hardcoded in `config/default.yaml`)
2. **Configuration file** (`config/default.yaml` or custom path)
3. **Environment variables**
4. **Command line flags** (for CLI and worker)

## Configuration Files

### Main Configuration File

**Location:** `config/default.yaml`

This YAML file contains all default configuration values for the application.

### Environment File

**Location:** `.env` (create from `.env.example`)

Contains environment variable overrides for development and deployment.

### CLI Configuration

**Location:** `~/.airavata/config.json`

User-specific CLI configuration including server URL and authentication tokens.

## Configuration Sections

### Database Configuration

```yaml
database:
  dsn: "postgres://user:password@localhost:5432/airavata?sslmode=disable"
```

**Environment Variables:**
- `DATABASE_URL` - Complete database connection string

**Components:**
- `POSTGRES_HOST` - Database host (default: localhost)
- `POSTGRES_PORT` - Database port (default: 5432)
- `POSTGRES_USER` - Database user (default: user)
- `POSTGRES_PASSWORD` - Database password (default: password)
- `POSTGRES_DB` - Database name (default: airavata)

### Server Configuration

```yaml
server:
  host: "0.0.0.0"
  port: 8080
  read_timeout: "15s"
  write_timeout: "15s"
  idle_timeout: "60s"
```

**Environment Variables:**
- `HOST` - Server bind address (default: 0.0.0.0)
- `PORT` - HTTP server port (default: 8080)

### gRPC Configuration

```yaml
grpc:
  host: "0.0.0.0"
  port: 50051
```

**Environment Variables:**
- `GRPC_PORT` - gRPC server port (default: 50051)

### Worker Configuration

```yaml
worker:
  binary_path: "./build/worker"
  binary_url: "http://localhost:8080/api/worker-binary"
  default_working_dir: "/tmp/worker"
  heartbeat_interval: "10s"
  dial_timeout: "30s"
  request_timeout: "60s"
```

**Environment Variables:**
- `WORKER_BINARY_PATH` - Path to worker binary (default: ./build/worker)
- `WORKER_BINARY_URL` - URL for worker binary download (default: http://localhost:8080/api/worker-binary)
- `WORKER_WORKING_DIR` - Default working directory (default: /tmp/worker)
- `WORKER_SERVER_URL` - gRPC server URL for workers (default: localhost:50051)
- `WORKER_HEARTBEAT_INTERVAL` - Heartbeat interval (default: 30s)
- `WORKER_TASK_TIMEOUT` - Task timeout (default: 24h)

### SpiceDB Configuration

```yaml
spicedb:
  endpoint: "localhost:50052"
  preshared_key: "somerandomkeyhere"
  dial_timeout: "30s"
```

**Environment Variables:**
- `SPICEDB_ENDPOINT` - SpiceDB server endpoint (default: localhost:50052)
- `SPICEDB_PRESHARED_KEY` - SpiceDB authentication token (default: somerandomkeyhere)

### OpenBao/Vault Configuration

```yaml
openbao:
  address: "http://localhost:8200"
  token: "dev-token"
  mount_path: "secret"
  dial_timeout: "30s"
```

**Environment Variables:**
- `VAULT_ENDPOINT` - Vault server address (default: http://localhost:8200)
- `VAULT_TOKEN` - Vault authentication token (default: dev-token)

### Services Configuration

```yaml
services:
  postgres:
    host: "localhost"
    port: 5432
    database: "airavata"
    user: "user"
    password: "password"
    ssl_mode: "disable"
  minio:
    host: "localhost"
    port: 9000
    access_key: "minioadmin"
    secret_key: "minioadmin"
    use_ssl: false
  sftp:
    host: "localhost"
    port: 2222
    username: "testuser"
  nfs:
    host: "localhost"
    port: 2049
    mount_path: "/mnt/nfs"
```

**Environment Variables:**
- `MINIO_HOST` - MinIO server host (default: localhost)
- `MINIO_PORT` - MinIO server port (default: 9000)
- `MINIO_ACCESS_KEY` - MinIO access key (default: minioadmin)
- `MINIO_SECRET_KEY` - MinIO secret key (default: minioadmin)
- `SFTP_HOST` - SFTP server host (default: localhost)
- `SFTP_PORT` - SFTP server port (default: 2222)
- `NFS_HOST` - NFS server host (default: localhost)
- `NFS_PORT` - NFS server port (default: 2049)

### Compute Resource Configuration

```yaml
compute:
  slurm:
    default_partition: "debug"
    default_account: ""
    default_qos: ""
    job_timeout: "3600s"
    ssh_timeout: "30s"
  baremetal:
    ssh_timeout: "30s"
    default_working_dir: "/tmp/worker"
  kubernetes:
    default_namespace: "default"
    default_service_account: "default"
    pod_timeout: "300s"
    job_timeout: "3600s"
  docker:
    default_image: "alpine:latest"
    container_timeout: "300s"
    network_mode: "bridge"
```

### Storage Configuration

```yaml
storage:
  s3:
    region: "us-east-1"
    timeout: "30s"
    max_retries: 3
  sftp:
    timeout: "30s"
    max_retries: 3
  nfs:
    timeout: "30s"
    max_retries: 3
```

### JWT Configuration

```yaml
jwt:
  secret_key: ""
  algorithm: "HS256"
  issuer: "airavata-scheduler"
  audience: "airavata-users"
  expiration: "24h"
```

### Cache Configuration

```yaml
cache:
  default_ttl: "1h"
  max_size: "100MB"
  cleanup_interval: "10m"
```

### Metrics Configuration

```yaml
metrics:
  enabled: true
  port: 9090
  path: "/metrics"
```

### Logging Configuration

```yaml
logging:
  level: "info"
  format: "json"
  output: "stdout"
```

## Test Configuration

Test-specific configuration is managed in `tests/testutil/test_config.go`:

### Test Timeouts and Retries

```go
// Test timeouts and retries
DefaultTimeout        int  // Default test timeout in seconds
DefaultRetries        int  // Default number of retries
ResourceTimeout       int  // Resource operation timeout
CleanupTimeout        int  // Cleanup operation timeout
GRPCDialTimeout       int  // gRPC dial timeout
HTTPRequestTimeout    int  // HTTP request timeout
```

**Environment Variables:**
- `TEST_DEFAULT_TIMEOUT` - Default test timeout (default: 30)
- `TEST_DEFAULT_RETRIES` - Default retries (default: 3)
- `TEST_RESOURCE_TIMEOUT` - Resource timeout (default: 60)
- `TEST_CLEANUP_TIMEOUT` - Cleanup timeout (default: 10)
- `TEST_GRPC_DIAL_TIMEOUT` - gRPC dial timeout (default: 30)
- `TEST_HTTP_REQUEST_TIMEOUT` - HTTP request timeout (default: 30)

### Test User Configuration

```go
TestUserName        string  // Test user name
TestUserEmail       string  // Test user email
TestUserPassword    string  // Test user password
```

**Environment Variables:**
- `TEST_USER_NAME` - Test user name (default: testuser)
- `TEST_USER_EMAIL` - Test user email (default: test@example.com)
- `TEST_USER_PASSWORD` - Test user password (default: testpass123)

### Kubernetes Test Configuration

```go
KubernetesClusterName string  // Kubernetes cluster name
KubernetesContext     string  // Kubernetes context
KubernetesNamespace   string  // Kubernetes namespace
KubernetesConfigPath  string  // Path to kubeconfig file
```

**Environment Variables:**
- `KUBERNETES_CLUSTER_NAME` - Cluster name (default: docker-desktop)
- `KUBERNETES_CONTEXT` - Context name (default: docker-desktop)
- `KUBERNETES_NAMESPACE` - Namespace (default: default)
- `KUBECONFIG` - Path to kubeconfig (default: $HOME/.kube/config)

## Script Configuration

Script configuration is managed in `scripts/config.sh`:

### Service Endpoints

```bash
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
SPICEDB_HOST=localhost
SPICEDB_PORT=50052
VAULT_HOST=localhost
VAULT_PORT=8200
MINIO_HOST=localhost
MINIO_PORT=9000
```

### Compute Resource Ports

```bash
SLURM_CLUSTER1_SSH_PORT=2223
SLURM_CLUSTER1_SLURM_PORT=6817
SLURM_CLUSTER2_SSH_PORT=2224
SLURM_CLUSTER2_SLURM_PORT=6818
BAREMETAL_NODE1_PORT=2225
BAREMETAL_NODE2_PORT=2226
```

### Storage Resource Ports

```bash
SFTP_PORT=2222
NFS_PORT=2049
```

### Script Timeouts

```bash
DEFAULT_TIMEOUT=30
DEFAULT_RETRIES=3
HEALTH_CHECK_TIMEOUT=60
SERVICE_START_TIMEOUT=120
```

## CLI Configuration

CLI configuration is stored in `~/.airavata/config.json`:

```json
{
  "server_url": "http://localhost:8080",
  "token": "encrypted_token",
  "username": "user@example.com",
  "encrypted": true
}
```

**Environment Variables:**
- `AIRAVATA_SERVER` - Default server URL (default: http://localhost:8080)

## Docker Compose Configuration

Docker Compose uses environment variables for port configuration:

```yaml
services:
  postgres:
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
  scheduler:
    ports:
      - "${PORT:-8080}:8080"
      - "${GRPC_PORT:-50051}:50051"
```

## Configuration Validation

### Required Configuration

The following configuration is required for basic operation:

- Database DSN or connection components
- SpiceDB endpoint and token
- Vault endpoint and token

### Optional Configuration

All other configuration has sensible defaults and is optional.

### Configuration Validation

The application validates configuration on startup and will fail with clear error messages if required configuration is missing or invalid.

## Best Practices

### Development

1. Use `.env` file for local development overrides
2. Never commit `.env` file to version control
3. Use `config/default.yaml` for application defaults
4. Use environment variables for deployment-specific values

### Production

1. Use environment variables for all sensitive configuration
2. Use secrets management for tokens and passwords
3. Validate all configuration before deployment
4. Use configuration management tools (Ansible, Terraform, etc.)

### Testing

1. Use test-specific environment variables
2. Override timeouts for faster test execution
3. Use separate test databases and services
4. Mock external services when possible

## Troubleshooting

### Common Issues

1. **Port conflicts**: Check that all configured ports are available
2. **Connection timeouts**: Increase timeout values for slow networks
3. **Authentication failures**: Verify tokens and credentials
4. **Service discovery**: Ensure all service endpoints are reachable

### Debug Configuration

Enable debug logging to see configuration loading:

```bash
export LOG_LEVEL=debug
```

### Configuration Dump

Use the CLI to dump current configuration:

```bash
airavata config show
```
