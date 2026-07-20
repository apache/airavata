# Airavata Scheduler Architecture

## Overview

The Airavata Scheduler is a distributed task execution system designed for scientific computing experiments. It implements a clean hexagonal architecture (ports-and-adapters pattern) that provides cost-based scheduling, intelligent resource allocation, and comprehensive data management with a focus on clarity, reliability, and performance.

## Hexagonal Architecture

The system follows the hexagonal architecture pattern, also known as ports-and-adapters, which provides:

- **Clear separation of concerns**: Business logic is isolated from infrastructure
- **Testability**: Core domain can be tested without external dependencies
- **Flexibility**: Easy to swap implementations or add new adapters
- **Maintainability**: Changes to external systems don't affect core business logic

## Credential Management Architecture

The Airavata Scheduler implements a **three-layer credential architecture** that separates authorization logic from storage for maximum security and scalability:

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│   (Experiments, Resources, Users, Groups)                   │
└────────────┬────────────────────────────────────────────────┘
             │
             ├──────────────────┬──────────────────┐
             │                  │                  │
┌────────────▼─────┐  ┌────────▼────────┐  ┌─────▼──────────┐
│   PostgreSQL     │  │    SpiceDB      │  │   OpenBao      │
│                  │  │                 │  │                │
│  Domain Data     │  │  Authorization  │  │  Secrets       │
│  - Users         │  │  - Permissions  │  │  - SSH Keys    │
│  - Groups        │  │  - Ownership    │  │  - Passwords   │
│  - Experiments   │  │  - Sharing      │  │  - Tokens      │
│  - Resources     │  │  - Hierarchies  │  │  (Encrypted)   │
└──────────────────┘  └─────────────────┘  └────────────────┘
```

### Component Responsibilities

#### PostgreSQL - Domain Entity Storage
- **Purpose**: Stores non-sensitive business domain entities
- **Data**: Users, groups, experiments, resources, tasks
- **What it DOES NOT store**: Credentials, permissions, or access control lists

#### SpiceDB - Fine-Grained Authorization
- **Purpose**: Manages all permission relationships and access control
- **Capabilities**: Owner/reader/writer relationships, hierarchical groups, resource bindings
- **Schema**: Zanzibar model with transitive permission inheritance

#### OpenBao - Secure Credential Storage
- **Purpose**: Encrypts and stores sensitive credential data
- **Features**: KV v2 secrets engine, AES-256-GCM encryption, audit logging
- **Storage**: Encrypted SSH keys, passwords, API tokens

### Credential Resolution Flow

When an experiment is submitted, the system follows this flow:

```
1. User submits experiment
   ↓
2. System identifies required resources (compute, storage)
   ↓
3. SpiceDB: Find credentials bound to each resource
   ↓
4. SpiceDB: Check user has read permission on each credential
   ↓
5. OpenBao: Decrypt and retrieve credential data
   ↓
