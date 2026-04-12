# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Apache Airavata — a unified middleware for distributed computing resource management. Single Spring Boot JVM serving gRPC + REST via Armeria on port 9090. Protobuf defines the API contracts; HTTP/JSON transcoding provides REST automatically from gRPC definitions.

## Build & Dev

```bash
tilt up                                           # Start everything (infra + server). ALWAYS use this.
mvn install -DskipTests -T4                       # Build only (no server start)
mvn test -T4                                      # Unit tests (parallel)
mvn test -pl airavata-api -Dgroups=runtime        # Integration tests (needs Docker via tilt)
mvn test -pl airavata-api/research-service        # Single module tests
```

Health check: `http://localhost:9090/internal/actuator/health`
API docs: `http://localhost:9090/docs`

### Infrastructure (started by `tilt up`)

| Service | Port | Notes |
|---------|------|-------|
| Airavata Server | 9090 | gRPC + REST (Armeria) |
| MariaDB | 13306 | `airavata` / `123456` |
| Keycloak | 18080 | `admin` / `admin` |
| RabbitMQ | 5672, 15672 | `airavata` / `airavata` (admin UI on 15672) |
| Kafka | 9092 | |
| ZooKeeper | 2181 | |
| SFTP | 2222 | `airavata` / `pass` |

## Module Structure

### `airavata-api` (core services)

Contains all service modules, each with `src/main/{java,proto,resources}`:

| Module | Purpose |
|--------|---------|
| `iam-service` | User auth via Keycloak, gateways, user profiles |
| `credential-service` | SSH key and password vault |
| `compute-service` | HPC resource catalog, resource profiles, scheduling |
| `sharing-service` | Permissions, groups, resource access control |
| `research-service` | Projects, experiments, app catalog, data products |
| `storage-service` | Storage registry, file operations |
| `orchestration-service` | Workflow orchestration, job monitoring (Helix state machine) |
| `agent-service` | Remote agent communication (bidirectional streaming) |

Also contains `src/main/` at the api level for shared utilities: `EventPublisher`, `GrpcStatusMapper`, `GrpcAuthInterceptor`, `RequestContext`.

### `airavata-server`

Unified Spring Boot launcher. Produces the fat JAR. Entry point: `org.apache.airavata.server.AiravataServerMain`. Configures Armeria with gRPC services, HTTP/JSON transcoding, CORS, and DocService.

### `airavata-agent`

Go-based remote agent binary. Handles job execution, Jupyter sessions, tunnels on compute nodes. Built separately from the Java project.

### `airavata-python-sdk`

Python gRPC client. `airavata/` has generated stubs, `airavata_sdk/clients/` has integration wrappers, `airavata_experiments/` has experiment workflow APIs.

### `airavata-jupyterhub`

JupyterHub authenticator and spawner plugins.

## Architecture Patterns

### Request Flow

```
Proto definition (google.api.http annotations)
  → gRPC stub (generated)
  → gRPC service impl (@Component, thin adapter)
  → Service bean (@Service, business logic)
  → Repository (@Repository, Spring Data JPA)
  → MariaDB
```

Example: `ProjectService` proto → `ProjectServiceGrpcImpl` (adapter) → `ProjectService` (logic) → `ProjectRegistry` (data).

**All business logic lives in `@Service` beans.** gRPC impls are thin adapters that delegate and map exceptions via `GrpcStatusMapper`. The Python SDK and Django portal have no business logic.

### Key Frameworks

- **Spring Boot 3.5** — DI, JPA, Actuator
- **Armeria 1.32** — gRPC + REST server, HTTP/JSON transcoding, DocService
- **gRPC 1.73 / Protobuf 4.29** — API contracts
- **MapStruct 1.6** — Entity ↔ Protobuf mapping
- **Flyway** — Schema migration (`V1__Baseline_schema.sql`)
- **Helix 1.4** — Workflow orchestration state machine
- **Keycloak 26** — IAM (JWT validation via `GrpcAuthInterceptor`)

### Background Services

Launched as `IServer` workers in the same JVM:
- `DBEventManagerRunner` — RabbitMQ listener for task events
- `HelixController` / `GlobalParticipant` — Workflow state machine (env setup, staging, submission, monitoring)
- `RealtimeMonitor` — Kafka listener for job completions
- `ProcessReschedulingService` — Retries failed processes

### Messaging

- **RabbitMQ** — Experiment/process state changes (`experiment_exchange`, `process_exchange`, `status_exchange`)
- **Kafka** — Real-time job monitoring (`monitoring-data` topic)

## Test Patterns

- JUnit 5 with `@Tag("integration")` and `@Tag("runtime")` categories
- Mockito for unit tests (`@ExtendWith(MockitoExtension.class)`)
- TestContainers (MariaDB) for integration tests via `AbstractIntegrationTest` base class
- Integration tests share a singleton MariaDB container across the test suite
