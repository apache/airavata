# Airavata Scheduler

A production-ready distributed task execution system for scientific computing experiments with a crystal-clear hexagonal architecture, cost-based scheduling, and comprehensive data management.

## 🎯 Conceptual Model

The Airavata Scheduler is built around **6 core domain interfaces** that represent the fundamental operations of distributed task execution, with a **gRPC-based worker system** for task execution:

```
┌─────────────────────────────────────────────────────────────────┐
│                    Core Domain Interfaces                       │
├─────────────────────────────────────────────────────────────────┤
│  ResourceRegistry    │  CredentialVault     │  ExperimentOrch   │
│  • Register compute  │  • Secure storage    │  • Create exper   │
│  • Register storage  │  • Unix permissions  │  • Generate tasks │
│  • Validate access   │  • Encrypt/decrypt   │  • Submit for exec│
├─────────────────────────────────────────────────────────────────┤
│  TaskScheduler       │  DataMover          │  WorkerLifecycle  │
│  • Cost optimization │  • 3-hop staging    │  • Spawn workers  │
│  • Worker distrib    │  • Persistent cache │  • gRPC workers   │
│  • Atomic assignment │  • Lineage tracking │  • Task execution │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    gRPC Worker System                          │
├─────────────────────────────────────────────────────────────────┤
│  Worker Binary      │  Script Generation   │  Task Execution   │
│  • Standalone exec  │  • SLURM scripts     │  • Poll for tasks │
│  • gRPC client      │  • K8s manifests     │  • Execute tasks  │
│  • Auto-deployment  │  • Bare metal scripts│  • Report results │
└─────────────────────────────────────────────────────────────────┘
```

### 🏗️ Hexagonal Architecture

The system implements a **clean hexagonal architecture** (ports-and-adapters) with a `core/` directory structure:

```
┌─────────────────────────────────────────────────────────────────┐
│                    Airavata Scheduler                          │
├─────────────────────────────────────────────────────────────────┤
│  Core Domain Layer (Business Logic)                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐              │
│  │domain/      │ │domain/      │ │domain/      │              │
│  │models.go    │ │interface.go │ │enum.go      │              │
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

### 🛠️ Technology Stack

- **Language**: Go 1.21+
- **Database**: PostgreSQL 15+ 
- **ORM**: GORM v2 with PostgreSQL driver
- **Storage Adapters**: SFTP, S3, NFS
- **Compute Adapters**: SLURM, Kubernetes, Bare Metal
- **Authentication**: JWT with bcrypt password hashing
- **Credential Storage**: OpenBao with AES-256-GCM encryption
- **Authorization**: SpiceDB with Zanzibar model for fine-grained permissions
- **Data Caching**: PostgreSQL-backed with lineage tracking
- **API Framework**: Gorilla Mux for HTTP routing
- **gRPC**: Protocol Buffers for worker communication
- **Architecture**: Hexagonal (ports-and-adapters) pattern

**Production Ready - Clean hexagonal architecture**

## 🚀 Quick Start

### Cold Start (Recommended for Testing)

For a complete cold start from scratch (no existing containers or volumes):

```bash
# Complete cold start setup - builds everything from scratch
./scripts/setup-cold-start.sh

# This script automatically:
# 1. Validates prerequisites (Go, Docker, ports)
# 2. Downloads Go dependencies
# 3. Generates protobuf files
# 4. Creates deterministic SLURM munge key
# 5. Starts all services with test profile
# 6. Waits for services to be healthy
# 7. Uploads SpiceDB schema
# 8. Builds all binaries
```

### Integration Tests

Run comprehensive integration tests across all compute and storage types:

```bash
# Run integration tests (includes cold start)
./scripts/test/run-integration-tests.sh

# This validates:
# - SLURM clusters (both cluster 1 and cluster 2)
# - Bare metal compute nodes
# - Storage backends (S3/MinIO, SFTP, NFS)
# - Credential management via SpiceDB/OpenBao
# - Workflow execution and task dependencies
# - Worker system and scheduler recovery
# - Multi-runtime experiments
```

### Manual Setup

Get up and running quickly with the Airavata Scheduler:

```bash
# 1. Build all binaries (scheduler, worker, CLI)
make build

