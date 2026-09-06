# Development Guide

## Overview

This guide explains the development workflow, patterns, and best practices for the Airavata Scheduler. The system follows a clean hexagonal architecture that promotes maintainability, testability, and extensibility.

**For practical setup instructions, see [Testing Guide](../tests/README.md)**

## Development Philosophy

### Hexagonal Architecture

The Airavata Scheduler follows a hexagonal architecture (ports-and-adapters pattern):

1. **Core Domain Layer** (`core/domain/`): Pure business logic with no external dependencies
2. **Core Services Layer** (`core/service/`): Implementation of domain interfaces
3. **Core Ports Layer** (`core/port/`): Infrastructure interfaces that services depend on
4. **Adapters Layer** (`adapters/`): Concrete implementations of infrastructure ports
5. **Application Layer** (`core/app/`): Dependency injection and application wiring

### Key Principles

- **Dependency Inversion**: Core domain depends on abstractions, not concretions
- **Interface Segregation**: Small, focused interfaces over large, monolithic ones
- **Single Responsibility**: Each component has one clear purpose
- **Testability**: All components are easily testable in isolation
- **Extensibility**: New features added through new adapters without modifying core logic

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

**Key Files:**
- `interface.go`: Defines the 6 core domain interfaces
- `model.go`: Contains domain entities like `Experiment`, `Task`, `Worker`
- `enum.go`: Contains status enums like `TaskStatus`, `WorkerStatus`
- `value.go`: Contains value objects
- `error.go`: Domain-specific error types and constants
- `event.go`: Domain events for event-driven architecture

### Services Layer (`services/`)

Implements the domain interfaces with business logic:

```
services/
├── registry/         # ResourceRegistry implementation
│   ├── service.go    # Core resource management logic
│   └── factory.go    # Service factory
├── vault/            # CredentialVault implementation
│   ├── service.go    # Credential management logic
│   └── factory.go    # Service factory
├── orchestrator/     # ExperimentOrchestrator implementation
│   ├── service.go    # Experiment lifecycle management
│   └── factory.go    # Service factory
├── scheduler/        # TaskScheduler implementation
│   ├── service.go    # Cost-based scheduling logic
│   └── factory.go    # Service factory
├── datamover/        # DataMover implementation
│   ├── service.go    # Data staging and caching logic
│   └── factory.go    # Service factory
└── worker/           # WorkerLifecycle implementation
    ├── service.go    # Worker management logic
    └── factory.go    # Service factory
```

**Key Patterns:**
- Each service implements one domain interface
- Services depend only on ports (infrastructure interfaces)
- Factory functions for service creation with dependency injection
- No direct dependencies on external systems

### Ports Layer (`ports/`)

Defines infrastructure interfaces that services depend on:

```
ports/
├── database.go       # Database operations interface
├── cache.go          # Caching operations interface
├── events.go         # Event publishing interface
├── security.go       # Authentication/authorization interface
├── storage.go        # File storage interface
├── compute.go        # Compute resource interaction interface
└── metrics.go        # Metrics collection interface
```

**Key Patterns:**
- Each port defines one infrastructure concern
- Ports are implemented by adapters
- Services depend on ports, not concrete implementations
- Ports enable easy testing with mocks

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

**Key Patterns:**
- Primary adapters drive the system (HTTP, CLI, etc.)
- Secondary adapters are driven by the system (Database, Cache, etc.)
- External adapters integrate with third-party systems
- Each adapter implements one or more ports

### Application Layer (`app/`)

Handles dependency injection and application wiring:

```
app/
└── bootstrap.go      # Application bootstrap and dependency injection
```

**Key Patterns:**
- Single bootstrap function that wires all dependencies
- Configuration-driven setup
- Clean separation of concerns
- Easy to test and mock

## Development Workflow

### 1. Understanding the Codebase

**Start with Domain**: Begin by understanding the core business logic:
- `domain/interfaces.go`: The 6 core interfaces
- `domain/models.go`: Domain entities and their relationships
- `domain/value_objects.go`: Value objects and enums
- `domain/errors.go`: Domain-specific error handling

**Study Services**: Understand the business logic implementations:
- `services/*/service.go`: Core business logic
- `services/*/factory.go`: Service creation and dependency injection

**Examine Ports**: Understand the infrastructure interfaces:
- `ports/*.go`: Infrastructure interfaces that services depend on