6. System: Provide credentials to workers for execution
```

### Permission Model

```
credential owner    → Full control (read/write/delete/share)
credential reader   → Read-only access (can be user or group)
credential writer   → Read + write (can be user or group)
```

**Hierarchical groups**: If Group B is a member of Group A, and a credential is shared with Group A, members of Group B automatically inherit access through the `is_member` permission.

### Architecture Layers

```
┌─────────────────────────────────────────────────────────────────┐
│                    Airavata Scheduler                          │
├─────────────────────────────────────────────────────────────────┤
│  Core Domain Layer (Business Logic)                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│  │domain/      │ │domain/      │ │domain/      │              │
│  │model.go     │ │interface.go │ │enum.go      │              │
│  └─────────────┘ └─────────────┘ └─────────────┘              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│  │domain/      │ │domain/      │ │domain/      │              │
│  │value.go     │ │error.go     │ │event.go     │              │
│  └─────────────┘ └─────────────┘ └─────────────┘              │
├─────────────────────────────────────────────────────────────────┤
│  Core Services Layer (Implementation)                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│  │service/     │ │service/     │ │service/     │              │
│  │registry.go  │ │vault.go     │ │orchestrator │              │
│  └─────────────┘ └─────────────┘ └─────────────┘              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│  │service/     │ │service/     │ │service/     │              │
│  │scheduler.go │ │datamover.go │ │worker.go    │              │
│  └─────────────┘ └─────────────┘ └─────────────┘              │
├─────────────────────────────────────────────────────────────────┤
│  Core Ports Layer (Infrastructure Interfaces)                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│  │port/        │ │port/        │ │port/        │              │
│  │database.go  │ │cache.go     │ │events.go    │              │
│  └─────────────┘ └─────────────┘ └─────────────┘              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│  │port/        │ │port/        │ │port/        │              │
│  │security.go  │ │storage.go   │ │compute.go   │              │
│  └─────────────┘ └─────────────┘ └─────────────┘              │
├─────────────────────────────────────────────────────────────────┤
│  Adapters Layer (External Integrations)                        │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│  │HTTP         │ │PostgreSQL   │ │SLURM/K8s    │              │
│  │WebSocket    │ │Redis        │ │S3/NFS/SFTP  │              │
│  │gRPC Worker  │ │Cache        │ │Bare Metal   │              │
│  └─────────────┘ └─────────────┘ └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘
```

## Core Domain Interfaces

The system is built around 6 fundamental domain interfaces that represent the core business operations:

### 1. ResourceRegistry
Manages compute and storage resources with registration, validation, and discovery capabilities.

**Key Operations:**
- `RegisterComputeResource()` - Add new compute resources with validation
- `RegisterStorageResource()` - Add new storage resources with connectivity testing
- `ValidateResourceConnection()` - Test connectivity and credential validation
- `ListComputeResources()` - Discover available compute resources
- `ListStorageResources()` - Discover available storage resources

### 2. CredentialVault
Provides secure credential storage with Unix-style permissions and enterprise-grade encryption.

**Key Features:**
- AES-256-GCM encryption with envelope encryption
- Argon2id key derivation for memory-hard security
- Unix-style permission model (rwx for owner/group/other)
- Complete audit logging and access tracking
- Credential rotation and lifecycle management

### 3. ExperimentOrchestrator
Manages the complete lifecycle of computational experiments from creation to completion.

**Key Operations:**
- `CreateExperiment()` - Create new experiments with dynamic templates
- `GenerateTasks()` - Generate task sets from parameter combinations
- `GetExperimentStatus()` - Monitor experiment progress
- `CreateDerivativeExperiment()` - Create new experiments based on results
- `ListExperiments()` - Query and filter experiments

### 4. TaskScheduler
Implements cost-based task scheduling with multi-objective optimization.

**Key Features:**
- Cost optimization (time, cost, deadline)
- Dynamic worker distribution
- Atomic task assignment
- Worker lifecycle management
- Performance metrics collection

### 5. DataMover
Manages 3-hop data staging with persistent caching and lineage tracking.

**Key Operations:**
- `StageIn()` - Move data from central storage to compute storage
- `StageOut()` - Move results from compute storage to central storage
- `CacheData()` - Persistent caching with integrity verification
- `RecordDataLineage()` - Track data provenance and transformations
- `VerifyDataIntegrity()` - Ensure data consistency

### 6. WorkerLifecycle
Manages the spawning, monitoring, and termination of computational workers.

**Key Operations:**
- `SpawnWorker()` - Create workers on compute resources
- `TerminateWorker()` - Clean shutdown of workers
- `GetWorkerLogs()` - Access worker execution logs
- `UpdateWorkerStatus()` - Monitor worker health
- `Heartbeat()` - Track worker metrics and status

## Credential Management Architecture

The Airavata Scheduler implements a modern, secure credential management system using **OpenBao** for credential storage and **SpiceDB** for authorization. This architecture provides enterprise-grade security with fine-grained access control and comprehensive audit capabilities.

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Credential Management System                 │
├─────────────────────────────────────────────────────────────────┤
│  Core Services Layer                                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│  │VaultService │ │Registry     │ │Compute/     │              │
│  │(Business    │ │Service      │ │Storage      │              │
│  │ Logic)      │ │             │ │Services     │              │
│  └─────────────┘ └─────────────┘ └─────────────┘              │
├─────────────────────────────────────────────────────────────────┤
│  Port Interfaces                                               │
│  ┌─────────────┐ ┌─────────────┐                              │
│  │VaultPort    │ │Authorization│                              │
│  │(Storage)    │ │Port (ACL)   │                              │
│  └─────────────┘ └─────────────┘                              │
├─────────────────────────────────────────────────────────────────┤
│  Adapter Layer                                                 │
│  ┌─────────────┐ ┌─────────────┐                              │
│  │OpenBao      │ │SpiceDB      │                              │
│  │Adapter      │ │Adapter      │                              │
│  └─────────────┘ └─────────────┘                              │
├─────────────────────────────────────────────────────────────────┤
│  External Services                                             │
│  ┌─────────────┐ ┌─────────────┐                              │
│  │OpenBao      │ │SpiceDB      │                              │
│  │(Vault)      │ │(AuthZ)      │                              │
│  └─────────────┘ └─────────────┘                              │
└─────────────────────────────────────────────────────────────────┘
```

