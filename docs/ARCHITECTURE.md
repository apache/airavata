# Airavata Architecture

This document provides a comprehensive overview of the Apache Airavata architecture, including server components, workflows, activities, and state machines.

---

## System Overview

Apache Airavata is a software framework for executing and managing computational jobs on distributed computing resources. All services run in a **unified Spring Boot application** (`AiravataServer`) within a single JVM process.

### High-Level Architecture

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
│                  │   │                  │   │                  │
│ Thrift Endpoints │   │ - Airavata API   │   │ - Agent Streams  │
│ for Airavata API │   │ - File API       │   │ - Research API   │
│                  │   │ - Agent API      │   │                  │
│                  │   │ - Research API   │   │                  │
└──────────────────┘   └──────────────────┘   └──────────────────┘
           │                        │                        │
           └────────────────────────┼────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       Unified Spring Boot Application                        │
│                                                                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐              │
│  │   Orchestrator  │  │    Registry     │  │ Profile Service │              │
│  │                 │  │                 │  │                 │              │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐              │
│  │ Sharing Registry│  │ Credential Store│  │ Workflow Managers│             │
│  │                 │  │                 │  │                 │              │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘              │
└─────────────────────────────────────────────────────────────────────────────┘
           │                        │                        │
           ▼                        ▼                        ▼
┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
│     MariaDB      │   │      Redis       │   │    Keycloak      │
│   (Database)     │   │  (Dapr Pub/Sub   │   │      (IAM)       │
│                  │   │   & State Store) │   │                  │
└──────────────────┘   └──────────────────┘   └──────────────────┘
```

---

## Server Ports

| Server | Port | Configuration Property | Description |
|--------|------|------------------------|-------------|
| **Thrift Server** | 8930 | `airavata.services.thrift.server.port` | Thrift Endpoints for Airavata API functions |
| **HTTP Server** | 8080 | `airavata.services.http.server.port` | HTTP Endpoints (Airavata API, File API, Agent API, Research API) |
| **gRPC Server** | 9090 | `airavata.services.grpc.server.port` | Persistent channels for agents and research API |
| **Dapr gRPC** | 50001 | `airavata.dapr.grpc-port` | Sidecar for pub/sub, state, and workflow execution |

---

## API Layers

### Thrift Server (Port 8930)

Multiplexed Thrift services exposing the Airavata API:

| Service Name | Description |
|--------------|-------------|
| `Airavata` | Main service for experiments, processes, and workflows |
| `ProfileService.UserProfileService` | User profile management |
| `ProfileService.TenantProfileService` | Tenant/gateway profile management |
| `ProfileService.IamAdminServices` | IAM administration |
| `ProfileService.GroupManagerService` | Group management |
| `OrchestratorService` | Workflow orchestration (internal) |
| `RegistryService` | Metadata registry (internal) |
| `CredentialStoreService` | Credential storage (internal) |
| `SharingRegistryService` | Permissions and sharing (internal) |

**Implementation:** `modules/thrift-api/`

### HTTP Server (Port 8080)

RESTful HTTP APIs:

| API | Base Path | Description |
|-----|-----------|-------------|
| **Airavata API** | `/api/v1/` | HTTP endpoints for core Airavata functions |
| **File API** | `/list/`, `/download/`, `/upload/` | File operations for process data |
| **Agent API** | `/api/v1/agent/` | Agent management and execution |
| **Research API** | `/api/v1/research/` | Research hub integration |

**Implementation:** 
- Airavata API: `modules/rest-api/`
- File API: `modules/file-server/`
- Agent API: `modules/agent-framework/agent-service/`
- Research API: `modules/research-framework/research-service/`

### gRPC Server (Port 9090)

Bidirectional streaming for persistent connections:

- **Agent Communication:** Long-lived connections with remote agents
- **Research API:** Persistent channels for research hub

**Proto definition:** `modules/agent-framework/proto/agent-communication.proto`

---

## Internal Services

These services are accessible via the Thrift Server (port 8930) and are not separate processes:

| Service | Description | Implementation |
|---------|-------------|----------------|
| **Orchestrator** | Constructs workflow DAGs, manages experiment lifecycle | `modules/airavata-api/.../service/orchestrator/` |
| **Registry** | Manages metadata and application definitions | `modules/airavata-api/.../service/registry/` |
| **Profile Service** | Manages users, tenants, compute resources | `modules/airavata-api/.../service/profile/` |
| **Credential Services** | Credential storage and access control | `modules/airavata-api/.../credential/` |

---

## Credential-Centric Access Control

Airavata uses a credential-centric architecture where credentials are the root of all compute resource access. This simplifies the access model and makes sharing more intuitive.

### Core Model

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CREDENTIAL-CENTRIC MODEL                             │
│                                                                              │
│  ┌────────────────┐     ┌────────────────────────┐     ┌─────────────────┐  │
│  │   Credential   │────▶│ CredentialComputeConfig│────▶│   Deployment    │  │
│  │   (SSH Key)    │     │  (access settings)     │     │                 │  │
│  └────────────────┘     └────────────────────────┘     └─────────────────┘  │
│          │                         │                                         │
│          │                         ▼                                         │
│          │              ┌────────────────────────┐                          │
│          │              │  CredentialQueueConfig │                          │
│          │              │  (per-queue settings)  │                          │
│          │              └────────────────────────┘                          │
│          │                                                                   │
│          ▼                                                                   │
│  ┌────────────────┐     ┌────────────────────────┐     ┌─────────────────┐  │
│  │CredentialGroup │────▶│ CredentialGroupMember  │     │   GroupPermission│ │
│  │  (sharing)     │     │     (users)            │     │  (what's shared) │ │
│  └────────────────┘     └────────────────────────┘     └─────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Key Entities

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **CredentialComputeConfig** | Links credential to compute resource | `credentialToken`, `computeResourceId`, `loginUsername`, `scratchLocation` |
| **CredentialQueueConfig** | Queue-specific settings | `queueName`, `allocationProjectNumber`, `maxNodeCount`, `defaultQueue` |
| **CredentialQueueMacro** | Queue environment variables | `macroName`, `macroValue` |
| **CredentialGroup** | Credential sharing group | `credentialToken`, `ownerId`, `members[]`, `permissions[]` |
| **CredentialGroupMember** | User in a group | `userId`, `role` (OWNER/ADMIN/MEMBER/VIEWER) |
| **CredentialGroupPermission** | Resource permission | `resourceType`, `resourceId`, `permissionLevel` (READ/USE/ADMIN) |

### Application Deployment Model

Deployments are bound to credential configs rather than directly to compute resources:

```
ApplicationDeployment {
    applicationId      // What application to run
    credentialConfigId // Which credential config provides access
    executablePath     // How to run it
    parallelism        // MPI, OpenMP, etc.
}
```

This model ensures:
- **Access inheritance**: Deployment inherits all access settings from the credential config
- **Simple sharing**: Share via credential groups instead of complex ACLs
- **Natural organization**: Users see deployments they have access to via their credentials

### Access Resolution

The `CredentialAccessService` handles access control:

```java
// Check if user can use a credential config
credentialAccessService.hasConfigAccess(userId, configId, PermissionLevel.USE)

// Get all configs a user can access
credentialAccessService.getAccessibleConfigs(userId, gatewayId)

// Get all deployments a user can access
credentialAccessService.getAccessibleDeployments(userId, gatewayId)
```

Access is granted if:
1. User owns the credential directly, OR
2. User is a member of a credential group that has permission for the resource

### REST API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/credential-configs` | GET | List credential configs |
| `/api/v1/credential-configs` | POST | Create credential config |
| `/api/v1/credential-configs/{id}` | GET/PUT/DELETE | Manage specific config |
| `/api/v1/credential-groups` | GET | List groups user can access |
| `/api/v1/credential-groups` | POST | Create sharing group |
| `/api/v1/credential-groups/{id}/members` | GET/POST/DELETE | Manage group members |
| `/api/v1/credential-groups/{id}/permissions` | GET/POST/DELETE | Manage permissions |
| `/api/v1/application-deployments` | GET/POST | List/create deployments |

---

