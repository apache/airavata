# Deployment Guide

## Overview

This guide covers deploying the Airavata Scheduler in various environments.

## Prerequisites

- Go 1.21 or higher
- PostgreSQL 13 or higher (or MySQL 8.0+)
- Docker and Docker Compose (for containerized deployment)
- SpiceDB (for fine-grained authorization)
- OpenBao (for secure credential storage)
- Access to compute resources (SLURM cluster, Kubernetes, or bare metal servers)
- Access to storage resources (S3, NFS, or SFTP)

## Database Setup

### PostgreSQL

1. **Install PostgreSQL**
```bash
# Ubuntu/Debian
sudo apt-get install postgresql-13

# macOS
brew install postgresql@13
```

2. **Create Database**
```sql
CREATE DATABASE airavata_scheduler;
CREATE USER airavata WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE airavata_scheduler TO airavata;
```

3. **Initialize Schema**
```bash
psql -U airavata -d airavata_scheduler -f db/schema.sql
```

## Credential Management Services

The Airavata Scheduler requires SpiceDB for authorization and OpenBao for secure credential storage.

### SpiceDB Deployment

SpiceDB provides fine-grained authorization using the Zanzibar model.

#### Docker Compose (Recommended for Development)

```yaml
# docker-compose.yml
version: '3.8'
services:
  spicedb-postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: spicedb
      POSTGRES_USER: spicedb
      POSTGRES_PASSWORD: spicedb
    volumes:
      - spicedb_data:/var/lib/postgresql/data
    ports:
      - "5433:5432"

  spicedb:
    image: authzed/spicedb:latest
    command: ["serve", "--grpc-preshared-key", "somerandomkeyhere", "--datastore-engine", "postgres", "--datastore-conn-uri", "postgres://spicedb:spicedb@spicedb-postgres:5432/spicedb?sslmode=disable"]
    ports:
      - "50051:50051"
      - "50052:50052"
    depends_on:
      - spicedb-postgres
    healthcheck:
      test: ["CMD", "grpc_health_probe", "-addr=:50051"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  spicedb_data:
```

#### Kubernetes Deployment

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
          - "--grpc-preshared-key=somerandomkeyhere"
          - "--datastore-engine=postgres"
          - "--datastore-conn-uri=postgres://spicedb:spicedb@spicedb-postgres:5432/spicedb?sslmode=disable"
        ports:
        - containerPort: 50051
        - containerPort: 50052
        livenessProbe:
          exec:
            command:
            - grpc_health_probe
            - -addr=:50051
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          exec:
            command:
            - grpc_health_probe
            - -addr=:50051
          initialDelaySeconds: 5
          periodSeconds: 5
```

#### Schema Upload

After deployment, upload the authorization schema:

```bash
# Using zed CLI
docker run --rm --network host \
  -v $(pwd)/db/spicedb_schema.zed:/schema.zed \
  authzed/zed:latest schema write \
  --endpoint localhost:50051 \
  --token "somerandomkeyhere" \
  --insecure \
  /schema.zed