### Key Components

#### 1. VaultService (Core Business Logic)
The `VaultService` implements the `CredentialVault` domain interface and provides the main credential management functionality:

**Core Operations:**
- `StoreCredential()` - Securely store credentials with encryption
- `RetrieveCredential()` - Retrieve and decrypt credentials
- `UpdateCredential()` - Update existing credentials
- `DeleteCredential()` - Securely delete credentials
- `ListCredentials()` - List accessible credentials
- `ShareCredential()` - Share credentials with users/groups
- `RevokeAccess()` - Revoke credential access

**Security Features:**
- Automatic encryption/decryption using OpenBao
- Permission-based access control via SpiceDB
- Audit logging for all operations
- Credential rotation support

#### 2. VaultPort Interface
Defines the contract for credential storage operations:

```go
type VaultPort interface {
    StoreCredential(ctx context.Context, id string, credentialType CredentialType, data []byte, ownerID string) (string, error)
    RetrieveCredential(ctx context.Context, id string, userID string) (*Credential, error)
    UpdateCredential(ctx context.Context, id string, data []byte, userID string) error
    DeleteCredential(ctx context.Context, id string, userID string) error
    ListCredentials(ctx context.Context, userID string) ([]*Credential, error)
}
```

#### 3. AuthorizationPort Interface
Defines the contract for authorization operations:

```go
type AuthorizationPort interface {
    CheckPermission(ctx context.Context, userID, objectID, objectType, permission string) (bool, error)
    ShareCredential(ctx context.Context, credentialID, userID, objectType, permission string) error
    RevokeAccess(ctx context.Context, credentialID, userID, objectType, permission string) error
    ListAccessibleCredentials(ctx context.Context, userID string) ([]string, error)
    GetUsableCredentialsForResource(ctx context.Context, userID, resourceID, resourceType, permission string) ([]string, error)
}
```

### External Services Integration

#### OpenBao (Credential Storage)
OpenBao provides secure credential storage with:

**Features:**
- **AES-256-GCM encryption** for data at rest
- **Envelope encryption** for key management
- **Transit secrets engine** for encryption/decryption
- **Audit logging** for compliance
- **High availability** with clustering support

**Integration:**
- Uses OpenBao's KV secrets engine for credential storage
- Automatic encryption/decryption via transit engine
- Token-based authentication with role-based access
- Comprehensive audit trails

#### SpiceDB (Authorization)
SpiceDB provides fine-grained authorization with:

**Features:**
- **Relationship-based permissions** (Zanzibar model)
- **Real-time consistency** for authorization decisions
- **Schema-driven** permission model
- **Horizontal scalability** for high-throughput systems
- **Strong consistency** guarantees

**Schema Design:**
```zed
definition user {}

definition group {
    relation member: user | group
}

definition credential {
    relation owner: user
    relation reader: user | group
    relation writer: user | group
    permission read = reader + owner
    permission write = writer + owner
    permission delete = owner
}

definition compute_resource {
    relation credential: credential
    relation reader: user | group
    relation writer: user | group
    permission read = reader + owner
    permission write = writer + owner
    permission use = credential->read
}

definition storage_resource {
    relation credential: credential
    relation reader: user | group
    relation writer: user | group
    permission read = reader + owner
    permission write = writer + owner
    permission use = credential->read
}
```

### Credential Lifecycle

#### 1. Credential Creation
```
User Request → VaultService → OpenBao (Store) → SpiceDB (Owner Permission)
     ↓              ↓              ↓                    ↓
  Validation → Business Logic → Encryption → Authorization Setup
```

#### 2. Credential Access
```
User Request → VaultService → SpiceDB (Check Permission) → OpenBao (Retrieve)
     ↓              ↓              ↓                        ↓
  Validation → Business Logic → Authorization Check → Decryption
```

