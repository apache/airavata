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

Health check: `https://api.airavata.host/actuator/health`
API docs: `https://api.airavata.host/docs`

### Verifying changes

Compile-green is not enough — the running Tilt-managed server must actually stay up. After each batch of changes:

```bash
tilt logs airavata-server 2>&1 | tail -40        # scan for stack traces
curl https://api.airavata.host/actuator/health
```

Do not run `mvn clean` during an active session: it deletes `airavata-server/target/*.jar` while the JVM is running, and lazy classloaders (e.g. the MariaDB JDBC ServiceLoader) then fail at runtime with `NoSuchFileException`, cascading into HikariCP connection timeouts. Prefer incremental `mvn compile`. If a clean was unavoidable, rebuild and force a restart: `mvn package -DskipTests -pl airavata-server -am` then `tilt trigger airavata-server` (Tilt may watch sources but not the jar artifact).

### Infrastructure (started by `tilt up`)

Services run directly in the shared colima VM (`airavata` profile) on the `airavata-devstack`
docker network. A single shared Traefik ingress (managed by `devstack/`) binds `127.0.0.1:80/443`,
routes by Host header with a mkcert-trusted wildcard cert for `*.airavata.host`, and forwards to
containers by label. State is ephemeral: `tilt down` stops containers but volumes persist until
you run `./devstack/devstack reset`.

**One-time host setup (run once per machine):**

```bash
./devstack/devstack setup     # installs colima/dnsmasq/mkcert, starts VM, starts Traefik, sets up DNS
```

After that, `tilt up` is all you need.

Reach services by hostname (`*.airavata.host` → `127.0.0.1` via dnsmasq; HTTPS via shared Traefik;
no `/etc/hosts` needed):

| Hostname | Service | Creds |
|----------|---------|-------|
| `api.airavata.host` | Airavata server (gRPC + REST) | — |
| `auth.airavata.host` | Keycloak | `admin` / `admin` |
| `rabbitmq.airavata.host` | RabbitMQ management UI | `airavata` / `airavata` |
| `adminer.airavata.host` | Adminer (`--profile tools`) | — |
| `gateway.airavata.host` | Django portal (separate `tilt up --port 10351` in `airavata-portals`) | — |

### Web portals (separate `tilt up`)

The web portals run from the sibling `airavata-portals` repo. Both repos share the same
colima VM and `airavata-devstack` network, so the portal container reaches the server at
`airavata-server:9090` (in-network plaintext gRPC).

```bash
tilt up                                          # 1. this repo — infra + server
cd ../airavata-portals && tilt up --port 10351   # 2. portals (distinct Tilt UI port)
```

MariaDB is published on `127.0.0.1:3306` (replacing the old `13306`) and reachable
inside the VM as `db.airavata.host:3306`. Internally the server reaches infra by
service name (`rabbitmq:5672`, `kafka:9092`, `zookeeper:2181`, `keycloak:18080`,
`sftp:22`; SFTP creds `airavata`/`pass`).

**Restart matrix:**

| Scenario | Action |
|----------|--------|
| Machine reboot | `./devstack/devstack ensure` (idempotent; restarts VM + ingress if stopped), then `tilt up` |
| Colima stopped manually | `colima start -p airavata`, then `tilt up` |
| Wipe all data | `./devstack/devstack reset` (global — destroys the shared VM) |

Gotchas:
- Connection settings are injected as `-D` system properties via `JAVA_TOOL_OPTIONS` in the
  `airavata-server` service. Airavata's `ApplicationSettings` resolves *dotted* keys
  (`system-property > env-var-with-exact-dotted-key > file`), so `SPRING_DATASOURCE_URL`-style
  env vars do **not** override `kafka.broker.url` / `rabbitmq.broker.url` / etc.
- The Keycloak issuer is `https://auth.airavata.host/realms/default` for both browser and
  in-network server (TLS terminates at Traefik, which forwards `X-Forwarded-Proto`).
- `docker` must be on `PATH` for Tilt's shell-outs (colima installs the CLI keg-only — run
  `brew link docker` if `which docker` is empty).
- The mkcert root CA is mounted at `/certs/rootCA.pem` in the server container and imported
  into the JVM truststore at startup (alias `mkcert-airavata`, idempotent).

## Module Structure

### `airavata-api` (core services)

Contains all service modules, each with `src/main/{java,proto,resources}`:

| Module | Purpose |
|--------|---------|
| `iam-service` | User auth via Keycloak, gateways, user profiles; permissions, groups, resource access control (sharing) |
| `credential-service` | SSH key and password vault |
| `compute-service` | HPC resource catalog, resource profiles, scheduling |
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

## Logging

Use SLF4J parameterized logging: pass values as `{}` placeholders rather than string concatenation, so the message template stays readable and arguments are only stringified when the level is enabled.

```java
log.info("Could not find {} credentials for token {} in gateway {}", label, tokenId, gatewayId);  // do this
log.info("Could not find " + label + " credentials for token " + tokenId + " ...");               // not this
```

A trailing `Throwable` is logged with its stack trace and takes no placeholder: `log.error("Failed to fetch {}", tokenId, e);`. Exception messages are plain strings (no placeholder mechanism), so build those normally.

## Test Patterns

- JUnit 5 with `@Tag("integration")` and `@Tag("runtime")` categories
- Mockito for unit tests (`@ExtendWith(MockitoExtension.class)`)
- TestContainers (MariaDB) for integration tests via `AbstractIntegrationTest` base class
- Integration tests share a singleton MariaDB container across the test suite

## Schema & Code Consolidation

When simplifying or consolidating schemas or code, only merge things that naturally belong together — same lifecycle, same owning service, same conceptual identity. Do not consolidate by surface-level shape similarity (shared column names or a common family suffix): e.g. don't fold a workflow-engine `HANDLER_STATUS` into execution-core `EXEC_STATUS` just because both carry STATE + TIMESTAMP — they have different lifecycles, services, and invariants. Mechanical consolidation yields mostly-null columns, lost type safety, and blurred subsystem boundaries. Legitimate consolidations: variant-of-same-concept tables (different shapes of "a way to submit a job"), 1:1 splits that were premature normalization, and multi-value child tables with no independent identity. Anything beyond that needs a real reason.
