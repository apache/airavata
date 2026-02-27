# Airavata API Module

The Airavata API module provides HTTP endpoints for all core API functionalities, running as part of the unified HTTP server.

## API Type

- **Server**: HTTP Server (unified)
- **Port**: 8090 (default)
- **Configuration**: `server.port`

## Overview

The Airavata API provides HTTP endpoints for all core API functionalities. It runs as part of the unified HTTP server on port 8090.

See [Architecture](../../../README.md#architecture) for the full API layer overview.

## Airavata API Endpoints

The Airavata API provides HTTP endpoints for all core functionalities:

| Endpoint Category | Controller | Base Path | Description |
|-------------------|------------|-----------|-------------|
| Auth | `AuthController` | `/api/v1/auth` | Logout and federated logout |
| System | `SystemController` | `/api/v1` | Health check, public config |
| Experiments | `ExperimentController` | `/api/v1/experiments` | Experiment lifecycle management |
| Processes | `ProcessController` | `/api/v1/processes` | Process execution and monitoring |
| Jobs | `JobController` | `/api/v1/jobs` | Job status and management |
| Applications | `ApplicationController` | `/api/v1/applications` | Application CRUD |
| Installations | `ApplicationInstallationController` | `/api/v1/installations` | Application installation on resources |
| Resources | `ResourceController` | `/api/v1/resources` | Unified compute/storage resource management |
| Resource Bindings | `ResourceBindingController` | `/api/v1/bindings` | Credential-resource binding management |
| Projects | `ProjectController` | `/api/v1/projects` | Project management and resource accounts |
| Allocation Projects | `AllocationProjectController` | `/api/v1/allocation-projects` | HPC allocation project management |
| Gateways | `GatewayController` | `/api/v1/gateways` | Gateway CRUD |
| Gateway Config | `GatewayConfigController` | `/api/v1/gateway-config` | Gateway configuration and feature flags |
| Users | `UserController` | `/api/v1/users` | User management |
| Groups | `GroupController` | `/api/v1/groups` | Group management and membership |
| Credentials | `CredentialController` | `/api/v1` | Credential summaries and SSH/password credential CRUD |
| Workflows | `WorkflowController` | `/api/v1/workflows` | Workflow definitions |
| Workflow Runs | `WorkflowRunController` | `/api/v1/workflow-runs` | Workflow run execution and status |
| Notices | `NoticeController` | `/api/v1/notices` | Notification management |
| Statistics | `StatisticsController` | `/api/v1/statistics` | Experiment and system statistics |
| System Config | `SystemConfigController` | `/api/v1/system-config` | Global/gateway system configuration |
| SSH Keys | `SSHKeyController` | `/api/v1/ssh-keygen` | SSH key pair generation |
| Connectivity Test | `ConnectivityTestController` | `/api/v1/connectivity-test` | SSH/SFTP/SLURM connectivity validation |
| Monitoring | `MonitoringJobStatusController` | `/api/v1/monitoring` | Job status callback endpoint |
| Research Hub | `ResearchHubController` | `/api/v1/research-hub` | Research hub portal |
| Research Artifacts | `ResearchArtifactController` | `/api/v1/research/artifacts` | Research artifact CRUD |
| Research Projects | `ResearchProjectController` | `/api/v1/research/artifacts/projects` | Research project management |
| Research Sessions | `ResearchSessionController` | `/api/v1/research-hub/sessions` | Research session management |
| Agents | `AgentController` | `/api/v1/agents` | Agent registration and management |
| Agent Experiments | `AgentExperimentController` | `/api/v1/agent/experiments` | Agent-side experiment operations |
| Files | `FileController` | `/api/v1/files` | File upload/download |
| Plans | `PlanController` | `/api/v1/plans` | Execution plan management |

All endpoints are prefixed with `/api/v1/` and follow RESTful conventions.

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8090` | Unified HTTP server port |
| `spring.grpc.server.port` | `9090` | gRPC server port |

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

- Main project README: [../../../README.md](../../../README.md)
- Deployment scripts: [../../../deployment/scripts/README.md](../../../deployment/scripts/README.md)