## Background Services

### Workflow Managers

| Manager | Purpose | Implementation |
|---------|---------|----------------|
| **PreWorkflowManager** | Handles pre-execution phases | `org.apache.airavata.workflow.process.pre.PreWorkflowManager` |
| **PostWorkflowManager** | Handles post-execution phases | `org.apache.airavata.workflow.process.post.PostWorkflowManager` |
| **ParserWorkflowManager** | Handles data parsing | `org.apache.airavata.workflow.process.parsing.ParserWorkflowManager` |

### Monitors

| Monitor | Purpose | Configuration |
|---------|---------|---------------|
| **Email Monitor** | Monitors email for job status updates | `airavata.services.monitor.email.enabled` |
| **Realtime Monitor** | Listens for state-change messages via Dapr Pub/Sub | `airavata.services.monitor.realtime.enabled` |
| **Compute Monitor** | Monitors compute resources | `airavata.services.monitor.compute.enabled` |

---

## Dapr Workflows

Airavata uses Dapr Workflows for durable orchestration of experiment/process execution.

### Workflows

| Workflow | Trigger | Purpose | Implementation |
|----------|---------|---------|----------------|
| **ProcessPreWorkflow** | Process launch | Env setup → Input staging → Job submission | `org.apache.airavata.workflow.process.pre.ProcessPreWorkflow` |
| **ProcessPostWorkflow** | Job completion | Output staging → Archive → Parsing trigger | `org.apache.airavata.workflow.process.post.ProcessPostWorkflow` |
| **ProcessCancelWorkflow** | Cancel request | Workflow cancellation → Job cancellation | `org.apache.airavata.workflow.process.cancel.ProcessCancelWorkflow` |
| **ParsingWorkflow** | Parsing trigger | Data parsing execution | `org.apache.airavata.workflow.process.parsing.ParsingWorkflow` |

### Activities

Activities are organized by function in `org.apache.airavata.activities.*`:

#### Pre-Processing (`activities.process.pre`)
- `EnvSetupActivity` - Environment configuration
- `InputDataStagingActivity` - Input data staging
- `JobSubmissionActivity` - Job submission to compute

#### Post-Processing (`activities.process.post`)
- `OutputDataStagingActivity` - Output data retrieval
- `ArchiveActivity` - Data archiving
- `JobVerificationActivity` - Job verification
- `ParsingTriggeringActivity` - Trigger parsing workflow

#### Cancellation (`activities.process.cancel`)
- `WorkflowCancellationActivity` - Cancel Dapr workflows
- `RemoteJobCancellationActivity` - Cancel remote jobs
- `CancelCompletingActivity` - Finalize cancellation

#### Parsing (`activities.parsing`)
- `DataParsingActivity` - Data parsing execution

#### Shared (`activities.shared`)
- `CompletingActivity` - Process completion
- `TaskExecutorHelper` - Task execution utilities

### Dapr Configuration

| Class | Purpose | Package |
|-------|---------|---------|
| `DaprWorkflowRuntimeConfig` | Workflow runtime setup | `org.apache.airavata.orchestrator.internal.workflow` |
| `DaprWorkflowClientHolder` | Workflow client management | `org.apache.airavata.orchestrator.internal.workflow` |
| `ProcessStatusUpdateHelper` | Status update utilities | `org.apache.airavata.orchestrator.internal.workflow` |

---

## State Machine

### State Validators

All validators are in `org.apache.airavata.orchestrator.state`:

| Validator | States | Description |
|-----------|--------|-------------|
| `ExperimentStateValidator` | CREATED, SCHEDULED, LAUNCHED, EXECUTING, CANCELING, CANCELED, COMPLETED, FAILED | Validates experiment state transitions |
| `ProcessStateValidator` | CREATED, VALIDATED, STARTED, PRE_PROCESSING, INPUT_DATA_STAGING, EXECUTING, OUTPUT_DATA_STAGING, COMPLETED, FAILED, CANCELED, etc. | Validates process state transitions |
| `JobStateValidator` | SUBMITTED, QUEUED, ACTIVE, COMPLETE, CANCELED, FAILED, SUSPENDED, UNKNOWN | Validates job state transitions |
| `TaskStateValidator` | CREATED, EXECUTING, COMPLETED, FAILED, CANCELED | Validates task state transitions |

