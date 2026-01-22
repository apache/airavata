# Thrift API Module

The Thrift API module provides Thrift endpoints for Airavata API functions via a unified Thrift server.

## API Type

- **Server**: Thrift Server
- **Port**: 8930 (default)
- **Configuration**: `airavata.services.thrift.server.port`

## Overview

The Thrift Server provides Thrift Endpoints for Airavata API functions. It uses a multiplexed processor to expose multiple internal services through a single Thrift Server endpoint on port 8930.

This is one of four external API layers in Airavata:
- **Thrift Server** (port 8930) - Thrift Endpoints for Airavata API functions (this server)
- **HTTP Server** (port 8080):
  - Airavata API - HTTP Endpoints for Airavata API functions
  - File API - HTTP Endpoints for file upload/download
  - Agent API - HTTP Endpoints for interactive job contexts
  - Research API - HTTP Endpoints for use by research hub
- **gRPC Server** (port 9090) - For airavata binaries to open persistent channels with airavata APIs
- **Dapr gRPC** (port 50001) - Sidecar for pub/sub, state, and workflow execution

## Multiplexed Services

The following services are exposed through this unified Thrift Server endpoint using service name prefixes:

| Service Name | Description |
|--------------|-------------|
| `Airavata` | Main Airavata service for experiments, processes, and workflows |
| `ProfileService.UserProfileService` | User profile management |
| `ProfileService.TenantProfileService` | Tenant/gateway profile management |
| `ProfileService.IamAdminServices` | IAM administration |
| `ProfileService.GroupManagerService` | Group management |
| `OrchestratorService` | Workflow orchestration (internal service) |
| `RegistryService` | Application and metadata registry (internal service) |
| `CredentialStoreService` | Secure credential storage (internal service) |
| `SharingRegistryService` | Permissions and sharing (internal service) |

## Internal Services Used

The following internal services are accessed via Thrift Server (they are not separate servers):

- **Orchestrator** - Constructs workflow DAGs
- **Registry** - Manages metadata and application definitions
- **Profile Service** - Manages users, tenants, compute resources
- **Sharing Registry** - Handles permissions and sharing
- **Credential Store** - Secure storage of credentials

These services are multiplexed through the unified Thrift Server endpoint and are not directly accessible as separate servers.

## Thrift Interface Definitions

Thrift IDL files are located in the `thrift-interface-descriptions/` directory at the project root:

- `airavata-apis/airavata_api.thrift` - Main Airavata API
- `service-cpis/` - Service CPI definitions
- `data-models/` - Data model definitions

See the main project README.md for stub generation instructions.

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `airavata.services.thrift.enabled` | `true` | Enable/disable Thrift server |
| `airavata.services.thrift.server.port` | `8930` | Thrift server port |
| `airavata.security.tls.enabled` | `false` | Enable TLS encryption |
| `airavata.security.tls.keystore.path` | - | Keystore file path (relative to config directory) |
| `airavata.security.tls.keystore.password` | - | Keystore password |
| `airavata.security.tls.client-timeout` | `10000` | TLS client timeout (ms) |

## TLS Support

When TLS is enabled via `airavata.security.tls.enabled=true`, the server uses the keystore configured in `airavata.security.tls.keystore.path` (relative to the configuration directory).

## Implementation

The main server implementation is in:
- `src/main/java/org/apache/airavata/thriftapi/server/ThriftServer.java`

Service handlers are in:
- `src/main/java/org/apache/airavata/thriftapi/handler/`

## Related Documentation

- Main project README: [../../README.md](../../README.md)
- Deployment scripts: [../../dev-tools/deployment-scripts/README.md](../../dev-tools/deployment-scripts/README.md)
- Ansible deployment: [../../dev-tools/ansible/README.md](../../dev-tools/ansible/README.md)