# 2. Start services (PostgreSQL, SpiceDB, OpenBao)
# Production mode (default)
docker compose up -d postgres spicedb spicedb-postgres openbao

# Or explicitly use production profile
docker compose --profile prod up -d postgres spicedb spicedb-postgres openbao

# 3. Upload SpiceDB schema
make spicedb-schema-upload

# 4. Run your first experiment
./build/airavata-scheduler run tests/sample_experiment.yml \
  --project my-project \
  --compute cluster-1 \
  --storage s3-bucket-1 \
  --watch
```

For detailed setup instructions, see the [Quick Start Guide](docs/guides/quickstart.md).

## 🖥️ Command Line Interface

The Airavata Scheduler includes a comprehensive CLI (`airavata`) for complete system management:

### Complete Workflow Example

```bash
# 1. Authenticate
./bin/airavata auth login

# 2. Create project
./bin/airavata project create

# 3. Upload input data
./bin/airavata data upload input.dat minio-storage:/experiments/input.dat

# 4. Run experiment
./bin/airavata experiment run experiment.yml --project proj-123 --compute slurm-1

# 5. Monitor experiment
./bin/airavata experiment watch exp-456

# 6. Check outputs
./bin/airavata experiment outputs exp-456

# 7. Download results
./bin/airavata experiment download exp-456 --output ./results/
```

### Key CLI Features

- **Data Management**: Upload/download files and directories to/from any storage type
- **Experiment Lifecycle**: Run, monitor, cancel, pause, resume, and retry experiments
- **Output Collection**: Download experiment outputs organized by task with archive support
- **Project Management**: Create projects, manage team members, and organize experiments
- **Resource Management**: Register compute/storage resources with credential binding and verification
- **Real-time Monitoring**: Watch experiments with live status updates and logs

### CLI Command Groups

```bash
# Authentication and configuration
airavata auth login|logout|status
airavata config set|get|show

# User and project management
airavata user profile|update|password|groups|projects
airavata project create|list|get|update|delete|members|add-member|remove-member

# Resource management
airavata resource compute list|get|create|update|delete
airavata resource storage list|get|create|update|delete
airavata resource credential list|create|delete
airavata resource bind-credential|unbind-credential|test-credential
airavata resource status|metrics|test

# Data management
airavata data upload|upload-dir|download|download-dir|list

# Experiment management
airavata experiment run|status|watch|list|outputs|download
airavata experiment cancel|pause|resume|logs|resubmit|retry
airavata experiment tasks|task
```

For complete CLI documentation, see [CLI Reference](docs/reference/cli.md).

### Cold-Start Testing (Fresh Clone)

For testing on a fresh clone with only Docker and Go:

```bash
# Clone and enter directory
git clone <repo-url>
cd airavata-scheduler

