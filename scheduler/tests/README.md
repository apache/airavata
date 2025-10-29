# Testing Guide

## Overview

This guide provides comprehensive instructions for running and developing tests for the Airavata Scheduler. The system uses a multi-layered testing approach with unit tests, integration tests, and end-to-end tests following hexagonal architecture principles.

## Prerequisites

- Go 1.21+
- Docker and Docker Compose
- PostgreSQL 15+ (for integration tests)
- Make (optional, for convenience targets)

## Quick Start

### Run All Tests
```bash
make test
# or
go test ./... -v
```

### Run Unit Tests Only
```bash
make test-unit
# or
go test ./tests/unit/... -v
```

### Run Integration Tests

#### Cold Start Integration Tests (Recommended)
```bash
# Complete cold start setup and run integration tests
./scripts/setup-cold-start.sh
./scripts/test/run-integration-tests.sh

# This automatically:
# 1. Validates prerequisites
# 2. Generates deterministic SLURM munge key
# 3. Starts all services with test profile
# 4. Builds binaries
# 5. Runs integration tests
# 6. Cleans up
```

#### Complete Functionality Validation
```bash
# Run complete validation including cold-start, unit tests, and integration tests
./scripts/validate-full-functionality.sh

# This performs:
# 1. Cold-start setup validation
# 2. Unit test execution (30m timeout)
# 3. Integration test execution (60m timeout)
# 4. Summary report generation
# 5. Test coverage analysis
```

#### Cold Start Testing with CSV Reports
```bash
# Full cold start test with detailed CSV report generation
make cold-start-test-csv

# Or run directly with options
./scripts/test/run-cold-start-with-report.sh [OPTIONS]

# Options:
#   --skip-cleanup        Skip Docker cleanup (useful for debugging)
#   --skip-cold-start     Skip cold start setup (assume environment is ready)
#   --unit-only          Run only unit tests
#   --integration-only   Run only integration tests
#   --no-csv             Skip CSV report generation
```

**This comprehensive test will:**
1. **Destroy all containers and volumes** for a true cold start
2. **Recreate environment from scratch** using `scripts/setup-cold-start.sh`
3. **Run all test suites** (unit + integration) with JSON output
4. **Generate detailed CSV report** with test results in `logs/cold-start-test-results-[timestamp].csv`

**CSV Report Features:**
- Test categorization (Unit vs Integration)
- Individual test status (PASS/FAIL/SKIP/PASS_WITH_WARNING)
- Test duration tracking
- Warning/error message capture
- Summary statistics with success rates
- Proper CSV escaping for complex output

**Generated Files:**
- `logs/cold-start-test-results-[timestamp].csv` - Detailed test results
- `logs/unit-tests-[timestamp].json` - Unit test JSON output
- `logs/integration-tests-[timestamp].json` - Integration test JSON output
- `logs/cold-start-setup-[timestamp].log` - Cold start setup log

**CSV Format:**
```
Category,Test Name,Status,Duration (s),Warnings/Notes
Unit,TestExample,PASS,0.123,
Integration,TestE2E,FAIL,45.67,Timeout waiting for service
Integration,TestStorage,PASS_WITH_WARNING,12.34,Service took longer than expected
```

#### Manual Integration Test Setup
```bash
# Start Docker services first (test profile)
docker compose --profile test up -d

# Build worker binary (required for integration tests)
make build-worker

# Run integration tests
make test-integration
# or
go test ./tests/integration/... -v

# Clean up
docker compose --profile test down
```

### Enhanced Integration Test Execution
The integration tests now include comprehensive end-to-end workflows:

```bash
# Run specific enhanced integration tests
go test ./tests/integration/worker_system_e2e_test.go -v
go test ./tests/integration/connectivity_e2e_test.go -v
go test ./tests/integration/signed_url_staging_e2e_test.go -v
go test ./tests/integration/robustness_e2e_test.go -v

# Run with extended timeout for E2E tests
go test ./tests/integration/... -v -timeout=30m
```