**Review Adapters**: See how external systems are integrated:
- `adapters/primary/`: HTTP API handlers
- `adapters/secondary/`: Database and cache implementations
- `adapters/external/`: Third-party system integrations

### 2. Adding New Features

#### Adding a New Domain Service

1. **Define the interface** in `domain/interfaces.go`:
```go
type NewService interface {
    DoSomething(ctx context.Context, req *DoSomethingRequest) (*DoSomethingResponse, error)
}
```

2. **Create the service implementation** in `services/newservice/`:
```go
// services/newservice/service.go
type Service struct {
    repo ports.RepositoryPort
    cache ports.CachePort
}

func (s *Service) DoSomething(ctx context.Context, req *domain.DoSomethingRequest) (*domain.DoSomethingResponse, error) {
    // Business logic implementation
}
```

3. **Create the factory** in `services/newservice/factory.go`:
```go
func NewFactory(repo ports.RepositoryPort, cache ports.CachePort) domain.NewService {
    return &Service{
        repo: repo,
        cache: cache,
    }
}
```

4. **Wire the service** in `app/bootstrap.go`:
```go
newService := newservice.NewFactory(repo, cache)
```

#### Adding a New Adapter

1. **Implement the port interface**:
```go
// adapters/secondary/newsystem/adapter.go
type Adapter struct {
    client *NewSystemClient
}

func (a *Adapter) DoSomething(ctx context.Context, req *ports.DoSomethingRequest) (*ports.DoSomethingResponse, error) {
    // External system integration
}
```

2. **Register the adapter** in `app/bootstrap.go`:
```go
newSystemAdapter := newsystem.NewAdapter(config)
```

### 3. Testing Strategy

#### Unit Testing

Test services in isolation using mocks:

```go
func TestExperimentService_CreateExperiment(t *testing.T) {
    // Arrange
    mockRepo := &MockRepository{}
    mockCache := &MockCache{}
    service := orchestrator.NewFactory(mockRepo, mockCache)
    
    // Act
    result, err := service.CreateExperiment(ctx, req)
    
    // Assert
    assert.NoError(t, err)
    assert.NotNil(t, result)
}
```

#### Integration Testing

Test with real infrastructure:

```go
func TestExperimentService_Integration(t *testing.T) {
    // Setup test database
    db := setupTestDatabase(t)
    defer cleanupTestDatabase(t, db)
    
    // Create real services
    app := app.Bootstrap(testConfig)
    
    // Test with real infrastructure
    result, err := app.ExperimentService.CreateExperiment(ctx, req)
    assert.NoError(t, err)
}
```

#### Adapter Testing

Test adapters with real external systems:

```go
func TestSlurmAdapter_Integration(t *testing.T) {
    if !*integration {
        t.Skip("Integration tests disabled")
    }
    
    adapter := slurm.NewAdapter(slurmConfig)
    
    // Test with real SLURM cluster
    result, err := adapter.SpawnWorker(ctx, 1*time.Hour)
    assert.NoError(t, err)
}
```

### 4. Code Organization

#### File Naming Conventions

- **Services**: `service.go` for implementation, `factory.go` for creation
- **Adapters**: `adapter.go` for main implementation, `repositories.go` for data access
- **Tests**: `*_test.go` with descriptive names
- **Configuration**: `config.go` or embedded in bootstrap

#### Import Organization

```go
import (
    // Standard library
    "context"
    "fmt"
    
    // Third-party packages
    "github.com/gorilla/mux"
    "gorm.io/gorm"
    
    // Internal packages
    "github.com/apache/airavata/scheduler/domain"
    "github.com/apache/airavata/scheduler/ports"
    "github.com/apache/airavata/scheduler/services/orchestrator"
)
```

#### Error Handling

Use domain-specific errors:

```go
// In domain/errors.go
var (
    ErrExperimentNotFound = errors.New("experiment not found")
    ErrInvalidParameter   = errors.New("invalid parameter")
)

// In service
func (s *Service) GetExperiment(ctx context.Context, id string) (*domain.Experiment, error) {
    experiment, err := s.repo.GetExperimentByID(ctx, id)
    if err != nil {
        if errors.Is(err, gorm.ErrRecordNotFound) {
            return nil, domain.ErrExperimentNotFound
        }
        return nil, fmt.Errorf("failed to get experiment: %w", err)
    }
    return experiment, nil
}
```

### 5. Performance Considerations

#### Database Optimization

- Use connection pooling
- Implement proper indexing
- Use batch operations for bulk data
- Monitor query performance

