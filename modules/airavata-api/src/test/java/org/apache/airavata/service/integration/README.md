# State Machine Integration Tests

This directory contains integration tests for state machine transitions in Airavata.

## Prerequisites

Before running these tests, you must start the required background services:

1. **MariaDB** (port 13306) - Database for state persistence
2. **Redis** (port 6379) - Backend for Dapr Pub/Sub and State Store

## Starting Services

**Important**: Services must be started before running tests. Tests will automatically detect and use existing services from `.devcontainer/docker-compose.yml` - no extra configuration needed.

### Using the startup script (recommended):

```bash
cd .devcontainer
./start-integration-services.sh
```

The script will automatically:
- Detect and use `nerdctl compose`, `docker compose`, or `docker-compose` (whichever is available)
- Start services: `db`, `redis`
- Verify services are running
- Perform basic health checks

### Manual startup:

```bash
cd .devcontainer
# Using nerdctl:
nerdctl compose -f docker-compose.yml up -d db redis

# Or using docker compose:
docker compose -f docker-compose.yml up -d db redis

# Or using docker-compose:
docker-compose -f docker-compose.yml up -d db redis
```

### Verify services are running:

```bash
# Check service status
cd .devcontainer
docker compose -f docker-compose.yml ps

# Or with nerdctl:
nerdctl compose -f docker-compose.yml ps
```

## Running Tests

### Run all state machine integration tests:

```bash
mvn test -Dtest='*StateMachineIntegrationTest' -pl airavata-api
```

### Run specific test class:

```bash
mvn test -Dtest=JobSubmissionStateMachineIntegrationTest -pl airavata-api
mvn test -Dtest=ProcessExecutionStateMachineIntegrationTest -pl airavata-api
mvn test -Dtest=DataMovementStateMachineIntegrationTest -pl airavata-api
mvn test -Dtest=StateTransitionValidationIntegrationTest -pl airavata-api
```

### Run specific test method:

```bash
mvn test -Dtest='JobSubmissionStateMachineIntegrationTest#testJobSubmission_CompleteStateTransitionFlow' -pl airavata-api
```

## Test Configuration

Tests automatically detect and use existing services from `.devcontainer/docker-compose.yml`:
- **Auto-detection**: Tests check if MariaDB is accessible at `localhost:13306`
- **If services are running**: Tests use existing containers (no Testcontainers needed)
- **If services are not running**: Tests fall back to creating Testcontainers

**Service endpoints** (from docker-compose.yml):
- Database: `localhost:13306` (MariaDB, user: `airavata`, password: `123456`)
- Redis: `localhost:6379` (for Dapr Pub/Sub and State Store)

**Note**: The `localhost` hostname resolves to `192.168.100.1` per the docker-compose.yml network configuration when running inside the devcontainer.

**Explicit override** (if needed):
- Set system property: `-Dtestcontainers.use.existing=true` to force using existing containers
- Set environment variable: `TESTCONTAINERS_USE_EXISTING=true` to force using existing containers
- Set system property: `-Dtestcontainers.use.existing=false` to force using Testcontainers

## Test Files

- **JobSubmissionStateMachineIntegrationTest**: Tests job submission state transitions (SUBMITTED → QUEUED → ACTIVE → COMPLETE)
- **ProcessExecutionStateMachineIntegrationTest**: Tests full process lifecycle state transitions
- **DataMovementStateMachineIntegrationTest**: Tests input/output data staging state transitions
- **StateTransitionValidationIntegrationTest**: Tests state machine validation rules

## Stopping Services

```bash
cd .devcontainer
docker compose -f docker-compose.yml down

# Or with nerdctl:
nerdctl compose -f docker-compose.yml down
```

## Troubleshooting

### Connection refused errors

If you see "Connection refused" errors, ensure:
1. Services are started and running
2. Services are accessible from the test environment
3. The `localhost` hostname resolves correctly (check `/etc/hosts` or docker network)

### Database connection issues

Verify MariaDB is accessible:
```bash
mysql -h localhost -P 13306 -u airavata -p123456
```

### Redis issues

Check if Redis is listening on expected port:
```bash
netstat -an | grep 6379  # Redis
```

