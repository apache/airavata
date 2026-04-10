# Python SDK Facade + Final Gap Fill

## Problem

The Python SDK exposes 16+ low-level gRPC client classes. Consumers (like the Django portal) must know which client to use, manage gRPC channels/metadata directly, and understand transport details. The SDK should provide a single transport-agnostic entry point aligned with the airavata-api module structure.

Additionally, 5 DataProduct/Replica CRUD methods are missing from the SDK.

## Scope

### 1. Fill 5 missing DataProduct SDK methods

Add to the existing APIServerClient (or wherever data product methods currently live):
- `update_data_product`
- `delete_data_product`
- `get_replica_location`
- `update_replica_location`
- `delete_replica_location`

### 2. AiravataClient facade

A single entry point that groups all SDK operations by airavata-api module:

```python
client = AiravataClient(host, port, token, gateway_id)

client.compute       # wraps ResourceService, GatewayResourceProfileService,
                     # GroupResourceProfileService, UserResourceProfileService

client.storage       # wraps StorageResource operations from ResourceService

client.credential    # wraps CredentialService

client.research      # wraps ExperimentService, ProjectService, ApplicationCatalogService,
                     # ParserService, DataProductService, NotificationService,
                     # ExperimentManagementService, ResearchHub/Project/Resource/Session

client.iam           # wraps GatewayService, IamAdminService, UserProfileService

client.sharing       # wraps SharingService, GroupManagerService

client.agent         # wraps AgentInteractionService, PlanService
```

Each sub-client:
- Creates its own gRPC stubs internally (consumers never see stubs/channels)
- Passes auth metadata automatically
- Threads `gateway_id` into calls that need it
- Accepts/returns proto types from `airavata_sdk.generated.*` (no wrapper classes)

### 3. Design principles

- **Transport-agnostic API**: consumers never import `grpc`, create channels, or handle metadata
- **Module-aligned**: sub-client grouping mirrors airavata-api module boundaries exactly
- **Proto types as-is**: consumers import model types from `airavata_sdk.generated.org.apache.airavata.model.*` directly
- **No duplication**: facade delegates to existing low-level clients, doesn't reimplement
- **Backward compatible**: existing low-level clients remain available for advanced use

## Sub-client method mapping

### client.compute
Wraps: ResourceService, GatewayResourceProfileService, GroupResourceProfileService, UserResourceProfileService

Compute resource CRUD, job submission CRUD, batch queue management, gateway/user/group resource profiles, compute/storage preferences, queue statuses, SSH account provisioners.

### client.storage
Wraps: storage-related RPCs from ResourceService (RegisterStorageResource, GetStorageResource, UpdateStorageResource, DeleteStorageResource, GetAllStorageResourceNames, data movement operations)

Note: storage RPCs are currently in ResourceService proto (compute-service), not a separate storage proto. The facade splits them out to match the module boundary.

### client.credential
Wraps: CredentialService

SSH key generation, password credentials, credential summaries, SSH account setup.

### client.research
Wraps: ExperimentService, ProjectService, ApplicationCatalogService, ParserService, DataProductService, NotificationService, ExperimentManagementService, ResearchHubService, ResearchProjectService, ResearchResourceService, ResearchSessionService

Experiments, projects, app modules/deployments/interfaces, parsers, data products/replicas, notifications, research hub sessions, research resources.

### client.iam
Wraps: GatewayService, IamAdminService, UserProfileService

Gateway CRUD, user registration/management, user profiles, role management.

### client.sharing
Wraps: SharingService, GroupManagerService

Domains, sharing users/groups, entities, entity types, permissions, resource sharing, group management.

### client.agent
Wraps: AgentInteractionService, PlanService

Interactive commands (shell, jupyter, python), tunnel management, environment setup, async commands, plans.

## Implementation approach

Each sub-client is a thin class that:
1. Receives a shared `grpc.Channel` and metadata from the parent `AiravataClient`
2. Creates the needed gRPC stubs internally
3. Exposes methods with clear Python-style names (snake_case)
4. Delegates to stub calls, passing metadata automatically

```python
class AiravataClient:
    def __init__(self, host, port, token, gateway_id, secure=False, claims=None):
        self._channel = GrpcChannel(host, port, secure).channel
        self._metadata = build_metadata(token, claims)
        self._gateway_id = gateway_id

        self.compute = ComputeClient(self._channel, self._metadata, self._gateway_id)
        self.storage = StorageClient(self._channel, self._metadata, self._gateway_id)
        self.credential = CredentialClient(self._channel, self._metadata, self._gateway_id)
        self.research = ResearchClient(self._channel, self._metadata, self._gateway_id)
        self.iam = IamClient(self._channel, self._metadata, self._gateway_id)
        self.sharing = SharingClient(self._channel, self._metadata, self._gateway_id)
        self.agent = AgentClient(self._channel, self._metadata, self._gateway_id)
```

## File structure

```
airavata-python-sdk/airavata_sdk/
├── client.py                    # AiravataClient facade
├── facade/
│   ├── __init__.py
│   ├── compute.py               # ComputeClient
│   ├── storage.py               # StorageClient
│   ├── credential.py            # CredentialClient
│   ├── research.py              # ResearchClient
│   ├── iam.py                   # IamClient
│   ├── sharing.py               # SharingClient
│   └── agent.py                 # AgentClient
├── clients/                     # existing low-level clients (unchanged)
├── transport/                   # existing transport layer (unchanged)
└── generated/                   # existing proto stubs (unchanged)
```

## Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Proto types | Use as-is from generated namespace | No wrapper classes, portal imports from SDK |
| Existing clients | Keep alongside facade | Backward compatible, facade can delegate to them |
| Storage split | Separate sub-client despite shared proto | Matches airavata-api module boundary |
| gateway_id | Stored on facade, threaded to calls | Most RPCs need it, avoids repetition |
| Orchestration | Excluded | Server-side only, not client-facing |
