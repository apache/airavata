# Apache Airavata
![image](https://github.com/user-attachments/assets/6d908819-cf5e-48d0-bbf7-f031c95adf94)

[![Build Status](https://github.com/apache/airavata/actions/workflows/maven-build.yml/badge.svg)](https://github.com/apache/airavata/actions/workflows/maven-build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Contributors](https://img.shields.io/github/contributors/apache/airavata.svg)](https://github.com/apache/airavata/graphs/contributors)

Apache Airavata is a software framework for executing and managing computational jobs on distributed computing resources including local clusters, supercomputers, national grids, academic and commercial clouds. Airavata builds on general concepts of service oriented computing, distributed messaging, and workflow composition and orchestration. Airavata bundles a server package with an API, client software development Kits and a general purpose reference UI implementation.

## Key Features

- Service-oriented architecture with distributed messaging
- Fully-managed task lifecycle (environment setup, data staging, execution, and output retrieval)
- Multi-cloud and hybrid cloud support
- Comprehensive API and SDK ecosystem
- [Reference UI Implementations](https://github.com/apache/airavata-portals)

## Using Airavata

If you're a researcher, Airavata offers several ways to streamline your workflows:

1. Submit Batch Jobs via the **Airavata Application Portal** -- [Example](https://admin.cybershuttle.org)
2. Launch Interactive Experiments through the **Airavata Research Portal** -- [Example](https://cybershuttle.org)
3. Explore and run published experiments from the **Airavata Research Catalog** -- [Example](https://cybershuttle.org/resources?resourceTypes=REPOSITORY)
4. Run interactive computational jobs directly from your IDE using **Airavata Python SDK** -- [PyPI](https://pypi.org/project/airavata-python-sdk/)

---

## Quick Start

### Prerequisites

| Requirement | Version | Verify |
|-------------|---------|--------|
| **Java JDK** | 25+ | `java --version` |
| **Apache Maven** | 3.8+ | `mvn -v` |
| **Git** | Latest | `git --version` |
| **Docker** | 20.10+ | `docker --version` |
| **Docker Compose** | 2.0+ | `docker compose version` |

**System Requirements:**
- Minimum 8GB RAM (16GB recommended)
- 10GB free disk space
- Unix-based OS (Linux/macOS) or Windows with WSL2

### Step 1: Clone and Build

```bash
git clone https://github.com/apache/airavata.git
cd airavata

# Build (without tests for speed)
mvn clean install -DskipTests
```

**Build time:** ~5-10 minutes (first build), ~2-3 minutes (subsequent builds)

### Step 2: Start Infrastructure

```bash
# Start all infrastructure services
docker compose -f .devcontainer/docker-compose.yml up -d

# Wait for services to be healthy (~60 seconds)
docker compose -f .devcontainer/docker-compose.yml ps
```

**Services started:**

| Service | Port | Purpose |
|---------|------|---------|
| MariaDB | 13306 | Database (8 catalogs) |
| Keycloak | 18080 | Identity/Access Management |
| Redis | 6379 | Dapr Pub/Sub and State Store |

**Access points:**
- Keycloak Admin: http://localhost:18080 (admin/admin)

### Step 3: Initialize Databases

```bash
cd modules/distribution

# Initialize all databases with Flyway migrations
mvn exec:java -Dexec.args="init"

# OR with clean slate (drops existing data)
mvn exec:java -Dexec.args="init --clean"
```

This creates and migrates 8 databases: `app_catalog`, `experiment_catalog`, `profile_service`, `sharing_registry`, `replica_catalog`, `workflow_catalog`, `credential_store`, `research_catalog`.

### Step 4: Start Server

```bash
cd modules/distribution

# Start in foreground (logs to console)
mvn exec:java -Dexec.args="serve --foreground"
```

**Server ports:**

| Port | Service |
|------|---------|
| 8930 | Thrift API |
| 8962 | Profile Service |
| 19908 | gRPC Agent Service |

### Step 5: Verify Installation

```bash
cd modules/distribution

# Check CLI help
mvn exec:java -Dexec.args="--help"

# Test connectivity
mvn exec:java -Dexec.args="test run"
```

---

## CLI Commands Reference

```bash
# When using Maven (development)
cd modules/distribution
mvn exec:java -Dexec.args="<command> [options]"

# When using distribution bundle or native binary
airavata <command> [options]
```

### Main Commands

| Command | Description |
|---------|-------------|
| `--help` | Show help |
| `--version` | Show version |
| `init` | Initialize databases |
| `init --clean` | Drop and recreate all databases |
| `serve` | Start as daemon (background) |
| `serve --foreground` | Start in foreground |

### Management Commands

| Command | Description |
|---------|-------------|
| `account --help` | Manage accounts |
| `project --help` | Manage projects |
| `compute --help` | Manage compute resources |
| `storage --help` | Manage storage resources |
| `group --help` | Manage groups |
| `application --help` | Manage applications |
| `service status` | Check service status |
| `test run` | Run tests |

---

## Architecture

Airavata is composed of 5 top-level services that work together to facilitate the full lifecycle of computational jobs. All services run in a unified Spring Boot application (`AiravataServer`) within a single JVM process.

![image](assets/airavata-dataflow.png)

### Unified Distribution

Airavata provides a **unified distribution bundle** that includes all services in a single Spring Boot application:

- **Thrift Mode** (default): Enables Thrift API Server along with all background services
- **REST Mode**: Enables REST Proxy instead of Thrift API, along with all background services

**Bundle Structure:**
```
airavata-0.21-SNAPSHOT/
├── bin/
│   └── airavata.sh                 # Startup script
├── lib/
│   └── airavata-0.21-SNAPSHOT.jar  # Executable JAR
├── conf/
│   ├── keystores/                  # Keystore files
│   ├── *.properties                # Config files
│   ├── *.yml                       # Config files
│   └── templates/                  # Template files
├── logs/                           # Logs directory
├── LICENSE
├── NOTICE
└── RELEASE_NOTES
```

### Services

| Service | Description |
|---------|-------------|
| **Thrift API Server** | Public-facing API on port 8930 |
| **REST API Server** | Alternative RESTful API interface |
| **Profile Service** | Manages users, tenants, compute resources (port 8962) |
| **Registry Service** | Manages metadata and application definitions |
| **Credential Store** | Secure storage of credentials |
| **Sharing Registry** | Handles permissions and sharing |
| **Orchestrator** | Constructs workflow DAGs |
| **DB Event Manager** | Syncs task events to database |

### Background Services

| Service | Purpose |
|---------|---------|
| **Controller** | Manages task state transitions using Apache Helix |
| **Participant** | Executes tasks (EnvSetup, DataStaging, JobSubmission, etc.) |
| **Email Monitor** | Monitors email for job status updates |
| **Realtime Monitor** | Listens for state-change messages |
| **Pre Workflow Manager** | Handles pre-execution phases |
| **Post Workflow Manager** | Handles post-execution phases |

![image](assets/airavata-components.png)

---

## Deployment Options

### Option 1: Using Distribution Bundle

```bash
cd distribution
tar -xzf airavata-0.21-SNAPSHOT.tar.gz
cd airavata-0.21-SNAPSHOT

# Copy configuration files from vault
cp ../../vault/airavata.properties conf/
cp ../../vault/airavata.sym.p12 conf/keystores/
cp ../../vault/*.yml conf/
cp ../../vault/logback.xml conf/

# Start all services
./bin/airavata.sh -d start      # daemon mode
./bin/airavata.sh               # foreground mode

# Stop/Restart
./bin/airavata.sh -d stop
./bin/airavata.sh -d restart
```

### Option 2: Docker (Experimental)

```bash
# Build containers
mvn docker:build -pl modules/distribution

# Start via compose
docker-compose -f modules/distribution/src/main/docker/docker-compose.yml up -d

# Stop
docker-compose -f modules/distribution/src/main/docker/docker-compose.yml down
```

### Option 3: Native Binary (GraalVM)

```bash
cd modules/distribution
mvn clean package -Pnative -DskipTests

# Binary generated in distribution/ folder
cd ../../distribution
./airavata-0.21-SNAPSHOT-{arch} init --clean
./airavata-0.21-SNAPSHOT-{arch} serve --config-dir /path/to/config
```

---

## Development

### IDE Setup

**IntelliJ IDEA:**
1. Import as Maven project
2. Set Project SDK to Java 25+
3. Enable annotation processing
4. Run configuration:
   - Main class: `org.apache.airavata.AiravataCommandLine`
   - Program arguments: `serve --foreground`
   - Working directory: `$MODULE_DIR$`
   - Environment: `AIRAVATA_HOME=$MODULE_DIR$/target/classes`

**VS Code:**
1. Install Java Extension Pack
2. Create `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [{
    "type": "java",
    "name": "Airavata Server",
    "request": "launch",
    "mainClass": "org.apache.airavata.AiravataCommandLine",
    "args": "serve --foreground",
    "cwd": "${workspaceFolder}/modules/distribution",
    "env": { "AIRAVATA_HOME": "${workspaceFolder}/modules/distribution/target/classes" }
  }]
}
```

### Running Tests

```bash
mvn test                                    # All tests
mvn test -pl modules/airavata-api           # Specific module
mvn test -Dtest=SomeTestClass               # Specific test
```

### Thrift Stubs

Airavata's Thrift IDLs live in `thrift-interface-descriptions/`. See `thrift-interface-descriptions/README.md` for stub generation.

---

## Troubleshooting

### Port already in use

```bash
lsof -i :8930
kill -9 <PID>
```

### Database connection refused

```bash
docker compose -f .devcontainer/docker-compose.yml ps db
docker compose -f .devcontainer/docker-compose.yml logs db
docker compose -f .devcontainer/docker-compose.yml restart db
```

### Maven build failures

```bash
rm -rf ~/.m2/repository/org/apache/airavata
mvn clean install -DskipTests -U
```

### Out of memory

```bash
export MAVEN_OPTS="-Xmx4g -XX:MaxMetaspaceSize=512m"
mvn clean install -DskipTests
```

### Logs Location

- **Development mode:** Console output
- **Distribution bundle:** `airavata-0.21-SNAPSHOT/logs/airavata.log`
- **Docker services:** `docker compose logs <service>`

---

## The Airavata Ecosystem

### Core Services
- [`airavata`](https://github.com/apache/airavata) – Main resource management and task orchestration middleware
- [`airavata-custos`](https://github.com/apache/airavata-custos) – Identity and access management framework
- [`airavata-mft`](https://github.com/apache/airavata-mft) – Managed file transfer services
- [`airavata-portals`](https://github.com/apache/airavata-portals) – All frontends for airavata

### Data & Metadata Services
- [`airavata-data-lake`](https://github.com/apache/airavata-data-lake) – Data lake and storage backend
- [`airavata-data-catalog`](https://github.com/apache/airavata-data-catalog) – Metadata and search services

### Documentation
- [`airavata-docs`](https://github.com/apache/airavata-docs) – Developer documentation
- [`airavata-user-docs`](https://github.com/apache/airavata-user-docs) – End-user guides
- [`airavata-site`](https://github.com/apache/airavata-site) – Project website

---

## Contributing

We welcome contributions from the community!

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

**Resources:**
- [Contributing Guidelines](http://airavata.apache.org/get-involved.html)
- [Code of Conduct](https://www.apache.org/foundation/policies/conduct.html)
- [Developer Wiki](https://cwiki.apache.org/confluence/display/AIRAVATA)

---

## Community & Support

- **User Mailing List:** [users@airavata.apache.org](mailto:users@airavata.apache.org)
- **Developer Mailing List:** [dev@airavata.apache.org](mailto:dev@airavata.apache.org)
- **All Mailing Lists:** [airavata.apache.org/mailing-list](https://airavata.apache.org/mailing-list.html)
- **GitHub Issues:** [github.com/apache/airavata/issues](https://github.com/apache/airavata/issues)

---

## License

```
Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements. See the NOTICE file distributed with this work for
additional information regarding copyright ownership.

The ASF licenses this file to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
```

See the [LICENSE](LICENSE) file for complete license details.