**Key Enhanced Test Categories**:
- **Worker System E2E**: Real gRPC communication, worker spawning, task execution
- **Connectivity Tests**: Docker service health verification, network connectivity
- **Signed URL Staging**: Complete data staging workflow with MinIO

### ✅ Enhanced Integration Tests

Integration tests have been significantly enhanced with:
- **Real gRPC Communication**: Tests now use actual gRPC server/client communication instead of mock validation
- **Complete E2E Workflows**: Full end-to-end scenarios from worker spawning to task completion
- **Docker Service Health Verification**: Tests verify actual Docker service connectivity and health
- **Worker Binary Integration**: Tests spawn and interact with real worker processes
- **Data Staging Validation**: Complete signed URL workflow testing with MinIO integration

### ⚠️ Integration Test Execution Requirements

Integration tests now require:
- **Docker Services**: PostgreSQL, MinIO, SLURM clusters, SFTP, NFS, SSH servers
- **Worker Binary**: Must be built before running integration tests
- **Network Connectivity**: Services must be accessible and healthy
- **Service Startup Time**: Allow 2-3 minutes for all services to become healthy

### Automated Execution

Use the provided script for automated test execution:

```bash
./scripts/test/run-integration-tests.sh
```

This script will:
1. Start all Docker services
2. Wait for services to become healthy
3. Build the worker binary
4. Run all integration tests
5. Clean up Docker services

### Manual Execution

If you need to run tests manually:

1. Start Docker services:
```bash
docker compose --profile test up -d
```

2. Wait for services (2-3 minutes):
```bash
sleep 180
```

3. Build worker binary:
```bash
make build-worker
```

4. Run tests:
```bash
go test -v -timeout 30m ./tests/integration/...
```

5. Cleanup:
```bash
docker compose --profile test down -v
```
- **Robustness Tests**: Worker failure scenarios, timeout handling, retry mechanisms

### Automated Test Execution Script
Use the provided script for automated integration test execution:

```bash
# Run the automated integration test script
./scripts/test/run-integration-tests.sh

# This script will:
# 1. Start Docker services
# 2. Wait for services to be healthy
# 3. Build worker binary
# 4. Run integration tests
# 5. Clean up services
```

## Current Test Status

### ✅ Compilation Status
- **Unit Tests**: All 27 test files compile and run successfully
- **Integration Tests**: All 16 test files compile successfully
- **Proto/gRPC Tests**: New test files added for comprehensive coverage

### ✅ Enhanced Integration Tests
Integration tests have been significantly enhanced with:
- **Real gRPC Communication**: Tests now use actual gRPC server/client communication instead of mock validation
- **Complete E2E Workflows**: Full end-to-end scenarios from worker spawning to task completion
- **Docker Service Health Verification**: Tests verify actual Docker service connectivity and health
- **Worker Binary Integration**: Tests spawn and interact with real worker processes
- **Data Staging Validation**: Complete signed URL workflow testing with MinIO integration
- **Real Worker Spawning**: Tests spawn actual worker processes on SLURM, Kubernetes, and Bare Metal
- **Complete Data Staging Workflow**: Input staging → execution → output staging with proper state transitions
- **Output Collection API**: Tests for listing and downloading experiment outputs organized by experiment ID
- **Cross-Storage Data Movement**: Tests data staging across different storage types (S3, SFTP, NFS)

### ⚠️ Integration Test Execution Requirements
Integration tests now require:
- **Docker Services**: PostgreSQL, MinIO, SLURM clusters, SFTP, NFS, SSH servers
- **Worker Binary**: Must be built before running integration tests
- **Network Connectivity**: Services must be accessible and healthy
- **Service Startup Time**: Allow 2-3 minutes for all services to become healthy

**Note**: Test compilation errors have been resolved. Current failures are infrastructure-related, not code issues.

## Infrastructure Requirements

### Required Services
Integration tests require the following Docker services (defined in `docker-compose.yml` with test profile):

#### Core Services
- **PostgreSQL**: Database for test data storage
  - Port: 5432
  - Database: `airavata_test`
  - User: `postgres` / Password: `password`

#### Storage Services
- **MinIO**: S3-compatible object storage
  - Port: 9000 (API), 9001 (Console)
  - Credentials: `testadmin` / `testpass123`