#### 3. Credential Sharing
```
User Request → VaultService → SpiceDB (Add Permission) → Audit Log
     ↓              ↓              ↓                      ↓
  Validation → Business Logic → Permission Update → Compliance
```

### Security Model

#### Encryption at Rest
- **OpenBao Transit Engine**: All credentials encrypted with AES-256-GCM
- **Key Derivation**: Argon2id for memory-hard key derivation
- **Envelope Encryption**: Separate encryption keys for each credential
- **Key Rotation**: Support for automatic key rotation

#### Access Control
- **Relationship-Based**: SpiceDB's Zanzibar model for fine-grained permissions
- **Hierarchical Groups**: Support for nested group memberships
- **Resource Binding**: Credentials can be bound to specific compute/storage resources
- **Permission Inheritance**: Group permissions inherited by members

#### Audit and Compliance
- **Complete Audit Trail**: All credential operations logged
- **Access Tracking**: Who accessed what credentials when
- **Compliance Reporting**: Built-in reports for security audits
- **Retention Policies**: Configurable audit log retention

### Integration with Resource Management

#### Credential-Resource Binding
Credentials can be bound to specific resources for enhanced security:

```go
// Bind credential to compute resource
err := authz.BindCredentialToResource(ctx, credentialID, resourceID, "compute_resource")

// Get usable credentials for a resource
credentials, err := authz.GetUsableCredentialsForResource(ctx, userID, resourceID, "compute_resource", "read")
```

#### Resource-Specific Access
- **Compute Resources**: Credentials bound to specific SLURM clusters, Kubernetes namespaces, or bare metal systems
- **Storage Resources**: Credentials bound to specific S3 buckets, NFS mounts, or SFTP servers
- **Dynamic Binding**: Credentials can be dynamically bound/unbound from resources

### Performance and Scalability

#### Caching Strategy
- **Permission Caching**: SpiceDB permission checks cached for performance
- **Credential Caching**: Frequently accessed credentials cached in memory
- **Cache Invalidation**: Automatic cache invalidation on permission changes

#### High Availability
- **OpenBao Clustering**: Multi-node OpenBao deployment for HA
- **SpiceDB Clustering**: Distributed SpiceDB deployment for scalability
- **Failover Support**: Automatic failover to backup services

#### Monitoring and Observability
- **Metrics Collection**: Comprehensive metrics for credential operations
- **Health Checks**: Service health monitoring for OpenBao and SpiceDB
- **Alerting**: Proactive alerting for security events and service issues

### Migration from Legacy System

The new credential management system replaces the previous in-memory ACL system with:

#### Removed Components
- **In-memory ACL maps**: Replaced with SpiceDB relationships
- **Database ACL tables**: Simplified to basic credential metadata
- **Custom encryption**: Replaced with OpenBao's enterprise-grade encryption

#### Enhanced Features
- **Enterprise Security**: OpenBao provides industry-standard security
- **Fine-grained Permissions**: SpiceDB enables complex permission models
- **Audit Compliance**: Built-in audit trails for regulatory compliance
- **Scalability**: Distributed architecture supports large-scale deployments

This credential management architecture provides a robust, secure, and scalable foundation for managing sensitive credentials in a distributed scientific computing environment.

## gRPC Worker System

The Airavata Scheduler uses a distributed worker architecture where standalone worker binaries communicate with the scheduler via gRPC. This design enables:

- **Scalability**: Workers can be deployed across multiple compute resources
- **Isolation**: Worker failures don't affect the scheduler
- **Flexibility**: Workers can be deployed on different platforms (SLURM, Kubernetes, Bare Metal)
- **Efficiency**: Direct binary deployment without container overhead