#### Caching Strategy

- Cache frequently accessed data
- Implement cache invalidation
- Use appropriate cache TTLs
- Monitor cache hit rates

#### Memory Management

- Use object pooling for frequently created objects
- Implement proper cleanup in adapters
- Monitor memory usage and garbage collection
- Use streaming for large data processing

### 6. Security Best Practices

#### Authentication and Authorization

- Use JWT tokens for stateless authentication
- Implement role-based access control
- Validate all inputs
- Use secure password hashing

#### Data Protection

- Encrypt sensitive data at rest
- Use secure communication protocols
- Implement proper key management
- Audit all security-sensitive operations

#### Input Validation

- Validate all user inputs
- Use whitelist validation where possible
- Implement rate limiting
- Sanitize data before processing

### 7. Monitoring and Observability

#### Logging

- Use structured logging
- Include correlation IDs
- Log at appropriate levels
- Implement log aggregation

#### Metrics

- Collect business metrics
- Monitor system performance
- Track error rates
- Implement alerting

#### Tracing

- Use distributed tracing
- Track request flows
- Monitor external system calls
- Implement performance profiling

## Common Patterns

### Service Pattern

```go
type Service struct {
    repo   ports.RepositoryPort
    cache  ports.CachePort
    events ports.EventPort
}

func (s *Service) DoSomething(ctx context.Context, req *domain.Request) (*domain.Response, error) {
    // 1. Validate input
    if err := s.validateRequest(req); err != nil {
        return nil, err
    }
    
    // 2. Check cache
    if cached, err := s.cache.Get(ctx, req.ID); err == nil {
        return cached, nil
    }
    
    // 3. Business logic
    result, err := s.repo.DoSomething(ctx, req)
    if err != nil {
        return nil, err
    }
    
    // 4. Cache result
    s.cache.Set(ctx, req.ID, result, time.Hour)
    
    // 5. Publish event
    s.events.Publish(ctx, &domain.Event{Type: "SomethingDone", Data: result})
    
    return result, nil
}
```

### Adapter Pattern

```go
type Adapter struct {
    client *ExternalClient
    config *Config
}

func (a *Adapter) DoSomething(ctx context.Context, req *ports.Request) (*ports.Response, error) {
    // 1. Transform request
    externalReq := a.transformRequest(req)
    
    // 2. Call external system
    externalResp, err := a.client.DoSomething(ctx, externalReq)
    if err != nil {
        return nil, fmt.Errorf("external system error: %w", err)
    }
    
    // 3. Transform response
    response := a.transformResponse(externalResp)
    
    return response, nil
}
```

### Factory Pattern

```go
func NewFactory(repo ports.RepositoryPort, cache ports.CachePort, events ports.EventPort) domain.Service {
    return &Service{
        repo:   repo,
        cache:  cache,
        events: events,
    }
}
```

## Troubleshooting

### Common Issues

1. **Import cycles**: Ensure domain doesn't import from services or adapters
2. **Missing interfaces**: Define ports for all external dependencies
3. **Test failures**: Check that mocks implement the correct interfaces
4. **Performance issues**: Profile and optimize database queries and caching

### Debugging Tips

1. **Use structured logging** to trace request flows
2. **Enable debug mode** for detailed error information
3. **Use distributed tracing** to track external system calls
4. **Monitor metrics** to identify performance bottlenecks

### Getting Help

1. **Check the logs** for error messages and stack traces
2. **Review the architecture** to understand the flow
3. **Test in isolation** to identify the problematic component
4. **Use the test suite** to verify expected behavior

## CLI Development

The Airavata Scheduler includes a comprehensive command-line interface built with Cobra.

### CLI Architecture

The CLI follows a modular structure with separate command groups:

```
cmd/cli/
├── main.go           # Root command and experiment management
├── auth.go           # Authentication commands
├── user.go           # User profile and account management
├── resources.go      # Resource management (compute, storage, credentials)
├── data.go           # Data upload/download commands
├── project.go        # Project management commands
└── config.go         # Configuration management
```

### Adding New Commands

To add a new command group:

1. **Create command group function**:
```go
func createNewCommands() *cobra.Command {
    newCmd := &cobra.Command{
        Use:   "new",
        Short: "New command group",
        Long:  "Description of new command group",
    }
    
    // Add subcommands
    newCmd.AddCommand(createSubCommand())
    
    return newCmd
}
```