- **SFTP Server**: File transfer protocol testing
  - Port: 2222
  - User: `testuser` / Password: `testpass`
- **NFS Server**: Network file system testing
  - Port: 2049

#### Compute Services
- **SLURM Clusters**: 2 different clusters for workload management
  - Cluster 1: Port 6817
  - Cluster 2: Port 6819
- **Bare Metal**: Ubuntu SSH servers
  - Node 1: Port 2223
  - Node 2: Port 2225
  - User: `testuser` / Password: `testpass`
- **Kubernetes**: Kind cluster for container orchestration
  - Uses `kindest/node:v1.27.0` image

#### Network Services
- **SSH Server**: For secure shell access testing
  - Port: 2223
  - User: `testuser` / Password: `testpass`

### Service Startup
```bash
# Start all services
docker compose --profile test up -d

# Check service health
docker compose --profile test ps

# View logs
docker compose --profile test logs [service-name]

# Stop services
docker compose --profile test down
```

### Test Coverage Summary
- **106 integration test functions** across 16 files
- **27 unit test functions** across 27 files
- **Comprehensive coverage** of:
  - Compute resource adapters (SLURM, Bare Metal, Kubernetes)
  - Storage backends (S3, SFTP, NFS)
  - Worker system and gRPC communication
  - Data staging and transfer
  - Authentication and permissions
  - Multi-runtime workflows

## Test Architecture

### Hexagonal Testing Strategy

Tests are organized to match the hexagonal architecture:

- **Domain Tests**: Test pure business logic without external dependencies
- **Service Tests**: Test service implementations with mocked ports
- **Port Tests**: Test infrastructure interfaces with real implementations
- **Adapter Tests**: Test external system integrations

### Test Structure

```
tests/
├── unit/              # Fast, isolated tests (< 100ms each)
│   ├── types/         # Type and interface tests
│   ├── core/          # Core implementation tests
│   └── adapters/      # Adapter tests
├── integration/       # Tests with real dependencies
│   ├── api/           # API integration tests
│   ├── worker/        # Worker integration tests
│   └── e2e/           # End-to-end workflow tests
├── performance/       # Performance and load tests
└── testutil/          # Shared test utilities
    ├── fixtures.go    # Test data
    ├── database.go    # DB test helpers
    ├── helpers.go     # General helpers
    └── mocks.go       # Mock implementations
```

## Test Categories

### Unit Tests (Foundation)

**Purpose**: Test individual components in isolation
**Speed**: Fast (< 100ms per test)
**Dependencies**: None (use mocks)
**Coverage**: 80%+ of business logic

```bash
# Run specific unit test package
go test ./tests/unit/core -v

# Run specific test
go test ./tests/unit/core -v -run TestResourceValidation

# With coverage
go test ./tests/unit/core -v -cover
```

**Key Unit Test Areas**:
- Type validation and serialization
- Core service logic (cost calculation, scheduling algorithms)
- State machine transitions
- Authentication and authorization logic
- Data validation and transformation

#### Domain Tests
Test pure business logic in the `core/domain/` package:

```go
func TestTaskStatus_Transitions(t *testing.T) {
    // Test domain value objects and business rules
    assert.True(t, domain.TaskStatusQueued.CanTransitionTo(domain.TaskStatusAssigned))
    assert.False(t, domain.TaskStatusCompleted.CanTransitionTo(domain.TaskStatusQueued))
}
```

#### Service Tests
Test service implementations with mocked ports:

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

### Integration Tests (Middle Layer)

**Purpose**: Test component interactions with real services
**Speed**: Medium (1-10s per test)
**Dependencies**: Docker services (PostgreSQL, MinIO, SFTP)
**Coverage**: Critical workflows and data flows

```bash
# Run all integration tests
go test ./tests/integration/... -v

# Run specific integration test
go test ./tests/integration/api -v -run TestCreateExperiment

# Run with race detection
go test ./tests/integration/... -v -race
```

**Key Integration Test Areas**:
- API endpoints with real database
- Storage adapter operations (S3, SFTP, NFS)
- Worker lifecycle and task execution
- Scheduler daemon operations
- Multi-user scenarios and isolation
- Credential management and vault operations

