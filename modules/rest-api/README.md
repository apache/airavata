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

All endpoints are prefixed with `/api/v1/` and follow RESTful conventions.

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
