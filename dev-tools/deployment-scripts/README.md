# Deployment Scripts

Simplified deployment scripts for Airavata unified server.

## Scripts

### `services_up.sh`

Starts the unified Airavata API server.

**Usage:**
```bash
./services_up.sh
```

**What it does:**
- Starts the unified Airavata server (all services in one process)
- All services (API, Orchestrator, Registry, Workflow Managers) run together

### `services_down.sh`

Stops the unified Airavata API server.

**Usage:**
```bash
./services_down.sh
```

### `distribution_update.sh`

Updates the Airavata distribution to a new version.

**Usage:**
```bash
# Place new distribution archive in current directory
./distribution_update.sh
```

**What it does:**
- Stops current server
- Extracts new distribution
- Copies configuration files from `vault/` directory
- Ready to start with new version

### `distribution_backup.sh`

Backs up the current distribution and configuration.

**Usage:**
```bash
./distribution_backup.sh
```

**What it does:**
- Creates timestamped backup directory
- Backs up distribution files
- Backs up configuration from `vault/` directory
- Backs up logs (optional)

### `docker-startup.sh`

Docker container startup script for unified server.

**Usage:**
Used by Docker containers to start Airavata.

**Environment Variables:**
- `DB_HOST`: MariaDB hostname
- `DB_PORT`: MariaDB port (default: 13306)
- `REDIS_HOST`: Redis hostname
- `REDIS_PORT`: Redis port (default: 6379)
- `AIRAVATA_HOME`: Airavata installation directory
- `AIRAVATA_CONFIG_DIR`: Configuration directory

## Unified Architecture

All Airavata services run in a single Spring Boot application:

- **Thrift API Server** (port 8930)
- **Orchestrator**
- **Registry**
- **Profile Service** (port 8962)
- **Sharing Registry** (port 7878)
- **Credential Store**
- **Workflow Managers** (Pre, Post, Parser)
- **Background Services** (Email Monitor, Realtime Monitor, etc.)

No separate service processes are needed - everything runs in one JVM.

## Configuration

Configuration files should be in the `vault/` directory:

- `airavata.properties`: Main configuration
- `airavata.sym.p12`: Keystore file
- `logback.xml`: Logging configuration

These are copied to the distribution's `conf/` directory during deployment.

## Service Management

### Using systemd (Recommended)

The Ansible deployment sets up systemd service:

```bash
systemctl start apiorch
systemctl stop apiorch
systemctl status apiorch
systemctl restart apiorch
```

### Using Scripts

```bash
# Start
./apache-airavata-server-0.21-SNAPSHOT/bin/airavata-server-start.sh -d

# Stop
./apache-airavata-server-0.21-SNAPSHOT/bin/airavata-server-stop.sh -f

# Status
./apache-airavata-server-0.21-SNAPSHOT/bin/airavata-server-status.sh
```

## Logs

All logs are written to:
- `./apache-airavata-server-0.21-SNAPSHOT/logs/airavata.log`

The unified server writes all service logs to this single file.

## Dependencies

The unified server requires:

1. **MariaDB**: Running and accessible
2. **Redis**: Running and accessible (for Dapr)
3. **Keycloak**: Running and accessible (for IAM)

All dependencies are checked on startup.