2. **Add to root command** in `main.go`:
```go
rootCmd.AddCommand(createNewCommands())
```

3. **Implement command functions**:
```go
func executeNewCommand(cmd *cobra.Command, args []string) error {
    // Command implementation
    return nil
}
```

### CLI Patterns

#### Authentication Check
All commands should check authentication:
```go
configManager := NewConfigManager()
if !configManager.IsAuthenticated() {
    return fmt.Errorf("not authenticated - run 'airavata auth login' first")
}
```

#### API Communication
Use consistent HTTP client patterns:
```go
ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
defer cancel()

req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
if err != nil {
    return fmt.Errorf("failed to create request: %w", err)
}

req.Header.Set("Authorization", "Bearer "+token)
```

#### Error Handling
Provide clear, actionable error messages:
```go
if resp.StatusCode != http.StatusOK {
    return fmt.Errorf("failed to %s: %s", operation, string(body))
}
```

#### Progress Feedback
Show progress for long-running operations:
```go
fmt.Printf("📤 Uploading %s...\n", filename)
// ... operation ...
fmt.Printf("✅ Upload completed successfully!\n")
```

### CLI Testing

Test CLI commands with:
```bash
# Test command help
./bin/airavata --help
./bin/airavata experiment --help

# Test command execution
./bin/airavata auth status
./bin/airavata resource compute list
```

### CLI Documentation

Update documentation when adding new commands:
1. Add to `docs/reference/cli.md`
2. Include examples and usage patterns
3. Update README.md if adding major features

## Building the Worker Binary

### Proto Code Generation

The system uses Protocol Buffers for gRPC communication. Generate proto code before building:

```bash
# Install protoc and Go plugins
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest

# Generate proto code
make proto

# Or manually
protoc --go_out=core/dto --go-grpc_out=core/dto \
    --go_opt=paths=source_relative \
    --go-grpc_opt=paths=source_relative \
    --proto_path=proto \
    proto/*.proto
```

### SLURM Munge Key Generation

For integration testing with SLURM clusters, the system uses a deterministic munge key to ensure reproducible authentication across all SLURM nodes:

```bash
# Generate deterministic munge key for SLURM clusters
./scripts/generate-slurm-munge-key.sh

# This creates tests/docker/slurm/shared-munge.key with:
# - Deterministic content based on fixed seed "airavata-munge-test-seed-v1"
# - 1024-byte binary key generated from SHA256 hashes
# - Same key used across all SLURM containers for consistent authentication
```

**Key Features:**
- **Deterministic**: Same key generated every time from fixed seed
- **Shared**: All SLURM containers (controllers and nodes) use identical key
- **Secure**: 1024-byte binary key suitable for production use
- **Reproducible**: Enables consistent cold-start testing

**Verification:**
```bash
# Verify all containers share the same munge key
docker exec airavata-scheduler-slurm-cluster-01-1 sha256sum /etc/munge/munge.key
docker exec airavata-scheduler-slurm-cluster-02-1 sha256sum /etc/munge/munge.key
docker exec airavata-scheduler-slurm-node-01-01-1 sha256sum /etc/munge/munge.key
docker exec airavata-scheduler-slurm-node-02-01-1 sha256sum /etc/munge/munge.key
# All should output identical SHA256 hash
```

### Building Both Binaries

```bash
# Build both scheduler and worker
make build

# Or build individually
make build-server  # Builds build/scheduler
make build-worker  # Builds build/worker

# Verify binaries
./build/scheduler --help
./build/worker --help
```

### Development Workflow

```bash
# 1. Generate proto code
make proto

# 2. Build binaries
make build

# 3. Run scheduler
./build/scheduler --mode=server

# 4. Run worker (in separate terminal)
./build/worker --server-address=localhost:50051
```

## Testing Worker Communication

### Unit Tests

```bash
# Test worker gRPC client
go test ./cmd/worker -v

# Test scheduler gRPC server
go test ./adapters -v -run TestWorkerService
```

### Integration Tests

```bash
# Start test services
docker compose --profile test up -d

# Test worker integration
go test ./tests/integration -v -run TestWorkerIntegration

# Clean up
docker compose --profile test down
```

### Manual Testing

```bash
# Test gRPC connectivity
grpcurl -plaintext localhost:50051 list
grpcurl -plaintext localhost:50051 worker.WorkerService/ListWorkers

# Test worker registration
./build/worker --server-address=localhost:50051 --worker-id=test-worker-1
```

