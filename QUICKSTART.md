# Apache Airavata Quick Start Guide

This guide provides step-by-step instructions to get Airavata running from a fresh git clone.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Clone and Build](#clone-and-build)
3. [Start Infrastructure](#start-infrastructure)
4. [Initialize Databases](#initialize-databases)
5. [Start Airavata Server](#start-airavata-server)
6. [Verify Installation](#verify-installation)
7. [CLI Commands Reference](#cli-commands-reference)
8. [Development Mode](#development-mode)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before you begin, ensure you have the following installed:

| Requirement | Version | Installation | Verify |
|-------------|---------|--------------|--------|
| **Java JDK** | 25+ | [Download](https://jdk.java.net/) | `java --version` |
| **Apache Maven** | 3.8+ | [Download](https://maven.apache.org/download.cgi) | `mvn -v` |
| **Git** | Latest | [Download](https://git-scm.com/) | `git --version` |
| **Docker** | 20.10+ | [Download](https://www.docker.com/get-started) | `docker --version` |
| **Docker Compose** | 2.0+ | Included with Docker Desktop | `docker compose version` |

**System Requirements:**
- Minimum 8GB RAM (16GB recommended)
- 10GB free disk space
- Unix-based OS (Linux/macOS) or Windows with WSL2

---

## Clone and Build

### Step 1: Clone the Repository

```bash
git clone https://github.com/apache/airavata.git
cd airavata
```

### Step 2: Build the Project

```bash
# Full build with tests (recommended for first build)
mvn clean install

# OR faster build without tests
mvn clean install -DskipTests
```

**Build time:** ~5-10 minutes (first build), ~2-3 minutes (subsequent builds)

**Build artifacts:**
- `modules/distribution/target/airavata-0.21-SNAPSHOT.jar` - Executable JAR
- `distribution/airavata-0.21-SNAPSHOT.tar.gz` - Distribution bundle

---

## Start Infrastructure

Airavata requires several external services. Use Docker Compose to start them:

### Step 3: Start Docker Services

```bash
# Start all infrastructure services
docker compose -f .devcontainer/docker-compose.yml up -d

# Wait for services to be healthy (about 60 seconds)
docker compose -f .devcontainer/docker-compose.yml ps
```

**Services started:**

| Service | Port | Purpose |
|---------|------|---------|
| MariaDB | 13306 | Database (8 catalogs) |
| Keycloak | 18080 | Identity/Access Management |
| RabbitMQ | 5672, 15672 | Messaging (AMQP) |
| Kafka | 9092 | Event streaming |
| Zookeeper | 2181 | Coordination |
| Adminer | 18088 | Database UI |

**Verify services are running:**

```bash
# Check service health
docker compose -f .devcontainer/docker-compose.yml ps

# Expected output: All services showing "healthy" or "running"
```

**Access points:**
- Keycloak Admin: http://localhost:18080 (admin/admin)
- RabbitMQ Management: http://localhost:15672 (guest/guest)
- Database Admin (Adminer): http://localhost:18088

---

## Initialize Databases

### Step 4: Run Database Migrations

```bash
# From the project root
cd modules/distribution

# Initialize all databases with Flyway migrations
mvn exec:java -Dexec.args="init"

# OR with clean slate (drops existing data)
mvn exec:java -Dexec.args="init --clean"
```

This creates and migrates 8 databases:
- `app_catalog` - Application definitions
- `experiment_catalog` - Experiments and jobs
- `profile_service` - User/tenant profiles
- `sharing_registry` - Permissions
- `replica_catalog` - Data replicas
- `workflow_catalog` - Workflows
- `credential_store` - Credentials (encrypted)
- `research_catalog` - Research artifacts

---

## Start Airavata Server

### Step 5: Start the Server

**Option A: Using Maven exec:java (Development)**

```bash
cd modules/distribution

# Start in foreground (logs to console)
mvn exec:java -Dexec.args="serve --foreground"
```

**Option B: Using JAR directly**

```bash
cd modules/distribution

# Build the JAR if not already built
mvn package -DskipTests

# Set AIRAVATA_HOME and start
export AIRAVATA_HOME=$(pwd)/target/classes
java -jar target/airavata-0.21-SNAPSHOT.jar serve --foreground
```

**Option C: Using Distribution Bundle**

```bash
# Extract the distribution
cd distribution
tar -xzf airavata-0.21-SNAPSHOT.tar.gz
cd airavata-0.21-SNAPSHOT

# Start as daemon
./bin/airavata.sh -d start

# OR start in foreground
./bin/airavata.sh
```

**Server ports:**

| Port | Service |
|------|---------|
| 8930 | Thrift API |
| 8962 | Profile Service |
| 19908 | gRPC Agent Service |

---

## Verify Installation

### Step 6: Verify Services

```bash
# Check CLI help
cd modules/distribution
mvn exec:java -Dexec.args="--help"

# List available commands
mvn exec:java -Dexec.args="service status"

# Test connectivity (if Thrift is enabled)
mvn exec:java -Dexec.args="test run"
```

**Expected output from `--help`:**

```
Usage: airavata [-hV] [COMMAND]
Airavata CLI for Server Startup and Configuration Management
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  init         Initialize all Airavata databases using Flyway migrations
  account      Manage Airavata accounts
  project      Manage Airavata projects
  storage      Manage storage resources
  compute      Manage compute resources
  group        Manage groups
  application  Manage applications
  serve        Start all Airavata services
  service      Manage Airavata services
  test         Test Airavata functionality
```

---

## CLI Commands Reference

### Main Commands

```bash
# Show help
airavata --help
airavata -h

# Show version
airavata --version

# Initialize databases
airavata init                    # Run migrations
airavata init --clean            # Drop and recreate all databases

# Start server
airavata serve                   # Start as daemon (background)
airavata serve --foreground      # Start in foreground (for debugging)
```

### Management Commands

```bash
# Account management
airavata account --help
airavata account init --username=admin --password=pass --gateway=default

# Project management
airavata project --help
airavata project list --gateway=default

# Compute resource management
airavata compute --help
airavata compute list

# Storage resource management
airavata storage --help
airavata storage list

# Group management
airavata group --help
airavata group list --gateway=default

# Application management
airavata application --help
airavata application list --gateway=default

# Service management
airavata service --help
airavata service status

# Testing
airavata test --help
airavata test run
```

### Running with Maven

When developing, use Maven to run CLI commands:

```bash
cd modules/distribution

# General pattern
mvn exec:java -Dexec.args="<command> [options]"

# Examples
mvn exec:java -Dexec.args="--help"
mvn exec:java -Dexec.args="init --clean"
mvn exec:java -Dexec.args="serve --foreground"
mvn exec:java -Dexec.args="account --help"
```

---

## Development Mode

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for a specific module
mvn test -pl modules/airavata-api

# Run a specific test class
mvn test -pl modules/airavata-api -Dtest=RegistryServiceIntegrationTest

# Run with verbose output
mvn test -X
```

### IDE Setup

**IntelliJ IDEA:**

1. Import as Maven project
2. Set Project SDK to Java 25+
3. Enable annotation processing (for MapStruct, Lombok)
4. Run/Debug configurations:
   - Main class: `org.apache.airavata.AiravataCommandLine`
   - Program arguments: `serve --foreground`
   - Working directory: `$MODULE_DIR$`
   - Environment: `AIRAVATA_HOME=$MODULE_DIR$/target/classes`

**VS Code:**

1. Install Java Extension Pack
2. Open project folder
3. Create `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Airavata Server",
      "request": "launch",
      "mainClass": "org.apache.airavata.AiravataCommandLine",
      "args": "serve --foreground",
      "cwd": "${workspaceFolder}/modules/distribution",
      "env": {
        "AIRAVATA_HOME": "${workspaceFolder}/modules/distribution/target/classes"
      }
    }
  ]
}
```

### Hot Reload (Development)

For faster development iteration:

```bash
# In terminal 1: Watch for changes and recompile
mvn compile -pl modules/airavata-api,modules/distribution -am -q -DskipTests

# In terminal 2: Run server (restart after recompile)
cd modules/distribution && mvn exec:java -Dexec.args="serve --foreground"
```

---

## Troubleshooting

### Common Issues

**1. Port already in use**

```bash
# Find process using port
lsof -i :8930

# Kill the process
kill -9 <PID>

# Or stop Docker services first
docker compose -f .devcontainer/docker-compose.yml down
```

**2. Database connection refused**

```bash
# Verify MariaDB is running
docker compose -f .devcontainer/docker-compose.yml ps db

# Check logs
docker compose -f .devcontainer/docker-compose.yml logs db

# Restart MariaDB
docker compose -f .devcontainer/docker-compose.yml restart db
```

**3. Maven build failures**

```bash
# Clear Maven cache
rm -rf ~/.m2/repository/org/apache/airavata

# Rebuild
mvn clean install -DskipTests -U
```

**4. Out of memory errors**

```bash
# Increase Maven memory
export MAVEN_OPTS="-Xmx4g -XX:MaxMetaspaceSize=512m"

# Rebuild
mvn clean install -DskipTests
```

**5. Docker not starting**

```bash
# Check Docker daemon
docker info

# Restart Docker Desktop (macOS/Windows)
# Or restart Docker service (Linux)
sudo systemctl restart docker
```

**6. Keycloak not healthy**

```bash
# Check Keycloak logs
docker compose -f .devcontainer/docker-compose.yml logs keycloak

# Keycloak needs ~60s to start
# Wait and check health again
docker compose -f .devcontainer/docker-compose.yml ps keycloak
```

### Logs Location

**Development mode (exec:java):**
- Console output

**Distribution bundle:**
- `airavata-0.21-SNAPSHOT/logs/airavata.log`

**Docker services:**
```bash
docker compose -f .devcontainer/docker-compose.yml logs <service>
```

### Getting Help

- **Documentation:** https://airavata.apache.org/
- **Developer Wiki:** https://cwiki.apache.org/confluence/display/AIRAVATA
- **Mailing List:** dev@airavata.apache.org
- **GitHub Issues:** https://github.com/apache/airavata/issues

---

## Next Steps

After successfully starting Airavata:

1. **Configure a Gateway:** Create a tenant/gateway for your organization
2. **Register Compute Resources:** Add HPC clusters or cloud resources
3. **Deploy Applications:** Register and deploy scientific applications
4. **Create Users:** Set up user accounts and permissions
5. **Run Experiments:** Submit computational jobs

See the full [README.md](README.md) for detailed architecture and configuration options.
