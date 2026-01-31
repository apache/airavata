# Airavata API Module

The Airavata API module provides HTTP endpoints that serve the same core API functionalities as the Thrift Server, running as part of the unified HTTP server.

## API Type

- **Server**: HTTP Server (unified)
- **Port**: 8080 (default)
- **Configuration**: `airavata.services.http.server.port`

## Overview

The Airavata API provides HTTP endpoints that serve the same core API functionalities as the Thrift Server. Both APIs access the same internal services and provide equivalent functionality through different protocols (HTTP vs Thrift). It runs as part of the unified HTTP server on port 8080.

This is part of one of four external API layers in Airavata:
- **Thrift Server** (port 8930) - Thrift Endpoints for Airavata API functions
- **HTTP Server** (port 8080):
  - Airavata API - HTTP Endpoints for Airavata API functions (this module)
  - File API - HTTP Endpoints for file upload/download
  - Agent API - HTTP Endpoints for interactive job contexts
  - Research API - HTTP Endpoints for use by research hub
- **gRPC Server** (port 9090) - For airavata binaries to open persistent channels with airavata APIs
- **Dapr gRPC** (port 50001) - Sidecar for pub/sub, state, and workflow execution

## Airavata API Endpoints

The Airavata API provides HTTP endpoints that serve the same core functionalities as the Thrift Server:

| Endpoint Category | Controller | Description |
|-------------------|------------|-------------|
| Experiments | `ExperimentController` | Experiment lifecycle management |
| Processes | `ProcessController` | Process execution and monitoring |
| Jobs | `JobController` | Job status and management |
| Applications | `ApplicationInterfaceController` | Application definitions |
| Deployments | `ApplicationDeploymentController` | Application deployment configurations |
| Compute Resources | `ComputeResourceController` | Compute resource management |
| Storage Resources | `StorageResourceController` | Storage resource management |
| Projects | `ProjectController` | Project management |
| Gateways | `GatewayController` | Gateway configuration |
| Groups | `GroupResourceProfileController` | Group resource profiles |
| User Profiles | `UserResourceProfileController` | User resource profiles |
| Workflows | `WorkflowController` | Workflow definitions |
| Data Products | `DataProductController` | Data product management |
| Proxy | `ProxyController` | Proxy utilities |
| Credentials | `CredentialController` | Credential summaries and SSH/password credential CRUD |
| Resource Access | `ResourceAccessController` | Access grants (credential + resource + login username) |
| Cluster Info | `ClusterInfoController` | SLURM partitions and grant-tied accounts per credential+resource |
| Connectivity Test | `ConnectivityTestController` | SSH validation (credential + loginUsername required) |

All endpoints are prefixed with `/api/v1/` and follow RESTful conventions.

### Cluster info and grant-tied accounts on the portal

When a credential is used to discover what is allowed on a resource, the system runs the bundled bash script (slurminfo.sh) via SSH and caches the result. The **cluster-info API** exposes this so the portal can show grant-tied partitions and accounts to users:

- **POST** `/api/v1/cluster-info/fetch` — Body: `credentialToken`, `computeResourceId`, `hostname`, `port` (optional, default 22), `loginUsername` (required; from resource access/grant), `gatewayId` (optional, from auth). Fetches and caches partitions and accounts for this credential on this compute resource.
- **GET** `/api/v1/cluster-info/{credentialToken}/{computeResourceId}` — Returns cached cluster info: `partitions` (list of partition objects with name, nodes, accounts, etc.) and `accounts` (list of Slurm account names). Use this to show "your accounts per resource" after fetch.
- **DELETE** `/api/v1/cluster-info/{credentialToken}/{computeResourceId}` — Invalidates the cache.

Portal flow: For each of the user's credential–resource pairs (e.g. from resource access grants), call fetch (once) then GET to display partitions and accounts. Users can then bind one account per resource to a project (see Project resource accounts).

### Project resource accounts (one account per resource per project)

A project can have **resource-account bindings**: for each compute resource, one Slurm account to use when running experiments in that project. This lets users work on a project with the correct accounts regardless of which resource runs.

- **GET** `/api/v1/projects/{projectId}/resource-accounts` — List bindings for the project (each: computeResourceId, credentialToken, accountName, gatewayId).
- **POST** `/api/v1/projects/{projectId}/resource-accounts` — Add or update a binding. Body: `{ "computeResourceId", "credentialToken", "accountName", "gatewayId" }` (gatewayId optional; defaults to project's gateway). The `accountName` must be one of the accounts returned by cluster-info for this credential and resource (fetch cluster info first).
- **DELETE** `/api/v1/projects/{projectId}/resource-accounts/{computeResourceId}` — Remove the binding for that resource.

Portal flow for editing a project's allocation: (1) List user's credential–resource pairs (e.g. from resource access). (2) For each, fetch cluster info and show accounts. (3) User selects one account per resource and POSTs to project resource-accounts. At job submit time, the system uses the project's account for the chosen compute resource when present. Credentials are stored without a login username; login username is set per resource in resource-access grants and must be supplied when testing connectivity.

## Internal Services Used

The Airavata API accesses the same internal services as the Thrift Server:

- **Orchestrator** - Workflow orchestration
- **Registry** - Application and metadata registry
- **Profile Service** - User, tenant, and resource profile management
- **Sharing Registry** - Permissions and sharing
- **Credential Store** - Secure credential storage

These services are internal components. Both the Thrift Server (port 8930) and the Airavata API (HTTP, port 8080) access these same internal services and provide equivalent functionality through different protocols.

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `airavata.services.rest.enabled` | `false` | Enable/disable Airavata API |
| `airavata.services.rest.server.port` | - | Maps to Spring Boot `server.port` (uses unified HTTP server port 8080) |
| `airavata.services.http.server.port` | `8080` | Unified HTTP server port |

When `airavata.services.rest.enabled=true`, the Airavata API runs on the unified HTTP server port (8080 by default).

## OpenAPI Documentation

When enabled, the Airavata API provides OpenAPI/Swagger documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Implementation

The main configuration is in:
- `src/main/java/org/apache/airavata/restapi/config/RestApiConfiguration.java`
- `src/main/java/org/apache/airavata/restapi/config/OpenApiConfig.java`

Controllers are in:
- `src/main/java/org/apache/airavata/restapi/controller/`

## Related Documentation

- Main project README: [../../README.md](../../README.md)
- Thrift Server: [../thrift-api/README.md](../thrift-api/README.md)
- Deployment scripts: [../../dev-tools/deployment-scripts/README.md](../../dev-tools/deployment-scripts/README.md)
