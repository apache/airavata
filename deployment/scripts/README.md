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
- `TEMPORAL_HOST`: Temporal hostname
- `TEMPORAL_PORT`: Temporal port (default: 7233)
- `AIRAVATA_HOME`: Airavata installation directory
- `AIRAVATA_CONFIG_DIR`: Configuration directory (defaults to `AIRAVATA_HOME/conf` if not explicitly set)

## Unified Architecture

All Airavata services run in a single Spring Boot application (one JVM). See the main [README Architecture section](../../README.md#architecture) for the full API layer and internal service overview.

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
systemctl start apiserver
systemctl stop apiserver
systemctl status apiserver
systemctl restart apiserver
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
2. **Temporal**: Running and accessible (for workflows)
3. **Keycloak**: Running and accessible (for IAM)

All dependencies are checked on startup.