#### PostgreSQL Schema Isolation

Each integration test gets its own PostgreSQL schema to ensure complete isolation:

```go
func TestIntegrationFeature(t *testing.T) {
    testDB := testutil.SetupFreshPostgresTestDB(t)
    defer testDB.Cleanup()
    
    // ... test code ...
}
```

The `SetupFreshPostgresTestDB` function:
- Creates a unique schema per test (e.g., `test_MyTest_1760484706714868000`)
- Sets `search_path` to that schema
- Runs migrations within that schema
- Cleanup drops the entire schema

### End-to-End Tests (Top Layer)

**Purpose**: Test complete user workflows
**Speed**: Slow (10s+ per test)
**Dependencies**: Full system stack
**Coverage**: Key user journeys

```bash
# Run E2E tests
go test ./tests/integration/e2e_workflow_test.go -v

# Run specific E2E scenario
go test ./tests/integration/e2e_workflow_test.go -v -run TestCompleteExperimentLifecycle
```

**Key E2E Test Areas**:
- Complete experiment lifecycle (create → submit → execute → results)
- Multi-resource workflows (SLURM + Kubernetes + S3)
- Data staging across different storage backends
- Failure recovery and error handling
- Performance under realistic load

### Enhanced Test Types

#### Real Worker Spawning Tests
Tests that spawn actual worker processes on different compute resources:

```bash
# Run worker spawning tests
go test ./tests/integration/worker_system_e2e_test.go -v

# Test specific compute resource
go test ./tests/integration/slurm_e2e_test.go -v -run TestSlurmCluster1_HelloWorld
```

**Features**:
- Real worker process execution on SLURM, Kubernetes, and Bare Metal
- Worker registration and heartbeat verification
- Task assignment and execution validation
- Worker metrics and status monitoring

#### Data Staging Workflow Tests
Tests the complete data staging workflow from input to output:

```bash
# Run data staging tests
go test ./tests/integration/data_staging_e2e_test.go -v

# Test specific staging scenario
go test ./tests/integration/data_staging_e2e_test.go -v -run TestDataStaging_InputStaging
```

**Features**:
- Input staging (central storage → compute node)
- Task execution with staged inputs
- Output staging (compute node → central storage)
- Cross-storage data movement (S3 → SLURM → NFS)
- Data integrity verification with checksums

#### Complete Workflow Tests
End-to-end tests covering the entire experiment lifecycle:

```bash
# Run complete workflow tests
go test ./tests/integration/complete_workflow_e2e_test.go -v

# Test specific workflow
go test ./tests/integration/complete_workflow_e2e_test.go -v -run TestCompleteWorkflow_FullDataStaging
```

**Features**:
- Complete experiment lifecycle with real workers
- Multi-task output collection and organization
- API endpoint testing for output listing and download
- Data lineage tracking and verification

## Docker Services for Integration Tests

The integration tests require several external services. Use the provided Docker Compose configuration:

```bash
# Start all services
docker compose --profile test up -d

# Check service status
docker compose --profile test ps

# View logs
docker compose --profile test logs

# Stop services
docker compose --profile test down
```

### Services Included

- **PostgreSQL**: Database for integration tests
- **MinIO**: S3-compatible storage for testing
- **Redis**: Caching and session storage
- **SFTP Server**: For SFTP storage adapter tests
- **SLURM Clusters**: For SLURM compute adapter tests
- **SSH Server**: For bare metal compute adapter tests

### Service Endpoints

| Service | Host | Port | Credentials | Purpose |
|---------|------|------|-------------|---------|
| MinIO | localhost | 9000 | minioadmin:minioadmin | S3 storage |
| MinIO Console | localhost | 9001 | minioadmin:minioadmin | Web UI |
| SFTP | localhost | 2222 | testuser:testpass | SFTP storage |
| NFS | localhost | 2049 | - | NFS storage |
| SLURM Cluster 1 | localhost | 6817 | slurm:slurm | Job scheduling |
| SLURM Cluster 2 | localhost | 6819 | slurm:slurm | Job scheduling |
| SSH | localhost | 2223 | testuser:testpass | Remote execution |
| PostgreSQL | localhost | 5432 | user:password | Database |
| Scheduler API | localhost | 8080 | - | REST API |