# Run cold-start setup
chmod +x scripts/*.sh
./scripts/setup-cold-start.sh

# Run all tests
make cold-start-test

# Or run individual test suites
make test-unit
make test-integration
```

**Prerequisites**: Docker and Go 1.21+ must be in PATH.

## 🔐 Credential Management Architecture

The Airavata Scheduler implements a **three-layer credential architecture** that separates authorization logic from storage for maximum security and scalability:

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

### Key Benefits

- **🔒 Separation of Concerns**: Authorization (SpiceDB) separate from secret storage (OpenBao)
- **🛡️ Fine-grained Permissions**: Read/write/delete permissions with hierarchical group inheritance
- **📋 Complete Audit Trail**: All operations logged across all three systems
- **🔄 Credential Rotation**: Support for automatic key rotation with zero downtime
- **👥 Group Management**: Groups can contain groups with transitive permission inheritance
- **🔗 Resource Binding**: Credentials bound to specific compute/storage resources

### Quick Start

```bash
# 1. Start all services including SpiceDB and OpenBao
make docker-up
make wait-services
make spicedb-schema-upload

# 2. Verify services
curl -s http://localhost:8200/v1/sys/health | jq  # OpenBao
curl -s http://localhost:50052/healthz            # SpiceDB

# 3. Create and share credentials via API
curl -X POST http://localhost:8080/api/v1/credentials \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name": "cluster-ssh", "type": "ssh_key", "data": "..."}'

# 4. Share with group
curl -X POST http://localhost:8080/api/v1/credentials/cred-123/share \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"principal_type": "group", "principal_id": "team-1", "permission": "read"}'

# 5. Bind to resource
curl -X POST http://localhost:8080/api/v1/credentials/cred-123/bind \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"resource_type": "compute", "resource_id": "cluster-1"}'
```

### Credential Resolution Flow

When an experiment runs, the system automatically:

1. **Identifies Required Resources**: Determines compute and storage resources needed
2. **Finds Bound Credentials**: Queries SpiceDB for credentials bound to each resource
3. **Checks User Permissions**: Verifies user has read access to each credential
4. **Retrieves Secrets**: Decrypts credential data from OpenBao
5. **Uses for Execution**: Provides credentials to workers for resource access

### Documentation

- **[Quick Start Guide](docs/guides/quickstart.md)** - Get up and running quickly
- **[Credential Management](docs/guides/credential-management.md)** - Complete credential system guide
- **[Deployment Guide](docs/guides/deployment.md)** - Production deployment instructions
- **[API Reference](docs/reference/api.md)** - Complete API documentation
- **[Architecture Overview](docs/reference/architecture.md)** - System design and patterns

## 📁 Project Structure

```
airavata-scheduler/
├── core/                     # Core application code
│   ├── domain/              # Business logic and entities
│   │   ├── interface.go     # 6 core domain interfaces
│   │   ├── model.go         # Domain entities
│   │   ├── enum.go          # Status enums and types
│   │   ├── value.go         # Value objects
│   │   ├── error.go         # Domain-specific errors
│   │   └── event.go         # Domain events
│   ├── service/             # Service implementations
│   │   ├── registry.go      # ResourceRegistry implementation
│   │   ├── vault.go         # CredentialVault implementation
│   │   ├── orchestrator.go  # ExperimentOrchestrator implementation
│   │   ├── scheduler.go     # TaskScheduler implementation
│   │   ├── datamover.go     # DataMover implementation
│   │   └── worker.go        # WorkerLifecycle implementation
│   ├── port/                # Infrastructure interfaces
│   │   ├── database.go      # Database operations
│   │   ├── cache.go         # Caching operations
│   │   ├── events.go        # Event publishing
│   │   ├── security.go      # Authentication/authorization
│   │   ├── storage.go       # File storage
│   │   ├── compute.go       # Compute resource interaction
│   │   └── metric.go        # Metrics collection
│   ├── dto/                 # Data transfer objects
│   │   ├── *.pb.go          # Generated protobuf types
│   │   └── *_grpc.pb.go     # Generated gRPC service code
│   ├── app/                 # Application bootstrap
│   │   ├── bootstrap.go     # Dependency injection and wiring
│   │   └── factory.go       # Service factories
│   ├── cmd/                 # Main application entry point
│   │   └── main.go          # Scheduler server binary
│   └── util/                # Utility functions
│       ├── common.go        # Common utilities
│       ├── analytics.go     # Analytics utilities
│       └── websocket.go     # WebSocket utilities
├── adapters/                # External system integrations
│   ├── handler_http.go      # HTTP API handlers
│   ├── handler_websocket.go # WebSocket handlers
│   ├── handler_grpc_worker.go # gRPC worker service
│   ├── database_postgres.go # PostgreSQL implementation
│   ├── cache_inmemory.go    # In-memory cache
│   ├── events_inmemory.go   # In-memory events
│   ├── security_jwt.go      # JWT authentication
│   ├── metrics_prometheus.go # Prometheus metrics
│   ├── compute_slurm.go     # SLURM compute adapter
│   ├── compute_kubernetes.go # Kubernetes compute adapter
│   ├── compute_baremetal.go # Bare metal compute adapter
│   ├── storage_s3.go        # S3 storage adapter
│   ├── storage_nfs.go       # NFS storage adapter
│   ├── storage_sftp.go      # SFTP storage adapter
│   ├── script_config.go     # Script generation config
│   └── utils.go             # Adapter utilities
├── cmd/                     # Application binaries
│   ├── worker/              # Worker binary
│   │   └── main.go          # Worker gRPC client
│   └── cli/                 # Command Line Interface
│       ├── main.go          # Root CLI commands and experiment management
│       ├── auth.go          # Authentication commands
│       ├── user.go          # User profile and account management
│       ├── resources.go     # Resource management (compute, storage, credentials)
│       ├── data.go          # Data upload/download commands
│       ├── project.go       # Project management commands
│       └── config.go        # Configuration management
├── proto/                   # Protocol buffer definitions
│   ├── worker.proto         # Worker gRPC service
│   ├── scheduler.proto      # Scheduler gRPC service
│   └── *.proto              # Other proto definitions
├── db/                      # Database schema and migrations
│   ├── schema.sql           # Main database schema
│   └── migrations/          # Database migrations
├── build/                   # Compiled binaries (gitignored)
│   ├── scheduler            # Scheduler server binary
│   └── worker               # Worker binary
├── tests/                   # Test suites
│   ├── unit/                # Unit tests
│   ├── integration/         # Integration tests
│   ├── performance/         # Performance tests
│   └── testutil/            # Test utilities
├── scripts/                 # Build and deployment scripts
├── docs/                    # Documentation
│   ├── architecture.md      # System architecture
│   ├── development.md       # Development guide
│   ├── deployment.md        # Deployment guide
│   ├── api.md              # API documentation
│   └── api_openapi.yaml    # OpenAPI specification
├── Makefile                 # Build automation
├── docker-compose.yml       # Docker services
└── go.mod                   # Go module definition
```

## 🔧 Development

### Prerequisites

- Go 1.21+
- PostgreSQL 15+
- Docker (for testing)

### Setup

```bash
# Clone repository
git clone https://github.com/apache/airavata/scheduler.git
cd airavata-scheduler

# Install dependencies
go mod download

# Generate proto code
make proto

# Or manually
protoc --go_out=core/dto --go-grpc_out=core/dto \
    --go_opt=paths=source_relative \
    --go-grpc_opt=paths=source_relative \
    --proto_path=proto \
    proto/*.proto

# Build binaries
make build

# Setup database
createdb airavata_scheduler
psql airavata_scheduler < db/schema.sql

# Run scheduler server
./build/scheduler --mode=server

# Run worker (in separate terminal)
./build/worker --server-address=localhost:50051
```

### Docker Compose Profiles

The project uses a single `docker-compose.yml` file with profiles to support different environments:

```bash
# Production mode (default) - Core services only
docker compose up -d

# Test mode - Full test environment with compute services
docker compose --profile test up -d
```

**Test Profile (`test`):**
- 2 SLURM clusters with controllers and compute nodes
- 2 baremetal SSH servers for direct execution
- Kubernetes-in-Docker (kind) cluster
- Standard healthcheck intervals and timeouts
- Used for integration testing with production-like environment

### Testing

```bash
# Run all tests
make test

# Run unit tests
make test-unit

# Run integration tests
make test-integration

# Run performance tests
make test-performance

# Run tests with coverage
make test-coverage

# Run cold start test with CSV report (destroys containers/volumes)
make cold-start-test-csv
```

#### Cold Start Testing with CSV Reports

For comprehensive testing from a clean state with detailed reporting:

```bash
# Full cold start test with CSV report generation
make cold-start-test-csv

# Or run directly with options
./scripts/test/run-cold-start-with-report.sh [OPTIONS]

# Options:
#   --skip-cleanup        Skip Docker cleanup
#   --skip-cold-start     Skip cold start setup
#   --unit-only          Run only unit tests
#   --integration-only   Run only integration tests
#   --no-csv             Skip CSV report generation
```

This will:
1. **Destroy all containers and volumes** for a true cold start
2. **Recreate environment from scratch** using `scripts/setup-cold-start.sh`
3. **Run all test suites** (unit + integration) with JSON output
4. **Generate CSV report** with detailed test results in `logs/cold-start-test-results-[timestamp].csv`

**CSV Report Format:**
```
Category,Test Name,Status,Duration (s),Warnings/Notes
Unit,TestExample,PASS,0.123,
Integration,TestE2E,FAIL,45.67,Timeout waiting for service
```

**Status Types:**
- `PASS`: Test passed without issues
- `FAIL`: Test failed with errors  
- `SKIP`: Test skipped (service unavailable, etc.)
- `PASS_WITH_WARNING`: Test passed but had warnings

**Generated Files:**
- `logs/cold-start-test-results-[timestamp].csv` - Detailed test results
- `logs/unit-tests-[timestamp].json` - Unit test JSON output
- `logs/integration-tests-[timestamp].json` - Integration test JSON output
- `logs/cold-start-setup-[timestamp].log` - Cold start setup log

## 📊 Key Features

### 🎯 Cost-Based Scheduling
- Multi-objective optimization (time, cost, deadline)
- Dynamic resource allocation
- Intelligent task distribution

### 🔒 Enterprise Security
- **OpenBao Integration**: AES-256-GCM encryption with envelope encryption
- **SpiceDB Authorization**: Fine-grained permissions with Zanzibar model
- **Complete Audit Trail**: All credential operations logged for compliance
- **JWT-based Authentication**: Secure user authentication and session management
- **Credential Rotation**: Support for automatic key rotation and lifecycle management
- **Group Management**: Hierarchical group memberships with permission inheritance

### 📈 Real-Time Monitoring
- WebSocket-based progress tracking
- Prometheus metrics
- Health checks and system status
- Comprehensive logging

### 🔄 Data Management
- 3-hop data staging (Central → Compute → Worker → Compute → Central)
- Persistent caching with lineage tracking
- Automatic data integrity verification
- Support for multiple storage backends
- **Output Collection API**: List and download experiment outputs organized by task ID
- **Archive Generation**: Download all experiment outputs as a single tar.gz archive
- **Individual File Access**: Download specific output files with checksum verification

### 🖥️ Command Line Interface
- **Complete CLI**: Full-featured command-line interface for all system operations
- **Data Management**: Upload/download files and directories to/from any storage type
- **Experiment Lifecycle**: Run, monitor, cancel, pause, resume, and retry experiments
- **Project Management**: Create projects, manage team members, and organize experiments
- **Resource Management**: Register compute/storage resources with credential binding and verification
- **Real-time Monitoring**: Watch experiments with live status updates and logs
- **Credential Security**: Verification-based credential binding with access testing

### 🚀 Scalability
- Horizontal scaling with multiple workers
- Rate limiting and resource management
- Caching layer for improved performance
- Event-driven architecture

## 📚 Documentation

- [Architecture Guide](docs/reference/architecture.md) - System design and patterns
- [CLI Reference](docs/reference/cli.md) - Complete command-line interface documentation
- [Development Guide](docs/guides/development.md) - Development workflow and best practices
- [Deployment Guide](docs/guides/deployment.md) - Production deployment instructions
- [API Documentation](docs/reference/api_openapi.yaml) - Complete API specification
- [Testing Guide](tests/README.md) - Comprehensive testing documentation
- [Dashboard Integration](docs/guides/dashboard-integration.md) - Frontend integration guide
- [WebSocket Protocol](docs/reference/websocket-protocol.md) - Real-time communication protocol

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes following the hexagonal architecture
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## ⚠️ Known Issues & Troubleshooting

### Cold Start Testing

When running cold-start tests from a fresh clone, ensure:

1. **SLURM Munge Key**: Run `./scripts/generate-slurm-munge-key.sh` to generate the shared authentication key
2. **Kubernetes Cluster**: The kind-cluster service may take 2-3 minutes to fully initialize
3. **Service Dependencies**: All services have proper health checks and startup dependencies

### Service Health Checks

All services now include health checks:
- **Scheduler**: HTTP health check on `/api/v2/health`
- **SLURM**: Munge authentication with shared key
- **Kubernetes**: Node readiness check
- **Storage**: Connection and availability checks

### Common Issues

- **SLURM Authentication Failures**: Regenerate munge key if nodes can't register
- **Kubernetes Cluster**: Kind cluster initialization is complex and may require manual setup
- **Port Conflicts**: Ensure ports 8080, 50051-50053, 5432, 8200, 9000-9001, 2222, 2049 are available

## 🏆 Production Ready

This system is designed for production deployment with:
- ✅ Clean hexagonal architecture
- ✅ Comprehensive error handling
- ✅ Security best practices
- ✅ Monitoring and observability
- ✅ Scalability and performance
- ✅ Single authoritative database schema
- ✅ Event-driven real-time updates
- ✅ Complete API documentation