### Experiment State Flow

```
CREATED → SCHEDULED → LAUNCHED → EXECUTING → COMPLETED
                ↓           ↓           ↓
            CANCELING ← CANCELING ← CANCELING → CANCELED
                                        ↓
                                      FAILED
```

### Process State Flow

```
CREATED → VALIDATED → STARTED → PRE_PROCESSING → INPUT_DATA_STAGING 
    → EXECUTING → MONITORING → OUTPUT_DATA_STAGING → POST_PROCESSING → COMPLETED
```

See [DAPR_WORKFLOW_STATE_MACHINE_SPEC.md](DAPR_WORKFLOW_STATE_MACHINE_SPEC.md) for complete state machine documentation.

---

## Configuration

### Standard Paths

| Path | Environment Variable | Description |
|------|---------------------|-------------|
| `/opt/apache-airavata` | `AIRAVATA_HOME` | Installation directory |
| `/opt/apache-airavata/conf` | `AIRAVATA_CONFIG_DIR` | Configuration directory |
| `/opt/apache-airavata/logs` | - | Logs directory |

### Key Configuration Properties

| Property | Description |
|----------|-------------|
| `airavata.dapr.enabled` | Enable Dapr integration |
| `airavata.dapr.pubsub.name` | Dapr pub/sub component name |
| `airavata.dapr.state.name` | Dapr state store component name |
| `airavata.services.thrift.enabled` | Enable Thrift server |
| `airavata.services.rest.enabled` | Enable REST API |
| `airavata.services.controller.enabled` | Enable workflow runtime |
| `airavata.services.prewm.enabled` | Enable pre-workflow manager |
| `airavata.services.postwm.enabled` | Enable post-workflow manager |
| `airavata.services.parser.enabled` | Enable parser workflow manager |

See `src/main/resources/application.properties` for complete configuration reference.

---

## Package Structure

```
org.apache.airavata
├── activities/                    # Dapr activities
│   ├── monitoring/               # Monitoring activities
│   ├── parsing/                  # Parsing activities
│   ├── process/                  # Process lifecycle activities
│   │   ├── cancel/              # Cancellation activities
│   │   ├── post/                # Post-processing activities
│   │   └── pre/                 # Pre-processing activities
│   ├── scheduling/               # Scheduling activities
│   └── shared/                   # Shared utilities
├── config/                        # Configuration classes
├── credential/                    # Credential management
├── monitor/                       # Monitoring services
├── orchestrator/                  # Orchestration
│   ├── internal/                 # Internal components
│   │   ├── messaging/           # Dapr messaging
│   │   ├── monitoring/          # Internal monitoring
│   │   └── workflow/            # Workflow runtime
│   └── state/                    # State validators
├── registry/                      # Registry services
├── service/                       # Core services
│   ├── orchestrator/             # Orchestrator service
│   ├── profile/                  # Profile services
│   ├── registry/                 # Registry service
│   └── sharing/                  # Sharing registry
├── task/                          # Task implementations
│   ├── base/                     # Base task classes
│   ├── parsing/                  # Parsing tasks
│   └── submission/               # Submission tasks
└── workflow/                      # Workflow managers
    ├── common/                   # Common utilities
    ├── monitoring/               # Monitoring workflows
    ├── orchestrator/             # Orchestrator
    ├── process/                  # Process workflows
    │   ├── cancel/              # Cancel workflow
    │   ├── parsing/             # Parsing workflow
    │   ├── post/                # Post workflow
    │   └── pre/                 # Pre workflow
    └── scheduling/               # Scheduling workflows
```

---

---

## Data Models

### Credential-Based Resource Access Model

Credentials in Airavata represent more than just authentication - they define an **access context** that determines what a user can do on a compute resource.