## Test Data Management

### Test Isolation

Each test should be isolated and clean up after itself:

```go
func TestExample(t *testing.T) {
    // Setup
    testDB := testutil.SetupTestDB(t)
    defer testDB.Cleanup()
    
    // Test logic
    // ...
}
```

### Entity IDs

**NEVER** use hardcoded IDs like `"test-user-1"`, `"test-worker-1"`, etc.

**ALWAYS** use dynamic IDs:

```go
// BAD - hardcoded ID causes UNIQUE constraint failures
worker := &types.Worker{
    ID: "test-worker-1",
    // ...
}

// GOOD - unique ID per test run
worker := &types.Worker{
    ID: uniqueID("test-worker"),  // or fmt.Sprintf("test-worker-%d", time.Now().UnixNano())
    // ...
}
```

Use the `uniqueID()` helper function from `test_helpers.go`:

```go
workerID := uniqueID("worker")
userID := uniqueID("user")
experimentID := uniqueID("experiment")
```

### Test Data Generation

Use the test utilities for consistent test data:

```go
// Generate unique test data
userID := testutil.GenerateTestUserID()
resourceID := testutil.GenerateTestResourceID()

// Create test fixtures
user := testutil.CreateTestUser(t, testDB, userID)
resource := testutil.CreateTestComputeResource(t, testDB, resourceID)
```

## Common Test Patterns

### Database Tests

```go
func TestDatabaseOperation(t *testing.T) {
    testDB := setupTestDB(t)
    defer cleanupTestDB(t, testDB)
    
    // Test database operations
    repo := core.NewUserRepository(testDB.DB)
    user, err := repo.Create(ctx, testUser)
    require.NoError(t, err)
    assert.NotEmpty(t, user.ID)
}
```

### API Tests

```go
func TestAPIEndpoint(t *testing.T) {
    testDB := testutil.SetupTestDB(t)
    defer testDB.Cleanup()
    
    // Setup API handlers
    handlers := api.NewAPIHandlers(testDB.DB)
    router := api.SetupRouter(handlers)
    
    // Make HTTP request
    req := httptest.NewRequest("POST", "/api/v1/experiments", body)
    w := httptest.NewRecorder()
    router.ServeHTTP(w, req)
    
    assert.Equal(t, http.StatusCreated, w.Code)
}
```

### Storage Adapter Tests

```go
func TestStorageAdapter(t *testing.T) {
    if !testutil.DockerServicesAvailable() {
        t.Skip("Docker services not available")
    }
    
    // Setup storage resource and credentials
    resource := testutil.CreateTestStorageResource(t, testDB)
    credential := testutil.CreateTestCredential(t, testDB, resource.ID)
    
    // Test adapter operations
    adapter := storage.NewStorageAdapter(resource, vault)
    err := adapter.Upload("/test/file.txt", data, userID)
    require.NoError(t, err)
}
```

### Adapter Tests
Test external system integrations:

```go
func TestSlurmAdapter_SpawnWorker(t *testing.T) {
    if !*integration {
        t.Skip("Integration tests disabled")
    }
    
    adapter := slurm.NewAdapter(slurmConfig)
    result, err := adapter.SpawnWorker(ctx, 1*time.Hour)
    assert.NoError(t, err)
}
```

## Performance Testing

### Load Testing

```bash
# Run performance tests
go test ./tests/performance/... -v

# Run with specific load
go test ./tests/performance/... -v -run TestHighThroughput
```

### Benchmarking

```bash
# Run benchmarks
go test ./tests/unit/core -bench=.

# Run specific benchmark
go test ./tests/unit/core -bench=BenchmarkCostCalculation
```

## Test Utilities

### Helper Functions

Located in `tests/testutil/`:

