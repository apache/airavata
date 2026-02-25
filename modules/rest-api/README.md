# Airavata API Module

The Airavata API module provides HTTP endpoints for all core API functionalities, running as part of the unified HTTP server.

## API Type

- **Server**: HTTP Server (unified)
- **Port**: 8090 (default)
- **Configuration**: `server.port` / `airavata.services.http.server.port`

## Overview

The Airavata API provides HTTP endpoints for all core API functionalities. It runs as part of the unified HTTP server on port 8090.

See [Architecture](../../README.md#architecture) for the full API layer overview.

## Airavata API Endpoints

The Airavata API provides HTTP endpoints for all core functionalities:

| Endpoint Category | Controller | Base Path | Description |
|-------------------|------------|-----------|-------------|
| Auth | `AuthController` | `/api/v1/auth` | Logout and federated logout |
| System | `SystemController` | `/api/v1` | Health check, public config |
| Experiments | `ExperimentController` | `/api/v1/experiments` | Experiment lifecycle management |
| Processes | `ProcessController` | `/api/v1/processes` | Process execution and monitoring |
| Jobs | `JobController` | `/api/v1/jobs` | Job status and management |
| Applications | `ApplicationInterfaceController` | `/api/v1/application-interfaces` | Application interface definitions |
| Application Modules | `ApplicationModuleController` | `/api/v1/application-modules` | Application module CRUD |
| Deployments | `ApplicationDeploymentController` | `/api/v1/application-deployments` | Application deployment configurations |
| Compute Resources | `ComputeResourceController` | `/api/v1/compute-resources` | Compute resource management |
| Storage Resources | `StorageResourceController` | `/api/v1/storage-resources` | Storage resource management |
| Projects | `ProjectController` | `/api/v1/projects` | Project management and resource accounts |
| Gateways | `GatewayController` | `/api/v1/gateways` | Gateway CRUD |
| Gateway Config | `GatewayConfigController` | `/api/v1/gateway-config` | Gateway configuration and feature flags |
| Gateway Resources | `GatewayResourceProfileController` | `/api/v1/gateway-resource-profile` | Gateway resource profiles |
| Group Resources | `GroupResourceProfileController` | `/api/v1/group-resource-profiles` | Group resource profiles |
| User Profiles | `UserResourceProfileController` | `/api/v1/user-resource-profiles` | User resource profiles |
| Users | `UserController` | `/api/v1/users` | User management |
| Groups | `GroupController` | `/api/v1/groups` | Group management and membership |
| Workflows | `WorkflowController` | `/api/v1/workflows` | Workflow definitions |
| Data Products | `DataProductController` | `/api/v1/data-products` | Data product management |
| Notices | `NoticeController` | `/api/v1/notices` | Notification management |
| Preferences | `PreferenceController` | `/api/v1/preferences` | Resource preferences (USER>GROUP>GATEWAY) |
| Credentials | `CredentialController` | `/api/v1` | Credential summaries and SSH/password credential CRUD |
| Resource Access | `ResourceAccessController` | `/api/v1/resource-access` | Access grants (credential + resource + login username) |
| Resource Access Grants | `ResourceAccessGrantController` | `/api/v1/resource-access-grants` | Resource access grant CRUD |
| Cluster Info | `ClusterInfoController` | `/api/v1/cluster-info` | SLURM partitions and grant-tied accounts |
| Connectivity Test | `ConnectivityTestController` | `/api/v1/connectivity-test` | SSH/SFTP/SLURM connectivity validation |
| Statistics | `StatisticsController` | `/api/v1/statistics` | Experiment and system statistics |
| Parsing Templates | `ParsingTemplateController` | `/api/v1/parsing-templates` | Parsing template CRUD |
| System Config | `SystemConfigController` | `/api/v1/system-config` | Global/gateway system configuration |
| SSH Keys | `SSHKeyController` | `/api/v1/ssh-keygen` | SSH key pair generation |
| Catalog | `CatalogController` | `/api/v1/catalog` | Catalog resources (Airavata Portal) |
| Proxy | `ProxyController` | (conditional) | Kafka topic proxy |

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

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `airavata.services.rest.server.port` | - | Maps to Spring Boot `server.port` (uses unified HTTP server port 8090) |
| `airavata.services.http.server.port` | `8090` | Unified HTTP server port |

The Airavata API runs on the unified HTTP server port (8090 by default).

## OpenAPI Documentation

When enabled, the Airavata API provides OpenAPI/Swagger documentation at:
- Swagger UI: `http://localhost:8090/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8090/v3/api-docs`

## Implementation

The main configuration is in:
- `src/main/java/org/apache/airavata/restapi/config/RestApiConfiguration.java`
- `src/main/java/org/apache/airavata/restapi/config/OpenApiConfig.java`

Controllers are in:
- `src/main/java/org/apache/airavata/restapi/controller/`

## Related Documentation

- Main project README: [../../README.md](../../README.md)
- Deployment scripts: [../../dev-tools/deployment-scripts/README.md](../../dev-tools/deployment-scripts/README.md)