# Or using Makefile
make spicedb-schema-upload
```

### OpenBao Deployment

OpenBao provides secure credential storage with encryption at rest.

#### Docker Compose (Recommended for Development)

```yaml
# docker-compose.yml
version: '3.8'
services:
  openbao:
    image: hashicorp/vault:latest
    command: ["vault", "server", "-dev", "-dev-root-token-id=root-token-change-in-production"]
    ports:
      - "8200:8200"
    environment:
      VAULT_DEV_LISTEN_ADDRESS: "0.0.0.0:8200"
    volumes:
      - openbao_data:/vault/data
    healthcheck:
      test: ["CMD", "vault", "status"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  openbao_data:
```

#### Kubernetes Deployment

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
          value: "http://0.0.0.0:8200"
        livenessProbe:
          httpGet:
            path: /v1/sys/health
            port: 8200
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /v1/sys/health
            port: 8200
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

#### OpenBao Configuration

```hcl
# vault.hcl
storage "file" {
  path = "/vault/data"
}

listener "tcp" {
  address = "0.0.0.0:8200"
  tls_disable = true
}

api_addr = "http://0.0.0.0:8200"
cluster_addr = "https://0.0.0.0:8201"
ui = true
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
```

### MySQL

1. **Install MySQL**
```bash
# Ubuntu/Debian
sudo apt-get install mysql-server

# macOS
brew install mysql
```

2. **Create Database**
```sql
CREATE DATABASE airavata_scheduler;
CREATE USER 'airavata'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON airavata_scheduler.* TO 'airavata'@'localhost';
FLUSH PRIVILEGES;
```

3. **Initialize Schema**
```bash
mysql -u airavata -p airavata_scheduler < db/schema.sql
```

## Credential Management Services Setup

The Airavata Scheduler uses **OpenBao** for secure credential storage and **SpiceDB** for fine-grained authorization. These services are essential for the credential management system.

### OpenBao Setup

OpenBao provides secure credential storage with enterprise-grade encryption.

#### Docker Compose Setup

Add to your `docker-compose.yml`:

```yaml
services:
  openbao:
    image: openbao/openbao:1.15.0
    container_name: openbao
    ports:
      - "8200:8200"
    environment:
      VAULT_DEV_ROOT_TOKEN_ID: "root-token-change-in-production"
      VAULT_DEV_LISTEN_ADDRESS: "0.0.0.0:8200"
    volumes:
      - openbao_data:/vault/data
    cap_add:
      - IPC_LOCK
    restart: unless-stopped

  # For production, use a proper OpenBao configuration
  openbao-prod:
    image: openbao/openbao:1.15.0
    container_name: openbao-prod
    ports:
      - "8200:8200"
    environment:
      VAULT_ADDR: "http://0.0.0.0:8200"
    volumes:
      - openbao_data:/vault/data
      - ./config/openbao.hcl:/vault/config/openbao.hcl
    cap_add:
      - IPC_LOCK
    restart: unless-stopped
    command: ["vault", "server", "-config=/vault/config/openbao.hcl"]

volumes:
  openbao_data:
```

#### Production OpenBao Configuration

Create `config/openbao.hcl`:

```hcl
storage "file" {
  path = "/vault/data"
}

listener "tcp" {
  address     = "0.0.0.0:8200"
  tls_disable = true  # Enable TLS in production
}

api_addr = "http://0.0.0.0:8200"
cluster_addr = "http://0.0.0.0:8201"
ui = true

# Enable audit logging
audit {
  enabled = true
  path = "file"
  file_path = "/vault/logs/audit.log"
  log_raw = false
  log_requests = true
  log_response = true
}
```

#### Initialize OpenBao

1. **Start OpenBao**
```bash
# Production mode (default)
docker compose up -d openbao

# Or explicitly use production profile
docker compose --profile prod up -d openbao
```

2. **Initialize OpenBao (Development)**
```bash
# For development, OpenBao auto-initializes with root token
export VAULT_ADDR="http://localhost:8200"
export VAULT_TOKEN="root-token-change-in-production"

# Verify OpenBao is running
vault status
```

3. **Initialize OpenBao (Production)**
```bash
# Initialize OpenBao
vault operator init -key-shares=5 -key-threshold=3

# Unseal OpenBao (repeat 3 times with different keys)
vault operator unseal <unseal-key-1>
vault operator unseal <unseal-key-2>
vault operator unseal <unseal-key-3>

# Login with root token
vault auth <root-token>
```

4. **Enable Required Secrets Engines**
```bash
# Enable KV secrets engine for credential storage
vault secrets enable -path=credentials kv-v2

# Enable transit secrets engine for encryption
vault secrets enable -path=transit transit

# Create encryption key
vault write -f transit/keys/credentials
```

5. **Create Application Policy**
```bash
# Create policy for Airavata Scheduler
vault policy write airavata-scheduler - <<EOF
# Allow read/write access to credentials
path "credentials/data/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}

# Allow encryption/decryption operations
path "transit/encrypt/credentials" {
  capabilities = ["update"]
}

path "transit/decrypt/credentials" {
  capabilities = ["update"]
}

# Allow key operations
path "transit/keys/credentials" {
  capabilities = ["read", "update"]
}
EOF

# Create token for application
vault token create -policy=airavata-scheduler -ttl=24h
```

### SpiceDB Setup

SpiceDB provides fine-grained authorization using the Zanzibar model.

#### Docker Compose Setup

Add to your `docker-compose.yml`:

```yaml
services:
  spicedb:
    image: authzed/spicedb:1.30.0
    container_name: spicedb
    ports:
      - "50051:50051"
      - "50052:50052"
    environment:
      SPICEDB_GRPC_PRESHARED_KEY: "somerandomkeyhere"
      SPICEDB_LOG_LEVEL: "info"
      SPICEDB_DISPATCH_UPSTREAM_ADDR: "spicedb:50051"
    volumes:
      - spicedb_data:/var/lib/spicedb
      - ./db/spicedb_schema.zed:/schema.zed
    command: ["spicedb", "serve", "--grpc-preshared-key", "somerandomkeyhere", "--datastore-engine", "memory", "--datastore-conn-uri", "mem://", "--schema", "/schema.zed"]
    restart: unless-stopped

  # For production, use PostgreSQL backend
  spicedb-prod:
    image: authzed/spicedb:1.30.0
    container_name: spicedb-prod
    ports:
      - "50051:50051"
      - "50052:50052"
    environment:
      SPICEDB_GRPC_PRESHARED_KEY: "somerandomkeyhere"
      SPICEDB_LOG_LEVEL: "info"
      SPICEDB_DISPATCH_UPSTREAM_ADDR: "spicedb-prod:50051"
      SPICEDB_DATASTORE_ENGINE: "postgres"
      SPICEDB_DATASTORE_CONN_URI: "postgres://spicedb:password@postgres:5432/spicedb?sslmode=disable"
    volumes:
      - ./db/spicedb_schema.zed:/schema.zed
    command: ["spicedb", "serve", "--grpc-preshared-key", "somerandomkeyhere", "--datastore-engine", "postgres", "--datastore-conn-uri", "postgres://spicedb:password@postgres:5432/spicedb?sslmode=disable", "--schema", "/schema.zed"]
    depends_on:
      - postgres
    restart: unless-stopped

volumes:
  spicedb_data:
```

#### Initialize SpiceDB

1. **Start SpiceDB**
```bash
# Production mode (default)
docker compose up -d spicedb

# Or explicitly use production profile
docker compose --profile prod up -d spicedb
```

2. **Upload Schema**
```bash
# Upload the authorization schema
make spicedb-schema-upload

# Or manually upload
docker run --rm --network host \
  -v $(PWD)/db/spicedb_schema.zed:/schema.zed \
  authzed/zed:latest schema write \
  --endpoint localhost:50051 \
  --token "somerandomkeyhere" \
  --insecure \
  /schema.zed
```

3. **Verify Schema**
```bash
# Validate schema
make spicedb-validate

# Or manually validate
docker run --rm -v $(PWD)/db/spicedb_schema.zed:/schema.zed \
  authzed/zed:latest validate /schema.zed
```

4. **Test SpiceDB Connection**
```bash
# Test with zed CLI
docker run --rm --network host \
  authzed/zed:latest relationship read \
  --endpoint localhost:50051 \
  --token "somerandomkeyhere" \
  --insecure
```

### Production Deployment Considerations

#### High Availability Setup

1. **OpenBao Clustering**
```yaml
# Multi-node OpenBao cluster
services:
  openbao-1:
    image: openbao/openbao:1.15.0
    environment:
      VAULT_ADDR: "http://0.0.0.0:8200"
      VAULT_CLUSTER_ADDR: "http://openbao-1:8201"
    volumes:
      - ./config/openbao-cluster.hcl:/vault/config/openbao.hcl
    command: ["vault", "server", "-config=/vault/config/openbao.hcl"]

  openbao-2:
    image: openbao/openbao:1.15.0
    environment:
      VAULT_ADDR: "http://0.0.0.0:8200"
      VAULT_CLUSTER_ADDR: "http://openbao-2:8201"
    volumes:
      - ./config/openbao-cluster.hcl:/vault/config/openbao.hcl
    command: ["vault", "server", "-config=/vault/config/openbao.hcl"]

  openbao-3:
    image: openbao/openbao:1.15.0
    environment:
      VAULT_ADDR: "http://0.0.0.0:8200"
      VAULT_CLUSTER_ADDR: "http://openbao-3:8201"
    volumes:
      - ./config/openbao-cluster.hcl:/vault/config/openbao.hcl
    command: ["vault", "server", "-config=/vault/config/openbao.hcl"]
```

2. **SpiceDB Clustering**
```yaml
# Multi-node SpiceDB cluster
services:
  spicedb-1:
    image: authzed/spicedb:1.30.0
    environment:
      SPICEDB_GRPC_PRESHARED_KEY: "somerandomkeyhere"
      SPICEDB_DISPATCH_UPSTREAM_ADDR: "spicedb-1:50051"
      SPICEDB_DATASTORE_ENGINE: "postgres"
      SPICEDB_DATASTORE_CONN_URI: "postgres://spicedb:password@postgres:5432/spicedb?sslmode=disable"
    command: ["spicedb", "serve", "--grpc-preshared-key", "somerandomkeyhere", "--datastore-engine", "postgres", "--datastore-conn-uri", "postgres://spicedb:password@postgres:5432/spicedb?sslmode=disable", "--schema", "/schema.zed"]

  spicedb-2:
    image: authzed/spicedb:1.30.0
    environment:
      SPICEDB_GRPC_PRESHARED_KEY: "somerandomkeyhere"
      SPICEDB_DISPATCH_UPSTREAM_ADDR: "spicedb-2:50051"
      SPICEDB_DATASTORE_ENGINE: "postgres"
      SPICEDB_DATASTORE_CONN_URI: "postgres://spicedb:password@postgres:5432/spicedb?sslmode=disable"
    command: ["spicedb", "serve", "--grpc-preshared-key", "somerandomkeyhere", "--datastore-engine", "postgres", "--datastore-conn-uri", "postgres://spicedb:password@postgres:5432/spicedb?sslmode=disable", "--schema", "/schema.zed"]
```

#### Security Configuration

1. **Enable TLS for OpenBao**
```hcl
# config/openbao.hcl
listener "tcp" {
  address     = "0.0.0.0:8200"
  tls_cert_file = "/vault/certs/vault.crt"
  tls_key_file  = "/vault/certs/vault.key"
}
```

2. **Enable TLS for SpiceDB**
```bash
# Generate certificates
openssl req -x509 -newkey rsa:4096 -keyout spicedb.key -out spicedb.crt -days 365 -nodes

# Update SpiceDB configuration
command: ["spicedb", "serve", "--grpc-preshared-key", "somerandomkeyhere", "--tls-cert-path", "/certs/spicedb.crt", "--tls-key-path", "/certs/spicedb.key"]
```

3. **Network Security**
```yaml
# Restrict network access
networks:
  internal:
    driver: bridge
    internal: true

services:
  openbao:
    networks:
      - internal
    ports:
      - "127.0.0.1:8200:8200"  # Only bind to localhost

  spicedb:
    networks:
      - internal
    ports:
      - "127.0.0.1:50051:50051"  # Only bind to localhost
```

#### Monitoring and Backup

1. **Health Checks**
```bash
# OpenBao health
curl -s http://localhost:8200/v1/sys/health | jq

# SpiceDB health
curl -s http://localhost:50052/healthz
```

2. **Backup Procedures**
```bash
# Backup OpenBao data
docker exec openbao vault operator raft snapshot save /vault/backup.snap

# Backup SpiceDB data (if using file storage)
docker exec spicedb tar -czf /backup/spicedb-backup.tar.gz /var/lib/spicedb
```

3. **Monitoring Setup**
```yaml
# Add to docker-compose.yml
services:
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./config/prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
```

## Environment Variables

Create a `.env` file or set environment variables:

```bash
# Database Configuration
DATABASE_URL="user:password@tcp(localhost:3306)/airavata_scheduler?parseTime=true"
# For PostgreSQL:
# DATABASE_URL="postgres://user:password@localhost:5432/airavata_scheduler?sslmode=disable"

# Server Configuration
SERVER_PORT="8080"
WORKER_INTERVAL="30s"

# JWT Configuration
JWT_SECRET_KEY="your-secret-key-change-this-in-production"
JWT_ACCESS_TOKEN_TTL="1h"
JWT_REFRESH_TOKEN_TTL="168h"

# Security Configuration
RATE_LIMIT_ENABLED="true"
MAX_REQUESTS_PER_MINUTE="100"
REQUIRE_HTTPS="false"  # Set to true in production

# Credential Management Configuration
# OpenBao Configuration
OPENBAO_ADDR="http://localhost:8200"
OPENBAO_TOKEN="root-token-change-in-production"
OPENBAO_CREDENTIALS_PATH="credentials"
OPENBAO_TRANSIT_PATH="transit"
OPENBAO_TRANSIT_KEY="credentials"

# SpiceDB Configuration
SPICEDB_ENDPOINT="localhost:50051"
SPICEDB_TOKEN="somerandomkeyhere"
SPICEDB_INSECURE="true"  # Set to false in production with TLS

# Worker Configuration (for worker nodes)
WORKER_ID="worker-001"
COMPUTE_ID="compute-resource-id"
API_URL="http://scheduler-api:8080"
OUTPUT_DIR="/data/output"
POLL_INTERVAL="30s"
```

## Build from Source

```bash
# Clone repository
git clone https://github.com/apache/airavata/scheduler.git
cd airavata-scheduler

# Build scheduler daemon
go build -o bin/scheduler ./cmd/scheduler

# Build worker daemon
go build -o bin/worker ./cmd/worker
```

## Running the Scheduler

### Standalone Mode

```bash
# Start scheduler server
./bin/scheduler server

# Or start as daemon (server + background jobs)
./bin/scheduler daemon

# Or start both (server + daemon)
./bin/scheduler both
```

### Systemd Service

Create `/etc/systemd/system/airavata-scheduler.service`:

```ini
[Unit]
Description=Airavata Scheduler
After=network.target postgresql.service

[Service]
Type=simple
User=airavata
WorkingDirectory=/opt/airavata-scheduler
EnvironmentFile=/opt/airavata-scheduler/.env
ExecStart=/opt/airavata-scheduler/bin/scheduler both
Restart=on-failure
RestartSec=5s

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable airavata-scheduler
sudo systemctl start airavata-scheduler
sudo systemctl status airavata-scheduler
```

## Running Workers

### On Compute Nodes

```bash
export WORKER_ID="worker-$(hostname)"
export COMPUTE_ID="slurm-cluster-1"
export API_URL="http://scheduler.example.com:8080"
export OUTPUT_DIR="/scratch/airavata-output"

./bin/worker
```

### Systemd Service for Workers

Create `/etc/systemd/system/airavata-worker.service`:

```ini
[Unit]
Description=Airavata Worker
After=network.target

[Service]
Type=simple
User=airavata
WorkingDirectory=/opt/airavata-scheduler
EnvironmentFile=/opt/airavata-scheduler/worker.env
ExecStart=/opt/airavata-scheduler/bin/worker
Restart=on-failure
RestartSec=10s

[Install]
WantedBy=multi-user.target
```

## Docker Deployment

### Using Docker Compose

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: airavata_scheduler
      POSTGRES_USER: airavata
      POSTGRES_PASSWORD: secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./db/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    ports:
      - "5432:5432"

  scheduler:
    build: .
    command: both
    environment:
      DATABASE_URL: "postgres://airavata:secure_password@postgres:5432/airavata_scheduler?sslmode=disable"
      SERVER_PORT: "8080"
      JWT_SECRET_KEY: "change-this-in-production"
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    restart: unless-stopped

  worker:
    build: .
    command: worker
    environment:
      WORKER_ID: "worker-docker-1"
      COMPUTE_ID: "local-compute"
      API_URL: "http://scheduler:8080"
      OUTPUT_DIR: "/data/output"
      DATABASE_URL: "postgres://airavata:secure_password@postgres:5432/airavata_scheduler?sslmode=disable"
    volumes:
      - worker_output:/data/output
    depends_on:
      - scheduler
    restart: unless-stopped

volumes:
  postgres_data:
  worker_output:
```

Start services:
```bash
# Production mode (default)
docker compose up -d

# Or explicitly use production profile
docker compose --profile prod up -d
```

### Dockerfile

```dockerfile
FROM golang:1.21-alpine AS builder

WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN go build -o /bin/scheduler ./cmd/scheduler
RUN go build -o /bin/worker ./cmd/worker

FROM alpine:latest
RUN apk --no-cache add ca-certificates

COPY --from=builder /bin/scheduler /bin/scheduler
COPY --from=builder /bin/worker /bin/worker

EXPOSE 8080

ENTRYPOINT ["/bin/scheduler"]
CMD ["both"]
```

## Kubernetes Deployment

### Prerequisites

For Kubernetes deployments, the following components are required:

#### Metrics Server

The Airavata Scheduler requires the Kubernetes metrics-server for worker performance monitoring. Install it using:

```bash
# Install metrics-server
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Verify installation
kubectl get deployment metrics-server -n kube-system
```

**Note:** If metrics-server is not available, worker metrics will show as 0% but the system will continue to function normally.

### Scheduler Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: airavata-scheduler
spec:
  replicas: 2
  selector:
    matchLabels:
      app: airavata-scheduler
  template:
    metadata:
      labels:
        app: airavata-scheduler
    spec:
      containers:
      - name: scheduler
        image: airavata/scheduler:latest
        ports:
        - containerPort: 8080
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: scheduler-secrets
              key: database-url
        - name: JWT_SECRET_KEY
          valueFrom:
            secretKeyRef:
              name: scheduler-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "256Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "2"
```

### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: airavata-scheduler
spec:
  selector:
    app: airavata-scheduler
  ports:
  - port: 8080
    targetPort: 8080
  type: LoadBalancer
```

## Production Considerations

### Security

1. **Use HTTPS**
   - Enable TLS/SSL for all API endpoints
   - Set `REQUIRE_HTTPS=true`
   - Use proper certificates (Let's Encrypt, etc.)

2. **Secure Secrets**
   - Use secret management (Vault, AWS Secrets Manager, etc.)
   - Rotate JWT secrets regularly
   - Never commit secrets to version control

3. **Database Security**
   - Use strong passwords
   - Enable SSL for database connections
   - Restrict database access by IP

4. **Network Security**
   - Use firewalls to restrict access
   - Deploy behind reverse proxy (Nginx, Traefik)
   - Enable rate limiting

### Performance

1. **Database Optimization**
   - Enable connection pooling
   - Add appropriate indexes
   - Regular VACUUM (PostgreSQL) or OPTIMIZE (MySQL)

2. **Horizontal Scaling**
   - Run multiple scheduler instances behind load balancer
   - Scale workers based on workload
   - Use Redis for shared state (optional)

3. **Monitoring**
   - Set up Prometheus metrics
   - Configure log aggregation (ELK, Loki)
   - Set up alerts for failures

### Backup and Recovery

1. **Database Backups**
```bash
# PostgreSQL
pg_dump -U airavata airavata_scheduler > backup.sql

# MySQL
mysqldump -u airavata -p airavata_scheduler > backup.sql
```

2. **Restore**
```bash
# PostgreSQL
psql -U airavata -d airavata_scheduler < backup.sql

# MySQL
mysql -u airavata -p airavata_scheduler < backup.sql
```

3. **Automated Backups**
   - Schedule daily backups via cron
   - Store backups in S3 or remote location
   - Test restore procedures regularly

## Monitoring

### Health Checks

```bash
# API health
curl http://localhost:8080/api/v1/health

# Database connectivity
curl http://localhost:8080/api/v1/health/db
```

### Logs

```bash
# View scheduler logs
journalctl -u airavata-scheduler -f

# View worker logs
journalctl -u airavata-worker -f

# Docker logs
docker compose logs -f scheduler
docker compose logs -f worker
```

### Metrics

The scheduler exposes Prometheus metrics at `/metrics`:

```bash
curl http://localhost:8080/metrics
```

Key metrics:
- `scheduler_tasks_total` - Total tasks processed
- `scheduler_tasks_duration_seconds` - Task execution duration
- `scheduler_workers_active` - Active workers
- `scheduler_api_requests_total` - API request count

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check DATABASE_URL format
   - Verify database is running
   - Check network connectivity

2. **Worker Not Receiving Tasks**
   - Verify API_URL is correct
   - Check worker is registered
   - Verify network connectivity

3. **Tasks Stuck in Queue**
   - Check worker availability
   - Verify compute resources are accessible
   - Review scheduler logs

### Debug Mode

Enable debug logging:
```bash
export LOG_LEVEL=debug
./bin/scheduler both
```

## Upgrade Guide

1. **Backup Database**
2. **Stop Services**
3. **Deploy New Binaries**
4. **Start Services**
5. **Verify Health**

```bash
# Example upgrade
systemctl stop airavata-scheduler
pg_dump -U airavata airavata_scheduler > backup_$(date +%Y%m%d).sql
cp bin/scheduler /opt/airavata-scheduler/bin/
systemctl start airavata-scheduler
systemctl status airavata-scheduler
```

**Note**: The system uses a single comprehensive schema file (`db/schema.sql`) with all production features ready for immediate deployment.