- `setupTestDB(t *testing.T) *core.Database` - Creates an isolated SQLite in-memory database
- `cleanupTestDB(t *testing.T, db *core.Database)` - Closes the database connection
- `uniqueID(prefix string) string` - Generates a unique ID with the given prefix
- `SetupFreshPostgresTestDB(t *testing.T)` - Creates isolated PostgreSQL schema
- `DockerServicesAvailable()` - Checks if Docker services are running

### Docker Compose Helper (`testutil/docker_compose_helper.go`)
- Service startup/shutdown
- Health check monitoring
- Connection information
- Test environment setup

### Service Checks (`testutil/service_checks.go`)
- Docker availability
- Kubernetes cluster access
- Service port availability
- Graceful test skipping

### Adapter Fixtures (`testutil/adapter_fixtures.go`)
- Test file generation
- Script creation
- Resource configuration
- Data verification

## Troubleshooting

### Common Issues

**1. Docker Services Not Available**
```bash
# Check if services are running
docker compose --profile test ps

# Restart services
docker compose --profile test down
docker compose --profile test up -d
```

**2. Database Connection Issues**
```bash
# Check PostgreSQL logs
docker compose --profile test logs postgres

# Verify database is accessible
docker exec -it airavata-scheduler-postgres-1 psql -U airavata -d airavata_scheduler
```

**3. Test Timeouts**
```bash
# Increase timeout for slow tests
go test ./tests/integration/... -v -timeout=5m
```

**4. Race Conditions**
```bash
# Run with race detection
go test ./tests/integration/... -v -race
```

**5. UNIQUE constraint failed**

**Cause**: Tests are sharing database state or using hardcoded IDs

**Solution**: Ensure each test creates its own database with `setupTestDB(t)` and use `uniqueID()` for entity IDs

**6. Incorrect counts (finding more items than created)**

**Cause**: Test is seeing data from previous tests

**Solution**: Ensure test is creating its own isolated database, not reusing a shared one

**7. Tests pass individually but fail together**

**Cause**: Tests are affecting each other's state

**Solution**: Verify each test function and subtest creates its own database with `setupTestDB(t)`

**8. Test hangs indefinitely**

**Cause**: Waiting for unavailable external service without timeout

**Solution**: Use `testutil.SetupFreshPostgresTestDB(t)` which skips if PostgreSQL unavailable, or add explicit timeouts

### Debug Mode

Enable debug logging for tests:

```bash
# Set debug environment variable
export TEST_DEBUG=1
go test ./tests/integration/... -v
```

### Test Coverage

Generate and view test coverage:

```bash
# Generate coverage report
go test ./... -coverprofile=coverage.out

# View coverage in browser
go tool cover -html=coverage.out

# Coverage by package
go test ./... -coverprofile=coverage.out
go tool cover -func=coverage.out
```

## Continuous Integration

### GitHub Actions

The project includes GitHub Actions workflows for automated testing:

- **Unit Tests**: Run on every push
- **Integration Tests**: Run on pull requests
- **E2E Tests**: Run on main branch

### Local CI Simulation

```bash
# Run full CI pipeline locally
make ci

# This runs:
# - go fmt
# - go vet
# - go test (unit)
# - go test (integration)
# - go test (e2e)
```

## Best Practices

### Writing Tests

1. **Use descriptive test names**: `TestComputeResourceValidation_WithInvalidCredentials`
2. **Test one thing per test**: Each test should verify a single behavior
3. **Use table-driven tests** for multiple scenarios
4. **Clean up resources**: Always use `defer` for cleanup
5. **Use appropriate assertions**: `require` for setup, `assert` for verification

### Test Organization

1. **Group related tests**: Use subtests with `t.Run()`
2. **Use test utilities**: Don't duplicate setup code
3. **Mock external dependencies**: Keep unit tests fast and isolated
4. **Test error conditions**: Verify proper error handling

### Performance Considerations

1. **Parallel tests**: Use `t.Parallel()` for independent tests
2. **Skip slow tests**: Use `t.Skip()` when services unavailable
3. **Reuse resources**: Set up expensive resources once per test suite
4. **Clean up promptly**: Don't leave resources running

## Environment Variables

### Test Configuration

