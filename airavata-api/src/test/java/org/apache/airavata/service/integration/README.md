# State Machine Integration Tests

This directory contains integration tests for state machine transitions in Airavata.

## Prerequisites

Before running these tests, you must start the required background services:

1. **MariaDB** (port 13306) - Database for state persistence
2. **Zookeeper** (port 2181) - Required for Helix and Kafka
3. **Kafka** (port 9092) - Messaging for state change events
4. **RabbitMQ** (port 5672) - Alternative messaging backend

## Starting Services

### Using the startup script (recommended):

```bash
cd .devcontainer
./start-integration-services.sh
```

The script will automatically detect and use:
- `nerdctl compose` (if available)
- `docker-compose` (if available)
- `docker compose` (if available)

### Manual startup:

```bash
cd .devcontainer
# Using nerdctl:
nerdctl compose -f docker-compose.yml up -d db zookeeper kafka rabbitmq

# Or using docker compose:
docker compose -f docker-compose.yml up -d db zookeeper kafka rabbitmq
```

### Verify services are running:

```bash
# Check service status
docker compose -f .devcontainer/docker-compose.yml ps

# Or with nerdctl:
nerdctl compose -f .devcontainer/docker-compose.yml ps
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

Tests use the `airavata-integration.properties` file which configures:
- Database connections to `localhost:13306` (MariaDB)
- Kafka broker at `localhost:9092`
- Zookeeper at `localhost:2181`
- RabbitMQ at `localhost:5672`

The `localhost` hostname resolves to `192.168.100.1` per the docker-compose.yml network configuration.

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

### Kafka/Zookeeper issues

Check if services are listening on expected ports:
```bash
netstat -an | grep 9092  # Kafka
netstat -an | grep 2181  # Zookeeper
```