### Worker Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Scheduler Server                            │
├─────────────────────────────────────────────────────────────────┤
│  gRPC Server (Port 50051)                                      │
│  ├── WorkerService (proto/worker.proto)                       │
│  ├── Task Assignment                                          │
│  ├── Status Monitoring                                        │
│  └── Heartbeat Management                                     │
└─────────────────────────────────────────────────────────────────┘
                                │
                                │ gRPC
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Worker Binary                               │
├─────────────────────────────────────────────────────────────────┤
│  gRPC Client                                                   │
│  ├── Task Polling                                             │
│  ├── Status Reporting                                         │
│  └── Heartbeat Sending                                        │
├─────────────────────────────────────────────────────────────────┤
│  Task Execution Engine                                        │
│  ├── Script Generation                                        │
│  ├── Data Staging                                             │
│  ├── Command Execution                                        │
│  └── Result Collection                                        │
└─────────────────────────────────────────────────────────────────┘
```

### Worker Lifecycle

1. **Deployment**: Worker binary is deployed to compute resource via script generation
2. **Registration**: Worker connects to scheduler gRPC server
3. **Task Polling**: Worker continuously polls for available tasks
4. **Task Execution**: Worker executes assigned tasks with proper isolation
5. **Status Reporting**: Worker reports progress and completion status
6. **Cleanup**: Worker cleans up resources and reports final status

### Script Generation for Compute Resources

The system generates runtime-specific scripts for deploying workers:

#### SLURM Scripts
```bash
#!/bin/bash
#SBATCH --job-name=worker_${WORKER_ID}
#SBATCH --time=${WALLTIME}
#SBATCH --cpus-per-task=${CPU_CORES}
#SBATCH --mem=${MEMORY_MB}

# Download and execute worker binary
curl -L "${WORKER_BINARY_URL}" -o worker
chmod +x worker
./worker --server-address=${SERVER_ADDRESS}:${SERVER_PORT}
```

#### Kubernetes Manifests
```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: worker-${WORKER_ID}
spec:
  template:
    spec:
      containers:
      - name: worker
        image: worker-binary:latest
        command: ["./worker"]
        args: ["--server-address=${SERVER_ADDRESS}:${SERVER_PORT}"]
        resources:
          requests:
            cpu: "${CPU_CORES}"
            memory: "${MEMORY_MB}Mi"
```

#### Bare Metal Scripts
```bash
#!/bin/bash
# Download and execute worker binary
curl -L "${WORKER_BINARY_URL}" -o worker
chmod +x worker
timeout ${WALLTIME_SECONDS} ./worker --server-address=${SERVER_ADDRESS}:${SERVER_PORT}
```

### Worker Configuration

Workers are configured through environment variables and command-line flags:

```bash
# Required configuration
--server-address=localhost:50051    # Scheduler gRPC server address
--worker-id=worker_12345           # Unique worker identifier
--working-dir=/tmp/worker          # Working directory for tasks

# Optional configuration
--heartbeat-interval=30s           # Heartbeat frequency
--task-timeout=1h                  # Maximum task execution time
--log-level=info                   # Logging level
```

### Task Execution Flow

1. **Task Assignment**: Scheduler assigns task to available worker
2. **Data Staging**: Required input files are staged to worker
3. **Script Generation**: Task-specific execution script is generated
4. **Execution**: Worker executes the task in isolated environment
5. **Monitoring**: Scheduler monitors progress via heartbeats
6. **Result Collection**: Output files are collected from worker
7. **Cleanup**: Worker cleans up temporary files and reports completion

## Package Structure

### Core Domain Layer (`core/domain/`)

Contains pure business logic with no external dependencies:

```
core/domain/
├── interface.go      # 6 core domain interfaces
├── model.go         # Domain entities (Experiment, Task, Worker, etc.)
├── enum.go          # Status enums and types (TaskStatus, WorkerStatus, etc.)
├── value.go         # Value objects
├── error.go         # Domain-specific error types
└── event.go         # Domain events for event-driven architecture
```

### Core Services Layer (`core/service/`)

Implements the domain interfaces with business logic:

```
core/service/
├── registry.go      # ResourceRegistry implementation
├── vault.go         # CredentialVault implementation
├── orchestrator.go  # ExperimentOrchestrator implementation
├── scheduler.go     # TaskScheduler implementation
├── datamover.go     # DataMover implementation
├── worker.go        # WorkerLifecycle implementation
├── analytics.go     # Analytics service
├── audit.go         # Audit logging service
├── cache.go         # Cache service
├── event.go         # Event service
├── health.go        # Health check service
├── metric.go        # Metrics service
├── ratelimit.go     # Rate limiting service
└── script_generator.go # Script generation service
```

### Core Ports Layer (`core/port/`)

Defines infrastructure interfaces that services depend on:

```
core/port/
├── database.go      # Database operations interface
├── cache.go         # Caching operations interface
├── events.go        # Event publishing interface
├── security.go      # Authentication/authorization interface
├── storage.go       # File storage interface
├── compute.go       # Compute resource interaction interface
└── metric.go        # Metrics collection interface
```

### Adapters Layer (`adapters/`)

Provides concrete implementations of the ports:

```
adapters/
├── primary/          # Inbound adapters (driving the system)
│   └── http/         # HTTP API handlers
│       └── handlers.go
├── secondary/        # Outbound adapters (driven by the system)
│   └── database/     # PostgreSQL implementation
│       ├── adapter.go
│       └── repositories.go
└── external/         # External system adapters
    ├── compute/      # SLURM, Kubernetes, Bare Metal
    │   ├── slurm.go
    │   ├── kubernetes.go
    │   └── baremetal.go
    └── storage/      # S3, NFS, SFTP
        ├── s3.go
        ├── nfs.go
        └── sftp.go