## Local Development with Multiple Components

### Development Setup

#### 1. Start Required Services

```bash
# Start all infrastructure services (PostgreSQL, SpiceDB, OpenBao, MinIO, etc.)
make docker-up

# Wait for services to be healthy
make wait-services

# Upload SpiceDB authorization schema
make spicedb-schema

# Verify all services are running
docker compose ps
```

#### 2. Verify Service Connectivity

```bash
# Check PostgreSQL
psql postgres://user:password@localhost:5432/airavata -c "SELECT 1;"

# Check SpiceDB
grpcurl -plaintext -d '{"resource": {"object_type": "credential", "object_id": "test"}}' \
  localhost:50051 authzed.api.v1.PermissionsService/CheckPermission

# Check OpenBao
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='dev-token'
vault status

# Check MinIO
curl http://localhost:9000/minio/health/live
```

#### 3. Run Application Components

```bash
# Terminal 1: Start scheduler
./build/scheduler --mode=server --log-level=debug

# Terminal 2: Start worker
./build/worker --server-address=localhost:50051 --log-level=debug

# Terminal 3: Test API
curl http://localhost:8080/health
curl http://localhost:8080/api/v1/credentials  # Test credential management
```

### Environment Configuration

Create a `.env` file for local development:

```bash
# .env
# PostgreSQL
DATABASE_URL=postgres://user:password@localhost:5432/airavata?sslmode=disable

# SpiceDB
SPICEDB_ENDPOINT=localhost:50051
SPICEDB_TOKEN=somerandomkeyhere
SPICEDB_INSECURE=true

# OpenBao
VAULT_ADDR=http://localhost:8200
VAULT_TOKEN=dev-token
VAULT_MOUNT_PATH=secret

# MinIO
S3_ENDPOINT=localhost:9000
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin
S3_USE_SSL=false

# Server
SERVER_PORT=8080
LOG_LEVEL=debug
```

### Hot Reloading

```bash
# Install air for hot reloading
go install github.com/cosmtrek/air@latest

# Run scheduler with hot reload
air -c .air.toml

# Or use go run
go run ./core/cmd --mode=server
go run ./cmd/worker --server-address=localhost:50051
```

### Debugging

```bash
# Build with debug symbols
go build -gcflags="all=-N -l" -o build/scheduler ./core/cmd
go build -gcflags="all=-N -l" -o build/worker ./cmd/worker

# Run with debugger
dlv exec ./build/scheduler -- --mode=server
dlv exec ./build/worker -- --server-address=localhost:50051
```

## Working with Credentials and Authorization

### SpiceDB Development

#### Testing Permission Checks

```bash
# Install zed CLI
brew install authzed/tap/zed
# or
go install github.com/authzed/zed@latest

# Validate schema
make spicedb-validate

# Read current schema
zed schema read \
  --endpoint localhost:50051 \
  --token "somerandomkeyhere" \
  --insecure

# Write relationships manually (for testing)
zed relationship create \
  --endpoint localhost:50051 \
  --token "somerandomkeyhere" \
  --insecure \
  credential:test-cred owner user:alice
```

#### Query Relationships

```bash
# List all relationships for a credential
zed relationship read \
  --endpoint localhost:50051 \
  --token "somerandomkeyhere" \
  --insecure \
  --filter 'credential:test-cred'

# Check if user has permission
zed permission check \
  --endpoint localhost:50051 \
  --token "somerandomkeyhere" \
  --insecure \
  credential:test-cred read user:alice
```

### OpenBao Development

#### Working with Secrets

```bash
# Set environment
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='dev-token'

# Enable KV v2 engine (if not already enabled)
vault secrets enable -version=2 -path=secret kv

# Store a test credential
vault kv put secret/credentials/test-key \
  type=ssh_key \
  data="$(cat ~/.ssh/id_rsa)"

# Retrieve credential
vault kv get secret/credentials/test-key

# List all credentials
vault kv list secret/credentials/

# Delete credential
vault kv delete secret/credentials/test-key
```

#### Working with Policies

```bash
# Create a test policy
cat > test-policy.hcl <<EOF
path "secret/data/credentials/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
EOF

vault policy write test-policy test-policy.hcl

# Create token with policy
vault token create -policy=test-policy

# Test token
VAULT_TOKEN=<new-token> vault kv get secret/credentials/test-key
```

### Integration Testing

#### Test Credential Lifecycle

