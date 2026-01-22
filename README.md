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
# Start core infrastructure services
docker compose -f .devcontainer/docker-compose.yml up -d

# Wait for services to be healthy (~60 seconds)
docker compose -f .devcontainer/docker-compose.yml ps

# (Optional) Start with test infrastructure (SLURM + SFTP)
docker compose -f .devcontainer/docker-compose.yml --profile test up -d
```

**Core Services (always started):**

| Service | Port | Version | Purpose |
|---------|------|---------|---------|
| MariaDB | 13306 | 10.4.13 | Unified Airavata Database |
| Keycloak | 18080 | 25.0 | Identity/Access Management |
| Redis | 6379 | 7 | Dapr Pub/Sub and State Store |

**Test Profile Services (optional, `--profile test`):**

| Service | Port | Purpose |
|---------|------|---------|
| SLURM Cluster | 10022 (SSH), 6817-6818 | Test batch job submission |
| SFTP Server | 10023 | Test file transfers |

Test credentials: `testuser` / `testpass`

**Access points:**
- Keycloak Admin: http://localhost:18080 (admin/admin)
- Keycloak Realm: http://localhost:18080/realms/default

### Step 3: Initialize Databases

```bash
cd modules/distribution

# Initialize all databases with Flyway migrations
mvn exec:java -Dexec.args="init"

# OR with clean slate (drops existing data)
mvn exec:java -Dexec.args="init --clean"
```

This initializes the unified `airavata` database with all required tables.

### Step 4: Start Server

```bash
cd modules/distribution

# Start in foreground (logs to console)
mvn exec:java -Dexec.args="serve --foreground"
```

**Server ports:**

| Port | Service |
|------|---------|
| 8930 | Thrift Endpoints for Airavata API functions |

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

> For detailed architecture documentation including Dapr workflows, activities, state machines, and package structure, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

![image](assets/airavata-dataflow.png)

### Unified Distribution

Airavata provides a **unified distribution bundle** that includes all services in a single Spring Boot application:

- **Thrift Mode** (default): Enables Thrift Server (Thrift Endpoints for Airavata API functions) along with all background services
- **HTTP Mode**: Enables Airavata API (HTTP protocol) along with all background services. Both Thrift and HTTP modes can be enabled simultaneously to serve the same core API functionalities through different protocols.

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

All services run in a unified Spring Boot application with the following server ports:

| Server | Port | Description |
|--------|------|-------------|
| **Thrift Server** | 8930 | Thrift Endpoints for Airavata API functions |
| **HTTP Server** | 8080 | HTTP Endpoints for Airavata API functions, File API, Agent API, and Research API |
| **gRPC Server** | 9090 | For airavata binaries to open persistent channels with airavata APIs |
| **Dapr gRPC** | 50001 | Sidecar for pub/sub, state, and workflow execution |

**Internal Services** (not separate servers, accessible via the above ports):
- **Profile Service** - Manages users, tenants, compute resources (accessible via Thrift Server on port 8930)
- **Registry Service** - Manages metadata and application definitions (accessible via Thrift Server on port 8930)
- **Credential Store** - Secure storage of credentials (accessible via Thrift Server on port 8930)
- **Sharing Registry** - Handles permissions and sharing (accessible via Thrift Server on port 8930)
- **Orchestrator** - Constructs workflow DAGs (accessible via Thrift Server on port 8930)
- **DB Event Manager** - Syncs task events to database (internal component)

**Note:** Profile Service, Registry Service, Credential Store, Sharing Registry, and Orchestrator are **not separate servers**. They are multiplexed services accessible through the unified Thrift Server on port 8930 using service name prefixes (e.g., `ProfileService.UserProfileService`, `SharingRegistryService`).

### Background Services

| Service | Purpose |
|---------|---------|
| **Dapr Workflows** | Manages task state transitions and workflow orchestration (ProcessPreWorkflow, ProcessPostWorkflow, ProcessCancelWorkflow, ParsingWorkflow) |
| **Dapr Activities** | Executes tasks as Dapr activities (EnvSetup, InputDataStaging, JobSubmission, OutputDataStaging, etc.) |
| **Dapr Pub/Sub** | Messaging for experiment/process events and status updates (backed by Redis) |
| **Dapr State Store** | Persistent state management for workflows and process state (backed by Redis) |
| **Email Monitor** | Monitors email for job status updates |
| **Realtime Monitor** | Listens for state-change messages via Dapr Pub/Sub |
| **Pre Workflow Manager** | Handles pre-execution phases (schedules ProcessPreWorkflow via Dapr) |
| **Post Workflow Manager** | Handles post-execution phases (schedules ProcessPostWorkflow via Dapr) |
| **Parser Workflow Manager** | Handles data parsing (schedules ParsingWorkflow via Dapr) |

![image](assets/airavata-components.png)

---

## Standard Configuration

### Server Ports

All Airavata services use the following standardized ports:

| Server | Port | Configuration Property | Description |
|--------|------|------------------------|-------------|
| **Thrift Server** | 8930 | `airavata.services.thrift.server.port` | Thrift Endpoints for Airavata API functions |
| **HTTP Server** (unified) | 8080 | `airavata.services.http.server.port` | HTTP Endpoints for Airavata API functions, File API, Agent API, and Research API |
| **gRPC Server** (unified) | 9090 | `airavata.services.grpc.server.port` | For airavata binaries to open persistent channels with airavata APIs |
| **Dapr gRPC** | 50001 | `airavata.dapr.grpc-port` | Sidecar for pub/sub, state, and workflow execution |

### Agent Tunnel Server Configuration

The Agent Tunnel Server is **not a service started by Airavata**. It is a configuration property that points to a **remote server location** where agents connect for TCP tunneling. The Airavata server only reads these properties and passes them to agents via gRPC messages.

| Configuration Property | Description |
|------------------------|-------------|
| `airavata.services.agent.tunnelserver.host` | Remote tunnel server hostname |
| `airavata.services.agent.tunnelserver.port` | Remote tunnel server port (typically 17000) |
| `airavata.services.agent.tunnelserver.url` | Remote tunnel server API URL |
| `airavata.services.agent.tunnelserver.token` | Authentication token for tunnel server |

### Standard Paths

| Path | Environment Variable | Description |
|------|---------------------|-------------|
| `/opt/apache-airavata` | `AIRAVATA_HOME` | Installation directory |
| `/opt/apache-airavata/conf` | `AIRAVATA_CONFIG_DIR` | Configuration directory (defaults to `AIRAVATA_HOME/conf` if not explicitly set) |
| `/opt/apache-airavata/logs` | - | Logs directory |

### Configuration Files

| File | Location | Description |
|------|----------|-------------|
| `airavata.properties` | `/opt/apache-airavata/conf/airavata.properties` or `conf/application.properties` | Main configuration file |
| `airavata.sym.p12` | `/opt/apache-airavata/conf/airavata.sym.p12` or `conf/keystores/airavata.sym.p12` | Keystore file |
| `logback.xml` | `/opt/apache-airavata/conf/logback.xml` or `conf/logback.xml` | Logging configuration |

**Note:** Spring Boot also recognizes `application.properties` as the configuration file name. All deployments use `/opt/apache-airavata/conf/` (or `conf/` in distribution bundle) as the configuration directory.

---

## Deployment Options

### Option 1: Using Distribution Bundle

```bash
cd distribution
tar -xzf airavata-0.21-SNAPSHOT.tar.gz
cd airavata-0.21-SNAPSHOT