```

### Application Layer (`app/`)

Handles dependency injection and application wiring:

```
app/
└── bootstrap.go      # Application bootstrap and dependency injection
```

## Data Flow

### Request Flow
```
HTTP Request → Primary Adapter → Domain Service → Secondary Adapter → External System
     ↓              ↓              ↓              ↓              ↓
  Validation → Business Logic → Data Access → Integration → Response
```

### Event Flow
```
Domain Event → Event Port → Event Adapter → WebSocket/Message Queue → Client
     ↓            ↓            ↓              ↓                    ↓
  Business → Infrastructure → Transport → Real-time → User Interface
  Logic      Interface       Layer      Updates     Updates
```

## Key Design Principles

### 1. Dependency Inversion
- High-level modules don't depend on low-level modules
- Both depend on abstractions (interfaces)
- Abstractions don't depend on details

### 2. Single Responsibility
- Each service has one clear purpose
- Each adapter handles one external system
- Each port defines one infrastructure concern

### 3. Interface Segregation
- Clients depend only on interfaces they use
- Small, focused interfaces over large, monolithic ones
- Clear separation between different concerns

### 4. Open/Closed Principle
- Open for extension (new adapters)
- Closed for modification (core domain logic)
- New features added through new adapters

### 5. Testability
- Core domain can be tested in isolation
- Adapters can be mocked for testing
- Clear boundaries enable comprehensive testing

## Technology Integration

### Database Layer
- **PostgreSQL 15+** with GORM v2 ORM
- **Single schema file** approach (no migrations)
- **Repository pattern** for data access
- **Connection pooling** and transaction management

### Caching Layer
- **Redis** for distributed caching
- **In-memory caching** for frequently accessed data
- **Cache invalidation** strategies
- **Performance monitoring** and metrics

### Event System
- **WebSocket** for real-time updates
- **Event sourcing** for audit trails
- **Message queuing** for reliable delivery
- **Event replay** capabilities

### Security
- **JWT tokens** for authentication
- **Role-based access control** (RBAC)
- **AES-256-GCM encryption** for sensitive data
- **Audit logging** for compliance

### Monitoring
- **Prometheus metrics** for system monitoring
- **Health checks** for service availability
- **Distributed tracing** for request tracking
- **Performance profiling** and optimization

## Scalability Considerations

### Horizontal Scaling
- **Stateless services** enable easy scaling
- **Load balancing** across multiple instances
- **Database sharding** for large datasets
- **Caching strategies** to reduce database load

### Performance Optimization
- **Connection pooling** for database efficiency
- **Async processing** for long-running tasks
- **Batch operations** for bulk data processing
- **Resource optimization** algorithms

### Reliability
- **Circuit breakers** for external system failures
- **Retry mechanisms** with exponential backoff
- **Graceful degradation** under load
- **Health monitoring** and auto-recovery

## Deployment Architecture

### Container Deployment
```yaml
services:
  scheduler:
    image: airavata-scheduler:latest
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=postgres://...
      - REDIS_URL=redis://...
  
  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=airavata_scheduler
      - POSTGRES_USER=airavata
      - POSTGRES_PASSWORD=secure_password
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

### Production Considerations
- **High availability** with multiple replicas
- **Load balancing** with health checks
- **Database clustering** for reliability
- **Monitoring and alerting** for operations
- **Backup and recovery** strategies
- **Security hardening** and compliance

This architecture provides a solid foundation for a production-ready distributed task execution system that can scale to serve hundreds of researchers while maintaining clarity, reliability, and performance.