```go
// tests/integration/credential_test.go
func TestCredentialLifecycle(t *testing.T) {
    suite := testutil.SetupIntegrationTest(t)
    defer suite.Cleanup()
    
    // Start SpiceDB and OpenBao
    err := suite.StartServices(t, "postgres", "spicedb", "openbao")
    require.NoError(t, err)
    
    // Create user
    user, err := suite.CreateUser("test-user", 1001, 1001)
    require.NoError(t, err)
    
    // Create credential (stored in OpenBao)
    cred, err := suite.CreateCredential("test-ssh-key", user.ID)
    require.NoError(t, err)
    
    // Verify ownership in SpiceDB
    owner, err := suite.SpiceDBAdapter.GetCredentialOwner(context.Background(), cred.ID)
    require.NoError(t, err)
    assert.Equal(t, user.ID, owner)
    
    // Share with another user
    user2, err := suite.CreateUser("user2", 1002, 1002)
    require.NoError(t, err)
    
    err = suite.AddCredentialACL(cred.ID, "USER", user2.ID, "read")
    require.NoError(t, err)
    
    // Verify access
    hasAccess := suite.CheckCredentialAccess(cred.ID, user2.ID, "read")
    assert.True(t, hasAccess)
    
    // Retrieve credential data (from OpenBao)
    data, _, err := suite.VaultService.RetrieveCredential(context.Background(), cred.ID, user2.ID)
    require.NoError(t, err)
    assert.NotNil(t, data)
}
```

#### Test Group Hierarchies

```bash
# Via API
curl -X POST http://localhost:8080/api/v1/groups \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name": "engineering"}'

curl -X POST http://localhost:8080/api/v1/groups/engineering/members \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"user_id": "alice", "member_type": "user"}'

curl -X POST http://localhost:8080/api/v1/credentials/cred-123/share \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"principal_type": "group", "principal_id": "engineering", "permission": "read"}'
```

### Troubleshooting Common Issues

#### SpiceDB Connection Issues

```bash
# Check if SpiceDB is running
docker compose ps spicedb

# Check logs
docker compose logs spicedb

# Test connectivity
grpcurl -plaintext localhost:50051 list

# Verify preshared key
grpcurl -plaintext \
  -H "authorization: Bearer somerandomkeyhere" \
  localhost:50051 authzed.api.v1.SchemaService/ReadSchema
```

#### OpenBao Connection Issues

```bash
# Check if OpenBao is running
docker compose ps openbao

# Check logs
docker compose logs openbao

# Test connectivity
vault status

# Check mount points
vault secrets list
```

#### Permission Denied Errors

```bash
# Debug SpiceDB relationships
zed relationship read \
  --endpoint localhost:50051 \
  --token "somerandomkeyhere" \
  --insecure \
  --filter 'credential:YOUR_CRED_ID'

# Check if schema is loaded
make spicedb-schema

# Verify user membership
zed relationship read \
  --endpoint localhost:50051 \
  --token "somerandomkeyhere" \
  --insecure \
  --filter 'group:YOUR_GROUP_ID'
```

#### Secret Not Found Errors

```bash
# List all secrets
vault kv list secret/credentials/

# Check secret metadata
vault kv metadata get secret/credentials/YOUR_CRED_ID

# Verify token has correct policy
vault token lookup
```

## Performance Testing

### Load Testing Credentials

```bash
# Install k6
brew install k6

# Run load test
k6 run tests/performance/credential_load_test.js

# Test concurrent permission checks
k6 run tests/performance/permission_check_load_test.js
```

### Profiling

```bash
# Enable pprof in scheduler
./build/scheduler --mode=server --pprof=true

# Capture CPU profile
go tool pprof http://localhost:6060/debug/pprof/profile?seconds=30

# Capture memory profile
go tool pprof http://localhost:6060/debug/pprof/heap

# Analyze with web interface
go tool pprof -http=:8081 cpu.prof
```

This development guide provides the foundation for working effectively with the Airavata Scheduler's hexagonal architecture. Follow these patterns and principles to maintain code quality and system reliability.

For more detailed information, see:
- [Architecture Guide](architecture.md) - Overall system architecture
- [Credential Architecture](credential_architecture.md) - SpiceDB and OpenBao design
- [Deployment Guide](spicedb_openbao_deployment.md) - Production deployment
- [Worker System Guide](worker-system.md) - Worker architecture
- [Building Guide](building.md) - Build instructions
- [Testing Guide](../tests/README.md) - Testing strategies