```
┌─────────────────────────────────────────────────────────────────┐
│                        CredentialEntity                         │
│  (Authentication - SSH key, password, etc.)                     │
│  - tokenId (unique identifier)                                  │
│  - gatewayId                                                    │
│  - credential (encrypted blob)                                  │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                  CredentialComputeConfig                        │
│  (Access Context - what this credential grants)                 │
│  - configId                                                     │
│  - credentialToken ──────────► Credential                       │
│  - computeResourceId ────────► ComputeResource                  │
│  - loginUsername                                                │
│  - scratchLocation                                              │
│  - allocationProjectNumber (default)                            │
│  - jobSubmissionProtocol                                        │
│  - dataMovementProtocol                                         │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                  CredentialQueueConfig                          │
│  (Queue-specific allocations and limits)                        │
│  - queueName                                                    │
│  - allocationProjectNumber (overrides default)                  │
│  - maxNodeCount, maxCpuCount, maxWalltime                       │
│  - defaultNodeCount, defaultCpuCount, defaultWalltime           │
│  - qos (Quality of Service)                                     │
│  - isDefaultQueue                                               │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                  CredentialQueueMacro                           │
│  (Queue-specific environment/macros)                            │
│  - macroName                                                    │
│  - macroValue                                                   │
│  - description                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**Key Insight:** On systems like SLURM clusters, a credential (SSH key pair) doesn't grant access to the entire cluster. It grants access to **specific queues** with **specific allocations**. Different queues may have:
- Different allocation/account numbers
- Different resource limits (max nodes, walltime)
- Different environment configurations (CUDA paths, module loads)

Example: A research group might have:
- SSH key for user "jdoe" on cluster "stampede3"
- Access to "normal" queue with allocation "TG-CHE140073", max 128 nodes
- Access to "gpu" queue with allocation "TG-BIO210045", max 16 nodes, with CUDA environment

### Simplified Application Model

Applications are defined with inputs/outputs directly attached, without the legacy module/interface indirection:

```
┌─────────────────────────────────────────────────────────────────┐
│                         Application                             │
│  - applicationId                                                │
│  - applicationName, version, description                        │
│  - gatewayId                                                    │
│  - inputs[] ─────────────► InputDataEntity                      │
│  - outputs[] ────────────► OutputDataEntity                     │
│  - archiveWorkingDirectory                                      │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ApplicationDeployment                        │
│  (How to run an app on a resource via a credential)             │
│  - deploymentId                                                 │
│  - applicationId ────────────► Application                      │
│  - credentialConfigId ───────► CredentialComputeConfig          │
│      └── provides: computeResourceId, queues, allocations       │
│  - executablePath                                               │
│  - parallelism                                                  │
│  - defaultQueueName                                             │
│  - commands[], environment[], libraryPaths[]                    │
└─────────────────────────────────────────────────────────────────┘
```

**Deployment Resolution:** When a deployment specifies a `credentialConfigId`:
1. The **compute resource** is derived from the credential config
2. Available **queues** are determined by the credential's queue configs
3. **Allocations and limits** are looked up per queue
4. **Environment macros** are injected into the job

### Entity Relationships Summary

```
Gateway
├── Users[]
├── Groups[]
├── Credentials[] ─────────────► CredentialComputeConfig[]
│                                 └── CredentialQueueConfig[]
│                                      └── CredentialQueueMacro[]
├── Applications[]
│   ├── Inputs[]
│   └── Outputs[]
├── ApplicationDeployments[]
│   ├── → Application
│   └── → CredentialComputeConfig
├── Projects[]
│   └── Experiments[]
│       └── Processes[]
│           └── Tasks[]
└── ComputeResources[] (reference data)
    └── BatchQueues[] (available queues on resource)
```

---

## Related Documentation

- [README.md](../README.md) - Quick start and main documentation
- [DAPR_WORKFLOW_STATE_MACHINE_SPEC.md](DAPR_WORKFLOW_STATE_MACHINE_SPEC.md) - State machine specification
- [PLAN_REMOVE_PUBSUB_WORKFLOW_DRIVEN.md](PLAN_REMOVE_PUBSUB_WORKFLOW_DRIVEN.md) - Future architecture plans
- [dev-tools/ansible/README.md](../dev-tools/ansible/README.md) - Deployment documentation