```bash
# Database
TEST_DATABASE_URL="postgres://airavata:test123@localhost:5432/airavata_scheduler_test"

# Services
TEST_MINIO_ENDPOINT="localhost:9000"
TEST_SFTP_HOST="localhost"
TEST_SFTP_PORT="2222"

# Debug
TEST_DEBUG=1
TEST_VERBOSE=1
```

### Production vs Test

Tests use separate databases and services to avoid conflicts:

- **Test Database**: `airavata_scheduler_test`
- **Test MinIO**: Different bucket names
- **Test SFTP**: Isolated test directory

## Quality Gates

### Coverage Requirements
- **Unit Tests**: 80%+ code coverage
- **Integration Tests**: 100% of critical workflows
- **E2E Tests**: All major user journeys

### Performance Benchmarks
- **Unit Tests**: < 100ms per test
- **Integration Tests**: < 10s per test
- **E2E Tests**: < 60s per test
- **Full Test Suite**: < 10 minutes

### Reliability Standards
- **Flaky Tests**: Zero tolerance
- **Race Conditions**: All tests pass with `-race` flag
- **Resource Leaks**: No memory or connection leaks
- **Cleanup**: All resources properly cleaned up

## When to Write Each Test Type

### Write Unit Tests When:
- Testing pure business logic
- Validating data transformations
- Testing error handling
- Verifying algorithm correctness

### Write Integration Tests When:
- Testing database operations
- Validating external service interactions
- Testing multi-component workflows
- Verifying configuration and setup

### Write E2E Tests When:
- Testing complete user workflows
- Validating system behavior under load
- Testing failure recovery scenarios
- Verifying production-like scenarios

## Getting Help

### Common Commands Reference

```bash
# Quick test run
make test

# Full integration test
make test-integration

# Specific test with verbose output
go test ./tests/unit/core -v -run TestSpecific

# Test with coverage
make test-coverage

# Clean up everything
make clean
docker compose --profile test down -v
```

### Debugging Tips

1. **Use `-v` flag**: Get detailed test output
2. **Use `-run` flag**: Run specific tests
3. **Check logs**: Look at Docker service logs
4. **Verify setup**: Ensure all services are running
5. **Clean state**: Start with fresh Docker containers

## Success Metrics

✅ All tests pass when run together: `go test ./tests/unit/...`

✅ Zero "UNIQUE constraint failed" errors

✅ Zero incorrect count assertions (e.g., expecting 3 but finding 23)

✅ Tests can run in any order

✅ Tests can run multiple times with same results

✅ No test hangs waiting for services

## Test Results

**Last verified**: October 16, 2025

### Unit Tests
- **Total**: 556 tests
- **Passing**: 546 (98.2%)
- **Failing**: 10 (1.8%)
- **UNIQUE Constraint Errors**: 0 ✅
- **State Pollution Issues**: 0 ✅

### Test Isolation Status
✅ All tests use isolated databases  
✅ Zero UNIQUE constraint failures  
✅ Zero incorrect count assertions  
✅ Tests can run in any order  
✅ Tests can run multiple times with same results

### Remaining Failures

The 10 remaining test failures are logic/implementation issues, not isolation problems:
- `TestAuthorizationService_ShareCredential` (2 subtests) - Permission message wording
- `TestAuthorizationService_RevokeCredentialAccess` - Assertion logic
- `TestValidation_EmptyAndNullFields` - Validation logic
- `TestValidation_JSONMalformed` - Validation logic
- Other minor assertion failures

These do NOT affect test isolation and can be fixed independently.

## Verification

To verify test isolation is working:

```bash
# Should show 0 UNIQUE constraint errors
cd /Users/yasith/code/artisan/airavata-scheduler
go test ./tests/unit/... 2>&1 | grep "UNIQUE constraint" || echo "✅ No UNIQUE constraint errors"

# Should show high pass rate (98%+)
go test -json ./tests/unit/... 2>&1 | jq -r 'select(.Action=="pass" or .Action=="fail") | "\(.Action)"' | sort | uniq -c
```

For more detailed information, see the [Architecture Guide](../docs/architecture.md) and [Development Guide](../docs/development.md).