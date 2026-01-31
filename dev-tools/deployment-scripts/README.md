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
- Copies configuration files from `conf/` directory
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
- Backs up configuration from `conf/` directory
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
- `AIRAVATA_CONFIG_DIR`: Configuration directory (defaults to `AIRAVATA_HOME/conf` if not explicitly set)

## Unified Architecture

All Airavata services run in a single Spring Boot application with the following server ports:

- **Thrift Server** (port 8930) - Thrift Endpoints for Airavata API functions
- **HTTP Server** (port 8080):
  - Airavata API - HTTP Endpoints for Airavata API functions
  - File API - HTTP Endpoints for file upload/download
  - Agent API - HTTP Endpoints for interactive job contexts
  - Research API - HTTP Endpoints for use by research hub
- **gRPC Server** (port 9090) - For airavata binaries to open persistent channels with airavata APIs
- **Dapr gRPC** (port 50001) - Sidecar for pub/sub, state, and workflow execution

**Internal Services** (accessible via the above servers, not separate processes):
- **Orchestrator** - Constructs workflow DAGs
- **Registry** - Manages metadata
- **Profile Service** - Manages users, tenants, compute resources
- **Sharing Registry** - Handles permissions
- **Credential Store** - Secure credential storage
- **Workflow Managers** - Pre, Post, Parser (internal components)
- **Background Services** - Email Monitor, Status Change Handler (status-change-topic), etc. (internal components)

No separate service processes are needed - everything runs in one JVM.

## Configuration

Configuration files should be in the `conf/` directory (mounted at `/opt/apache-airavata/conf/`):

- `airavata.properties`: Main configuration (located at `/opt/apache-airavata/conf/airavata.properties`)
- `airavata.sym.p12`: Keystore file (located at `/opt/apache-airavata/conf/airavata.sym.p12`)
- `logback.xml`: Logging configuration (located at `/opt/apache-airavata/conf/logback.xml`)

**Note:** The Agent Tunnel Server configuration properties (`airavata.services.agent.tunnelserver.*`) point to a **remote server location**, not a service started by Airavata. These properties are passed to agents via gRPC messages to tell them where to connect for TCP tunneling.

**Installation Path**: `/opt/apache-airavata` (set as `AIRAVATA_HOME` environment variable)
**Configuration Path**: `/opt/apache-airavata/conf` (defaults to `AIRAVATA_HOME/conf` if `AIRAVATA_CONFIG_DIR` is not explicitly set)
**Logs Path**: `/opt/apache-airavata/logs`

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

The distribution tarball is named `airavata-0.21-SNAPSHOT`; the single launcher is `bin/airavata.sh`:

```bash
# Start (daemon)
./airavata-0.21-SNAPSHOT/bin/airavata.sh -d start

# Stop
./airavata-0.21-SNAPSHOT/bin/airavata.sh -d stop

# Restart
./airavata-0.21-SNAPSHOT/bin/airavata.sh -d restart
```

Or run in foreground: `./airavata-0.21-SNAPSHOT/bin/airavata.sh` (no `-d start`).

## Logs

All logs are written to:
- `./airavata-0.21-SNAPSHOT/logs/airavata.log`

The unified server writes all service logs to this single file.

## Dependencies

The unified server requires:

1. **MariaDB**: Running and accessible
2. **Redis**: Running and accessible (for Dapr)
3. **Keycloak**: Running and accessible (for IAM)

All dependencies are checked on startup.
