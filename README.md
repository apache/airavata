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
| **Java JDK** | 25+ (LTS) | `java --version` |
| **Apache Maven** | 3.8+ | `mvn -v` |
| **Git** | Latest | `git --version` |
| **Docker** | 20.10+ | `docker --version` |
| **Docker Compose** | 2.0+ | `docker compose version` |

**System Requirements:** Minimum 8GB RAM (16GB recommended), 10GB free disk space. Unix-based OS (Linux/macOS) or Windows with WSL2.

**One-click option:** Open in [Dev Container](#dev-container) (Docker only; no local Java/Maven).

### Step 1: Clone and Build

```bash
git clone https://github.com/apache/airavata.git
cd airavata

# Build (without tests for speed)
mvn clean install -DskipTests
```

**Build time:** ~5-10 minutes (first build), ~2-3 minutes (subsequent builds)

### Step 2: Initialize (Docker + Keycloak + databases)

Init starts Docker (db, redis, keycloak) if needed, runs Keycloak setup (realm, pga client, default-admin), and applies DB migrations.

**First time?** Use clean-initialize for a fresh slate:

```bash
./scripts/init.sh --clean
```

**Already ran init?** Reuse mode (no data wipe):

```bash
./scripts/init.sh
```

Credentials: `default-admin` / `admin123`. Keycloak: http://localhost:18080 (admin/admin).

### Step 3: Run

```bash
./scripts/dev.sh serve
```

One-line cold start (build + init + serve):

```bash
./scripts/quickstart.sh
```

**Server ports:** Thrift 8930, HTTP 8080. See [CLI Commands Reference](#cli-commands-reference) for `serve -d`, `--debug`, and JAR mode.

### Step 4 (optional): Run the Portal

To use the web UI ([airavata-nextjs-portal](https://github.com/apache/airavata-portals) or equivalent):

1. Clone the portal, `npm install`, copy `.env.example` to `.env.local`
2. Set `API_URL=http://localhost:8080`, `KEYCLOAK_ISSUER=http://localhost:18080/realms/default`, `KEYCLOAK_CLIENT_ID=pga`, `KEYCLOAK_CLIENT_SECRET=m36BXQIxX3j3VILadeHMK5IvbOeRlCCc` (from [docker-compose](.devcontainer/docker-compose.yml)), `NEXTAUTH_URL=http://localhost:3000`, `NEXTAUTH_SECRET=<generate with: openssl rand -base64 32>`
3. `npm run dev`, open http://localhost:3000, login `default-admin` / `admin123`

### Dev Container

Open the project in VS Code or Cursor with the **Dev Containers** extension; use "Reopen in Container." This uses Docker only—no local Java or Maven. The container includes Java 25, Maven, and the dev environment. Run `./scripts/init.sh --clean && ./scripts/dev.sh serve` inside the container.

### Troubleshooting

- **Database init failed:** Ensure `./scripts/dev.sh init` runs (check Flyway logs).
- **Keycloak login 400/409:** Run `./scripts/init.sh --clean` to reset.
- **Port in use:** See [Troubleshooting](#troubleshooting) below.

---

## CLI Commands Reference

From the **project root**:

```bash
./scripts/dev.sh <command> [options]   # dev mode (hot reload, optional --debug)
./scripts/jar.sh <command> [options]  # JAR mode (default command: serve)
```

Examples: `./scripts/dev.sh serve`, `./scripts/dev.sh --debug serve`, `./scripts/jar.sh serve`, `./scripts/dev.sh --help`.

Alternatively, from `modules/distribution`: `mvn exec:java -Dexec.args="<command> [options]"`.

When using the distribution bundle (tarball or fat JAR):

```bash
./bin/airavata.sh <command> [options]
# Or: java -Dairavata.home=... -Dairavata.config.dir=... -jar airavata-*.jar <command> [options]
```

### Main Commands

| Command | Description |
|---------|-------------|
| `--help` | Show help |
| `--version` | Show version |
| `init` | Initialize databases |
| `init --clean` | Drop and recreate all databases |
| `serve` | Start in foreground (default) |
| `serve -d` | Start in background (daemon) |
| `serve --dev` | Start in foreground with hot-reload (DevTools) |

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

### System overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            External Clients                                  │
│   (Portals, SDKs, Agents, External Monitoring Systems)                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
           ┌────────────────────────┼────────────────────────┐
           │                        │                        │
           ▼                        ▼                        ▼
┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
│  Thrift Server   │   │   HTTP Server    │   │   gRPC Server    │
│    (port 8930)   │   │   (port 8080)    │   │   (port 9090)    │
│ Thrift Endpoints │   │ - Airavata API   │   │ - Agent Streams  │
│ for Airavata API │   │ - File API       │   │ - Research API   │
│                  │   │ - Agent API      │   │                  │
└──────────────────┘   └──────────────────┘   └──────────────────┘
           └────────────────────────┼────────────────────────┘
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       Unified Spring Boot Application                        │
│  Orchestrator │ Registry │ Profile Service │ Sharing Registry │ Credential   │
│  Store │ Workflow Managers                                                 │
└─────────────────────────────────────────────────────────────────────────────┘
           ▼                        ▼                        ▼
┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
│     MariaDB      │   │      Redis        │   │    Keycloak       │
└──────────────────┘   └──────────────────┘   └──────────────────┘
```

### API layers

- **Thrift (8930):** Multiplexed services: `Airavata`, `ProfileService.UserProfileService`, `ProfileService.TenantProfileService`, `RegistryService`, `CredentialStoreService`, `SharingRegistryService`. Implementation: `modules/thrift-api/`.
- **HTTP (8080):** `/api/v1/` (Airavata API), `/list/`, `/download/`, `/upload/` (File API), `/api/v1/agent/`, `/api/v1/research/`. Implementation: `modules/rest-api/`, `modules/file-server/`, `modules/agent-framework/agent-service/`, `modules/research-framework/research-service/`.
- **gRPC (9090):** Bidirectional streaming for agents and research API.

### Dapr workflows and state machine

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| **ProcessPreWorkflow** | Process launch | Env setup → Input staging → Job submission |
| **ProcessPostWorkflow** | Job completion | Output staging → Archive → Parsing trigger |
| **ProcessCancelWorkflow** | Cancel request | Workflow cancellation → Job cancellation |
| **ParsingWorkflow** | Parsing trigger | Data parsing execution |

**Experiment state flow:** CREATED → SCHEDULED → LAUNCHED → EXECUTING → COMPLETED (or CANCELING → CANCELED, or FAILED).

**Process state flow:** CREATED → VALIDATED → STARTED → PRE_PROCESSING → INPUT_DATA_STAGING → EXECUTING → MONITORING → OUTPUT_DATA_STAGING → POST_PROCESSING → COMPLETED.

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
| **Dapr Pub/Sub** | Messaging for experiment/process events and job status updates (single `status-change-topic`, backed by Redis) |
| **Dapr State Store** | Persistent state management for workflows and process state (backed by Redis) |
| **Email Monitor** | Monitors email for job status updates; publishes to `status-change-topic` |
| **Status Change Handler** | Subscribes to `status-change-topic`; resolves job and applies status via JobStatusHandler |
| **Pre Workflow Manager** | Handles pre-execution phases (schedules ProcessPreWorkflow via Dapr) |
| **Post Workflow Manager** | Handles post-execution phases (schedules ProcessPostWorkflow via Dapr) |
| **Parser Workflow Manager** | Handles data parsing (schedules ParsingWorkflow via Dapr) |

![image](assets/airavata-components.png)

---

## Internal Code Architecture

The core API module (`modules/airavata-api`) implements a layered architecture with clear separation of concerns.

### Package Structure Overview

```
org.apache.airavata
├── accountprovisioning/       # SSH account provisioning
├── activities/                # Dapr workflow activities
├── agents/                    # Agent framework
├── common/                    # Shared utilities and domain models
│   ├── model/                # Domain models (183 classes)
│   ├── exception/            # Custom exceptions
│   └── utils/                # Utility classes
├── config/                    # Spring configuration
├── credential/                # Credential management
├── monitor/                   # Monitoring services
├── orchestrator/              # Workflow orchestration
├── profile/                   # User/tenant profile management
├── registry/                  # Data persistence layer
│   ├── entities/             # JPA entities organized by catalog
│   ├── repositories/         # Data access repositories (54 classes)
│   ├── services/             # Domain services (37 classes)
│   └── mappers/              # Entity ↔ Model mappers (49 classes)
├── scheduling/                # Job scheduling
├── security/                  # Security and authorization
├── service/                   # High-level service interfaces
├── sharing/                   # Resource sharing and permissions
├── task/                      # Task implementations
├── telemetry/                 # Telemetry and metrics
├── util/                      # General utilities
└── workflow/                  # Workflow managers
```

### Data Layer Architecture

The registry layer follows a 4-tier architecture:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Service Layer                                      │
│   ExperimentService, ProcessService, ComputeResourceService, etc.           │
│   (37 @Service beans with @Transactional support)                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Mapper Layer                                       │
│   Entity ↔ Domain Model conversion using MapStruct                          │
│   (49 mapper interfaces with Spring component model)                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Repository Layer                                     │
│   AbstractRepository<T, E, Id> with generic CRUD operations                 │
│   Catalog-specific: AppCat, ExpCat, RepCat, WorkflowCat                     │
│   (54 repository classes)                                                   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Entity Layer                                       │
│   JPA entities organized by catalog type                                    │
│   (100+ entity classes with relationships and composite keys)               │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     Unified Database (MariaDB)                               │
│   Single schema with 64 tables, Flyway migrations                           │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Entity Organization

Entities are organized by catalog type under `registry/entities/`:

| Catalog | Location | Key Entities |
|---------|----------|--------------|
| **App Catalog** | `appcatalog/` | `ApplicationEntity`, `ApplicationDeploymentEntity`, `ComputeResourceEntity`, `StorageResourceEntity` |
| **Exp Catalog** | `expcatalog/` | `ExperimentEntity`, `ProcessEntity`, `JobEntity`, `TaskEntity`, `ProjectEntity`, `NotificationEntity` |
| **Airavata Workflow** | `airavataworkflowcatalog/` | `AiravataWorkflowEntity`, `WorkflowApplicationEntity`, `WorkflowConnectionEntity` |
| **Replica Catalog** | `replicacatalog/` | `DataProductEntity`, `DataReplicaLocationEntity` |
| **Root Level** | `entities/` | `GatewayEntity`, `UserEntity`, `StatusEntity`, `ErrorEntity`, `InputDataEntity`, `OutputDataEntity` |

**Credential-Centric Entities** (under `credential/entities/`):

| Entity | Purpose |
|--------|---------|
| `CredentialComputeConfigEntity` | Links credential to compute resource with access settings |
| `CredentialQueueConfigEntity` | Queue-specific allocation and limits |
| `CredentialQueueMacroEntity` | Queue-specific macros/environment variables |
| `CredentialGroupEntity` | Credential sharing group |
| `CredentialGroupMemberEntity` | User membership in a credential group |
| `CredentialGroupPermissionEntity` | Resource permissions granted by a group |

### Unified Database Schema

A full entity-relationship description is in [docs/ERD.md](docs/ERD.md). The database uses a consolidated schema with unified entities for cross-cutting concerns:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CORE UNIFIED ENTITIES                                │
├─────────────────────────────────────────────────────────────────────────────┤
│  GATEWAY              │ Unified gateway (profile + experiment catalog)      │
│  AIRAVATA_USER        │ User with OIDC claims (profile from Keycloak)       │
│  STATUS               │ Unified status tracking (experiment, process, etc.) │
│  ERROR                │ Unified error tracking for all entity types         │
│  INPUT_DATA           │ Unified inputs (experiment, process, application)   │
│  OUTPUT_DATA          │ Unified outputs (experiment, process, application)  │
│  METADATA             │ Unified key-value metadata storage                  │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                      RESOURCE MANAGEMENT ENTITIES                            │
├─────────────────────────────────────────────────────────────────────────────┤
│  RESOURCE_PROFILE     │ Unified profile (gateway, group, user)              │
│  RESOURCE_PREFERENCE  │ Key-value preferences with level inheritance        │
│  RESOURCE_INTERFACE   │ Unified interfaces (job submission, data movement)  │
│  RESOURCE_ACCESS      │ User/group to resource credential mapping           │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                       COMPUTE & STORAGE RESOURCES                            │
├─────────────────────────────────────────────────────────────────────────────┤
│  COMPUTE_RESOURCE     │ HPC clusters, cloud instances                       │
│  STORAGE_RESOURCE     │ File systems, object stores                         │
│  BATCH_QUEUE          │ Queue definitions per compute resource              │
│  SSH_JOB_SUBMISSION   │ SSH job submission interface                        │
│  SCP_DATA_MOVEMENT    │ SCP data movement interface                         │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                       APPLICATION CATALOG                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│  APPLICATION_INTERFACE│ Application definition and I/O                      │
│  APPLICATION_DEPLOYMENT│ Deployment on specific compute resource            │
│  PARSER / PARSER_IO   │ Output parsing definitions                          │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                        EXPERIMENT CATALOG                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│  PROJECT              │ Project container for experiments                   │
│  EXPERIMENT           │ Experiment definition and metadata                  │
│  PROCESS              │ Process within experiment                           │
│  TASK                 │ Task within process                                 │
│  JOB                  │ Job submitted to compute resource                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Credential-Centric Access Control

Airavata implements a **credential-centric architecture** where credentials are the root of all compute resource access:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CREDENTIAL-CENTRIC MODEL                                  │
│                                                                              │
│   CREDENTIAL (SSH Key, etc.)                                                │
│        ↓ grants access to                                                   │
│   CREDENTIAL COMPUTE CONFIG (specific resource + allocation)                │
│        ↓ used by                                                            │
│   APPLICATION DEPLOYMENT (what to run + where to run it)                    │
│        ↓ shared via                                                         │
│   CREDENTIAL GROUP (controlled sharing with users)                          │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Core Concepts:**

| Concept | Description |
|---------|-------------|
| **Credential** | SSH key or other authentication material |
| **CredentialComputeConfig** | Links a credential to a compute resource with access settings (login, scratch, allocation) |
| **CredentialQueueConfig** | Queue-specific settings (allocation, limits, macros) within a credential config |
| **ApplicationDeployment** | Links an application to a credential config (what + where) |
| **CredentialGroup** | Enables credential owners to share access with users |

**Key Components:**

```
CredentialComputeConfig                 CredentialGroup
├── credentialToken                     ├── groupId, groupName
├── computeResourceId                   ├── credentialToken
├── loginUsername                       ├── ownerId, gatewayId
├── scratchLocation                     ├── members (CredentialGroupMember[])
├── allocationProjectNumber             │   ├── userId
└── queueConfigs[]                      │   └── role (OWNER|ADMIN|MEMBER|VIEWER)
    ├── queueName                       └── permissions (CredentialGroupPermission[])
    ├── allocationProjectNumber             ├── resourceType (CONFIG|DEPLOYMENT|QUEUE)
    ├── maxNodeCount, maxCpuCount           ├── resourceId
    └── macros[]                            └── permissionLevel (READ|USE|ADMIN)

ApplicationDeployment
├── applicationId      (what to run)
├── credentialConfigId (where/how to run)
├── executablePath
└── parallelism, commands, environment
```

**Access Resolution Services:**

| Service | Purpose |
|---------|---------|
| `CredentialGroupService` | Manage groups, members, and permissions |
| `CredentialAccessService` | Unified access control (replaces old sharing) |
| `CredentialComputeConfigService` | Manage credential-resource configurations |
| `ApplicationDeploymentService` | Manage deployments bound to credentials |

**Database Schema:**

```sql
-- Credential configs link credentials to compute resources
CREDENTIAL_COMPUTE_CONFIG (
    CONFIG_ID,           -- Unique config identifier
    CREDENTIAL_TOKEN,    -- Reference to credential (SSH key)
    COMPUTE_RESOURCE_ID, -- Target compute resource
    LOGIN_USERNAME,      -- Login username for this resource
    SCRATCH_LOCATION,    -- Scratch directory path
    ...
)

-- Queue-specific settings within a config
CREDENTIAL_QUEUE_CONFIG (
    CONFIG_ID,                  -- Reference to compute config
    QUEUE_NAME,                 -- Queue name
    ALLOCATION_PROJECT_NUMBER,  -- Allocation for this queue
    MAX_NODE_COUNT,             -- Max nodes allowed
    ...
)

-- Credential groups for sharing
CREDENTIAL_GROUP (
    GROUP_ID, GROUP_NAME, CREDENTIAL_TOKEN, OWNER_ID, GATEWAY_ID
)

CREDENTIAL_GROUP_MEMBER (
    GROUP_ID, USER_ID, ROLE
)

CREDENTIAL_GROUP_PERMISSION (
    GROUP_ID, RESOURCE_TYPE, RESOURCE_ID, PERMISSION_LEVEL
)

-- Deployments bound to credentials
APPLICATION_DEPLOYMENT (
    APP_DEPLOYMENT_ID,
    APPLICATION_ID,       -- What to run
    CREDENTIAL_CONFIG_ID, -- Where to run (credential + resource)
    EXECUTABLE_PATH,
    ...
)
```

### Service Layer Organization

All 37 services are in `registry/services/`, organized by domain:

**Experiment Catalog Services:**
- `ExperimentService`, `ExperimentSummaryService` - Experiment management
- `ExperimentInputService`, `ExperimentOutputService` - Experiment I/O
- `ProcessService`, `ProcessInputService`, `ProcessOutputService` - Process management
- `ProcessWorkflowService` - Process workflow associations
- `JobService`, `JobStatusService` - Job tracking
- `TaskService` - Task management
- `ProjectService` - Project management
- `NotificationService` - Notifications
- `GatewayUsageReportingCommandService` - Usage reporting

**Application Catalog Services:**
- `ApplicationInterfaceService` - Application definitions
- `ApplicationDeploymentService` - Deployment configurations
- `ComputeResourceService` - Compute resource management
- `StorageResourceService` - Storage resource management
- `ParserService`, `ParserIOService`, `ParsingTemplateService` - Parsing

**Resource Profile Services:**
- `GroupResourceProfileService` - Group-level profiles
- `UserResourceProfileService` - User-level profiles
- `GwyResourceProfileService` - Gateway profiles

**Configuration & Preference Services:**
- `PreferenceResolutionService` - Core preference resolution
- `BatchQueuePreferenceService` - Queue policies
- `ApplicationPreferenceService` - Application defaults
- `GatewayConfigService` - Gateway configuration
- `SystemConfigService` - System settings

**Core Entity Services:**
- `GatewayService`, `GatewayGroupsService` - Gateway management
- `UserService` - User management
- `StatusService`, `ErrorService` - Status/error tracking
- `WorkflowService` - Workflow management

**Data Services:**
- `DataProductService` - Data products (unified model for datasets and replica catalog)
- `DataReplicaLocationService` - Replica locations

**Data products and datasets:** Datasets are managed as **data products**, not as catalog resources. Each data product has catalog metadata (name, description, authors, tags, privacy, scope), a **primary storage path** (`primaryStorageResourceId` + `primaryFilePath`), and optional replica locations. Applications link to data products via `airavata-dp://{gatewayId}/{productUri}`; the orchestrator resolves this to the primary storage path at runtime.

**Resource scope model:** The catalog uses a two-level scope with inferred delegation. **USER scope:** stored with `RESOURCE_SCOPE='USER'`, `OWNER_ID=userId`; any authenticated user can create; only owner can access (unless shared via groups). **GATEWAY scope:** stored with `RESOURCE_SCOPE='GATEWAY'`, `OWNER_ID=NULL`; gateway admins create; all gateway users can access. **DELEGATED scope:** not stored; inferred at runtime when resource has `GROUP_RESOURCE_PROFILE_ID` and user is a member of that group but does not directly own the resource. For catalog resources only **REPOSITORY** is supported for create/update; **DATASET** is deprecated (use Data Product API). The baseline schema includes `RESOURCE_SCOPE` and `GROUP_RESOURCE_PROFILE_ID` on application interfaces and data products.

### Mapper Pattern

MapStruct is used for type-safe entity ↔ model mapping:

```java
@Mapper(componentModel = "spring")
public interface ExperimentMapper {
    ExperimentModel toModel(ExperimentEntity entity);
    ExperimentEntity toEntity(ExperimentModel model);
}
```

Configuration (`EntityMapperConfig`):
- Component model: Spring (auto-wired)
- Null handling: IGNORE for properties
- Collection mapping: ADDER_PREFERRED

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

| Path | How to set | Description |
|------|------------|-------------|
| `/opt/apache-airavata` | `--home` or `AIRAVATA_HOME` | Installation directory (home) |
| `/opt/apache-airavata/conf` | `--config-dir` or `AIRAVATA_CONFIG_DIR` | Config directory (defaults to `{home}/conf` if not set) |
| `/opt/apache-airavata/logs` | (under home or configured) | Logs directory |

### Configuration Files

| File | Location | Description |
|------|----------|-------------|
| `airavata.properties` | `/opt/apache-airavata/conf/airavata.properties` or `conf/application.properties` | Main configuration file |
| `airavata.sym.p12` | `/opt/apache-airavata/conf/airavata.sym.p12` or `conf/keystores/airavata.sym.p12` | Keystore file |
| `logback.xml` | `/opt/apache-airavata/conf/logback.xml` or `conf/logback.xml` | Logging configuration |

**Note:** Spring Boot also recognizes `application.properties` as the configuration file name. All deployments use `/opt/apache-airavata/conf/` (or `conf/` in distribution bundle) as the configuration directory.

---

## Deployment Options

### Option 1: Tarball or Fat JAR

Distributions are a **tarball** (directory layout with `bin/`, `lib/`, `conf/`) and a **fat JAR** (single executable JAR with all dependencies). You can pass Airavata home and config directory via arguments or environment variables.

**From tarball:**

```bash
cd distribution
tar -xzf airavata-0.21-SNAPSHOT.tar.gz
cd airavata-0.21-SNAPSHOT

# Copy configuration files into conf/
cp ../../conf/airavata.properties conf/
cp ../../conf/airavata.sym.p12 conf/keystores/
cp ../../conf/*.yml conf/
cp ../../conf/logback.xml conf/

# Start (default: AIRAVATA_HOME = parent of bin/, config = AIRAVATA_HOME/conf)
./bin/airavata.sh -d start      # daemon mode
./bin/airavata.sh               # foreground mode

# Or pass home and config dir as arguments
./bin/airavata.sh --home /opt/apache-airavata --config-dir /etc/airavata/conf -d start
./bin/airavata.sh --home /opt/apache-airavata --config-dir /etc/airavata/conf
```

**From fat JAR only:** put the JAR in a directory and point to install and config. Either use `bin/airavata.sh` (which sets `-Dairavata.home` and `-Dairavata.config.dir` from `--home`/`--config-dir` or env), or invoke Java directly:

```bash
# Recommended: use the script so home/config are set for you
./bin/airavata.sh --home /path/to/install --config-dir /path/to/conf -d start

# Or run Java directly (set airavata.home and airavata.config.dir)
java -Dairavata.home=/path/to/install -Dairavata.config.dir=/path/to/conf -jar airavata-0.21-SNAPSHOT.jar serve
```

**Stop/Restart:** same script with `-d stop` / `-d restart` (use the same `--home`/`--config-dir` or env if you used them to start).

**Paths:**
- **Home:** installation root (e.g. `/opt/apache-airavata`). Set via `--home` or `AIRAVATA_HOME`.
- **Config dir:** directory containing `application.properties`, `logback.xml`, etc. Set via `--config-dir` or `AIRAVATA_CONFIG_DIR`; defaults to `{AIRAVATA_HOME}/conf` if not set.
- Logs directory: under home or as configured (e.g. `logs/`).

### Option 2: Docker (Experimental)

```bash
# Build containers
mvn docker:build -pl modules/distribution

# Start via compose
docker-compose -f modules/distribution/src/main/docker/docker-compose.yml up -d

# Stop
docker-compose -f modules/distribution/src/main/docker/docker-compose.yml down
```

### Option 3: Ansible Deployment (Production)

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

Import as Maven project. Set Project SDK to Java 25+. Enable annotation processing.

**VS Code:** [.vscode/launch.json](.vscode/launch.json) and [.vscode/tasks.json](.vscode/tasks.json) provide run configurations and tasks. Install Java Extension Pack, then use Run and Debug (F5) or Tasks (Terminal > Run Task).

| Launch config | Purpose |
|---------------|---------|
| Init (clean) | Initialize databases (wipe + migrate) |
| Init (reuse) | Run DB migrations only |
| Serve Airavata | Start server |
| Serve Airavata (Debug) | Start server with jdwp on port 5005 |

| Task | Purpose |
|------|---------|
| Initialize Services | Run `./scripts/init.sh` |
| Initialize Services (clean) | Run `./scripts/init.sh --clean` |
| Generate Distribution | `mvn package -pl modules/distribution` |
| Run Tests (all) | `mvn test` |
| Run Tests (airavata-api) | `mvn test -pl modules/airavata-api` |

**IntelliJ:** [.idea/runConfigurations/](.idea/runConfigurations/) provides shared run configs. Use Run > Edit Configurations to select.

| Run config | Purpose |
|------------|---------|
| Init (clean) | Initialize databases (wipe + migrate) |
| Init (reuse) | Run DB migrations only |
| Serve Airavata | Start server |
| Serve Airavata (Debug) | Start server with jdwp on port 5005 |
| Generate Distribution | `mvn package -pl modules/distribution` |
| Run Tests | `mvn test` |

**Note:** Run Init or Initialize Services before Serve. Ensure Docker (db, redis, keycloak) is up—use `./scripts/init.sh` for full init.

### Running Tests

```bash
mvn test                                    # All tests
mvn test -pl modules/airavata-api           # Specific module
mvn test -Dtest=SomeTestClass               # Specific test
```

**Test categories:**
- **Unit and repository tests** run with Testcontainers (MariaDB, Redis) and do not require Dapr or external messaging. Many pass with `mvn test`; some repository tests may have environment-specific failures.
- **Workflow/state-machine integration tests** (e.g. experiment lifecycle, process state transitions) expect Dapr workflows and activities to run; they are intended for environments where the full stack (including Dapr) is available. In CI without Dapr, these tests may remain in CREATED/SUBMITTED state and fail assertions; this is expected when the workflow runtime is disabled.
- **Credential store integration tests** require a configured credential store backend (e.g. Vault); they may be skipped or fail if the backend is not available.
- For a fast build without running tests: `mvn clean install -DskipTests`.

### Thrift Stubs

Airavata's Thrift IDLs live in `thrift-interface-descriptions/`. See `thrift-interface-descriptions/README.md` for stub generation.

### Schema migrations (contributors)

The database schema is a single Flyway baseline: `src/main/resources/conf/db/migration/airavata/V1__Baseline_schema.sql`. JPA entities are discovered via package scanning (no `persistence.xml`). For schema changes: (1) Add or update the Entity class. (2) Add a new versioned Flyway migration (e.g. `V2__Description.sql`) in that directory so the database schema matches the entity, or for major baseline changes coordinate updating V1. Use `IF NOT EXISTS`/`IF EXISTS` where appropriate. **Known issues:** Removals (drop column/table) must be done manually. The distribution tarball uses only these Flyway scripts for DB setup.

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