# Copy configuration files from conf directory
cp ../../conf/airavata.properties conf/
cp ../../conf/airavata.sym.p12 conf/keystores/
cp ../../conf/*.yml conf/
cp ../../conf/logback.xml conf/

# Start all services
./bin/airavata.sh -d start      # daemon mode
./bin/airavata.sh               # foreground mode

# Stop/Restart
./bin/airavata.sh -d stop
./bin/airavata.sh -d restart
```

**Standard Paths:**
- Installation directory: `/opt/apache-airavata` (set as `AIRAVATA_HOME` environment variable)
- Configuration directory: `/opt/apache-airavata/conf` (defaults to `AIRAVATA_HOME/conf` if `AIRAVATA_CONFIG_DIR` is not explicitly set)
- Logs directory: `/opt/apache-airavata/logs`
- Configuration file: `/opt/apache-airavata/conf/airavata.properties` (or `conf/application.properties` in distribution bundle)

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

### Option 4: Ansible Deployment (Production)

For production deployments, use the consolidated Ansible playbook:

```bash
cd dev-tools/ansible

# Copy and customize inventory
cp -r inventories/template inventories/my-deployment
# Edit inventories/my-deployment/hosts and group_vars/all/vars.yml

# Deploy everything
ansible-playbook -i inventories/my-deployment deploy.yml

# Or deploy components individually using tags
ansible-playbook -i inventories/my-deployment deploy.yml --tags database
ansible-playbook -i inventories/my-deployment deploy.yml --tags redis
ansible-playbook -i inventories/my-deployment deploy.yml --tags apiserver
ansible-playbook -i inventories/my-deployment deploy.yml --tags keycloak
```

See [dev-tools/ansible/README.md](dev-tools/ansible/README.md) for detailed Ansible documentation.

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

### Powered by Airavata

Research software and projects using Airavata:

- [Cybershuttle](https://cybershuttle.org/) – Research computing platform
- [Bio-realistic multiscale simulations of cortical circuits](https://github.com/cyber-shuttle/allenai-v1)
- [Computational neuroscience models from brain atlases](https://github.com/cyber-shuttle/airavata-cerebrum)
- [Large-scale brain model simulations](https://github.com/cyber-shuttle/whole-brain-public)
- [Neural data analysis with torch_brain](https://github.com/cyber-shuttle/neurodata25_torchbrain_notebooks)
- [NAMD Workshop examples](https://github.com/cyber-shuttle/namd-workshop-2024)
- [OpenFold Attention Visualization](https://github.com/vizfold/attention-viz-demo)

See more projects at [cybershuttle.org/resources](https://cybershuttle.org/resources).

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
