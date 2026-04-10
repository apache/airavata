# gRPC CRUD + Python SDK Completeness

## Problem

After the gRPC/Armeria migration, several services have incomplete CRUD operations at the gRPC layer, and the Python SDK is missing 42 client methods across 8 existing services plus 7 entire service clients.

## Scope

### Part 1: gRPC CRUD Gap Fixes (Java)

Add missing RPC methods to proto files and implement gRPC handlers. The service/repository layer already has these methods — only proto definitions and thin handler wiring are needed.

**DataProductService** (research-service) — 5 new RPCs:
- `UpdateDataProduct` — PUT `/api/v1/data-products/{product_uri}`
- `DeleteDataProduct` — DELETE `/api/v1/data-products/{product_uri}`
- `GetReplicaLocation` — GET `/api/v1/data-products/replicas/{replica_id}`
- `UpdateReplicaLocation` — PUT `/api/v1/data-products/replicas/{replica_id}`
- `DeleteReplicaLocation` — DELETE `/api/v1/data-products/replicas/{replica_id}`

**GroupResourceProfileService** (compute-service) — 3 new RPCs:
- `CreateGroupComputePreference` — POST `/api/v1/group-profiles/{group_resource_profile_id}/compute-preferences`
- `CreateGroupComputeResourcePolicy` — POST `/api/v1/group-profiles/{group_resource_profile_id}/compute-resource-policies`
- `CreateGroupBatchQueuePolicy` — POST `/api/v1/group-profiles/{group_resource_profile_id}/batch-queue-policies`

### Part 2: Python SDK Completeness

**7 new service client classes + stub factories:**

| Service | RPCs | Client Class |
|---------|------|-------------|
| AgentInteractionService | 20 | AgentInteractionClient |
| ExperimentManagementService | 5 | ExperimentManagementClient |
| PlanService | 4 | PlanClient |
| ResearchHubService | 2 | ResearchHubClient |
| ResearchProjectService | 4 | ResearchProjectClient |
| ResearchResourceService | 15 | ResearchResourceClient |
| ResearchSessionService | 4 | ResearchSessionClient |

Each needs: stub factory in `transport/utils.py`, client class in `clients/`, methods matching every RPC.

**42 missing methods in 8 existing client classes:**

| Client | Missing Count | Category |
|--------|--------------|----------|
| APIServerClient (ResourceService) | 15 | Job submission + data movement CRUD |
| APIServerClient (GatewayResourceProfile) | 10 | Compute/storage preference CRUD |
| APIServerClient (UserResourceProfile) | 5 | Compute preference CRUD |
| SharingRegistryClient | 3 | RevokeFromUsers, RevokeFromGroups, UpdateUser |
| APIServerClient (GroupResourceProfile) | 3 | GetGroupComputePreference, preference/policy lists |
| APIServerClient (ExperimentService) | 2 | Intermediate outputs |
| APIServerClient (ApplicationCatalog) | 2 | Available resources, deployments for module |
| CredentialStoreClient | 2 | SSH setup check/account |

**Excluded (server-to-agent internal):** AgentCommunicationService, FuseService

## Approach

1. Add new proto RPCs with `google.api.http` annotations (REST transcoding)
2. Implement thin gRPC handler methods that delegate to existing service layer
3. Regenerate Python proto stubs from updated proto files
4. Add stub factories to `transport/utils.py` for 7 new services
5. Create 7 new client classes following existing client patterns
6. Add 42 missing methods to 8 existing client classes

## Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Agent internal services (Fuse, AgentCommunication) | Excluded | Server-to-agent only, not user-facing |
| AgentInteractionService | Included | Users interact with agents for interactive experiments |
| Service layer changes | None needed | Existing service/repository methods cover all CRUD operations |
| Python stub regeneration | Required | New proto RPCs need corresponding Python stubs |
