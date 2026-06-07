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

Health check: `http://localhost:9090/actuator/health` (or `https://api.airavata.localhost/actuator/health`)
API docs: `http://localhost:9090/docs` (or `https://api.airavata.localhost/docs`)

### Verifying changes

Compile-green is not enough — the running Tilt-managed server must actually stay up. After each batch of changes:

```bash
tilt logs airavata-server 2>&1 | tail -40        # scan for stack traces
curl http://localhost:9090/actuator/health
```

Do not run `mvn clean` during an active session: it deletes `airavata-server/target/*.jar` while the JVM is running, and lazy classloaders (e.g. the MariaDB JDBC ServiceLoader) then fail at runtime with `NoSuchFileException`, cascading into HikariCP connection timeouts. Prefer incremental `mvn compile`. If a clean was unavoidable, rebuild and force a restart: `mvn package -DskipTests -pl airavata-server -am` then `tilt trigger airavata-server` (Tilt may watch sources but not the jar artifact).

### Infrastructure (started by `tilt up`)

`tilt up` starts a **single** host container, `airavata-dind` (Docker-in-Docker), running
its own Docker daemon + registry. All services below run **inside** it — the host's
`docker ps` shows only `airavata-dind`. `tilt down` removes it and everything inside
(state is ephemeral: every `up` is a fresh DB/Kafka/Keycloak). Each inner service is its
own Tilt resource, driven via `docker exec airavata-dind docker compose …` (gated behind a
`dind-ready` step that waits for the inner daemon). The server image is built inside DinD
and pulled from the in-DinD registry (`localhost:5000`). The DinD layer is `compose.dind.yml`;
the inner stack is `compose.yml`.

One-time host setup for HTTPS: `brew install mkcert && mkcert -install` (trusts the local
CA so browsers accept `https://*.airavata.localhost`). The cert is generated under
`conf/traefik/certs/` (gitignored) and imported into the server's JVM truststore.

Reach services by hostname (`*.airavata.localhost` → loopback per RFC 6761; HTTP **and
HTTPS** via the Traefik ingress, no `/etc/hosts` needed):

| Hostname | Service | Creds |
|----------|---------|-------|
| `api.airavata.localhost` | Airavata server (gRPC + REST) | — |
| `auth.airavata.localhost` | Keycloak | `admin` / `admin` |
| `rabbitmq.airavata.localhost` | RabbitMQ management UI | `airavata` / `airavata` |
| `adminer.airavata.localhost` | Adminer (`--profile tools`) | — |

Also published directly on the host: `9090` (server gRPC/REST), `13306` (MariaDB
`airavata`/`123456`), `5000` (registry), `80`/`443` (Traefik). Internally the server reaches
infra by compose **service name** (`db:3306`, `rabbitmq:5672`, `kafka:9092`,
`zookeeper:2181`, `keycloak:18080`, `sftp:22`; SFTP creds `airavata`/`pass`).

Gotchas:
- Connection settings are injected as `-D` system properties via `JAVA_TOOL_OPTIONS` in the
  `airavata-server` service. Airavata's `ApplicationSettings` resolves *dotted* keys
  (`system-property > env-var-with-exact-dotted-key > file`), so `SPRING_DATASOURCE_URL`-style
  env vars do **not** override `kafka.broker.url` / `rabbitmq.broker.url` / etc.
- The Keycloak issuer is `https://auth.airavata.localhost/realms/default` for both browser and
  in-network server (TLS terminates at Traefik, which forwards `X-Forwarded-Proto`).
- `docker` must be on `PATH` for Tilt's shell-outs (colima installs the CLI keg-only — run
  `brew link docker` if `which docker` is empty).
- Add a hostname: give the service a `traefik.http.routers.<name>.rule=Host(...)` label
  (+ `entrypoints=web,websecure`, `tls=true`) and a network alias on the `traefik` service.

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

## Schema & Code Consolidation

When simplifying or consolidating schemas or code, only merge things that naturally belong together — same lifecycle, same owning service, same conceptual identity. Do not consolidate by surface-level shape similarity (shared column names or a common family suffix): e.g. don't fold a workflow-engine `HANDLER_STATUS` into execution-core `EXEC_STATUS` just because both carry STATE + TIMESTAMP — they have different lifecycles, services, and invariants. Mechanical consolidation yields mostly-null columns, lost type safety, and blurred subsystem boundaries. Legitimate consolidations: variant-of-same-concept tables (different shapes of "a way to submit a job"), 1:1 splits that were premature normalization, and multi-value child tables with no independent identity. Anything beyond that needs a real reason.
