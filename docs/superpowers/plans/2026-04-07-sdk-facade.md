# Python SDK Facade + Final Gap Fill — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a transport-agnostic `AiravataClient` facade to the Python SDK with module-aligned sub-clients, and fill the 5 remaining DataProduct gRPC gaps.

**Architecture:** `AiravataClient` creates a shared gRPC channel + metadata, then instantiates 7 sub-client classes (one per airavata-api module). Each sub-client creates its own stubs and delegates to them. Existing low-level clients remain untouched for backward compatibility.

**Tech Stack:** Python 3.10+, grpcio, protobuf

---

## File Structure

```
airavata-python-sdk/airavata_sdk/
├── client.py                      # AiravataClient entry point
├── facade/
│   ├── __init__.py                # Exports all sub-clients
│   ├── compute.py                 # ComputeClient
│   ├── storage.py                 # StorageClient
│   ├── credential.py              # CredentialClient
│   ├── research.py                # ResearchClient
│   ├── iam.py                     # IamClient
│   ├── sharing.py                 # SharingClient
│   └── agent.py                   # AgentClient
├── clients/                       # Existing low-level clients (UNCHANGED)
│   └── api_server_client.py       # Add 5 DataProduct methods here
├── transport/utils.py             # Existing stub factories (UNCHANGED)
└── generated/                     # Proto stubs (UNCHANGED)
```

---

### Task 1: Add 5 missing DataProduct SDK methods

**Files:**
- Modify: `airavata-python-sdk/airavata_sdk/clients/api_server_client.py`

- [ ] **Step 1: Read existing DataProduct methods to match pattern**

The existing methods (around line 1203) follow this pattern:
```python
def register_data_product(self, data_product):
    pb2 = self._svc("data_product_service_pb2")
    return self._data_product.RegisterDataProduct(
        pb2.RegisterDataProductRequest(data_product=data_product),
        metadata=self._metadata,
    )
```

- [ ] **Step 2: Add 5 new methods after the existing DataProduct methods**

Add after `get_child_data_products`:

```python
def update_data_product(self, product_uri, data_product):
    pb2 = self._svc("data_product_service_pb2")
    return self._data_product.UpdateDataProduct(
        pb2.UpdateDataProductRequest(product_uri=product_uri, data_product=data_product),
        metadata=self._metadata,
    )

def delete_data_product(self, product_uri):
    pb2 = self._svc("data_product_service_pb2")
    return self._data_product.DeleteDataProduct(
        pb2.DeleteDataProductRequest(product_uri=product_uri),
        metadata=self._metadata,
    )

def get_replica_location(self, replica_id):
    pb2 = self._svc("data_product_service_pb2")
    return self._data_product.GetReplicaLocation(
        pb2.GetReplicaLocationRequest(replica_id=replica_id),
        metadata=self._metadata,
    )

def update_replica_location(self, replica_id, replica_location):
    pb2 = self._svc("data_product_service_pb2")
    return self._data_product.UpdateReplicaLocation(
        pb2.UpdateReplicaLocationRequest(replica_id=replica_id, replica_location=replica_location),
        metadata=self._metadata,
    )

def delete_replica_location(self, replica_id):
    pb2 = self._svc("data_product_service_pb2")
    return self._data_product.DeleteReplicaLocation(
        pb2.DeleteReplicaLocationRequest(replica_id=replica_id),
        metadata=self._metadata,
    )
```

- [ ] **Step 3: Verify import works**

```bash
cd airavata-python-sdk && python3 -c "from airavata_sdk.clients.api_server_client import APIServerClient; print('OK')"
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat: add 5 missing DataProduct/Replica CRUD methods to APIServerClient"
```

---

### Task 2: Create facade infrastructure

**Files:**
- Create: `airavata-python-sdk/airavata_sdk/facade/__init__.py`
- Create: `airavata-python-sdk/airavata_sdk/client.py`

- [ ] **Step 1: Create facade package**

Create `airavata-python-sdk/airavata_sdk/facade/__init__.py`:

```python
from airavata_sdk.facade.compute import ComputeClient
from airavata_sdk.facade.storage import StorageClient
from airavata_sdk.facade.credential import CredentialClient
from airavata_sdk.facade.research import ResearchClient
from airavata_sdk.facade.iam import IamClient
from airavata_sdk.facade.sharing import SharingClient
from airavata_sdk.facade.agent import AgentClient

__all__ = [
    "ComputeClient",
    "StorageClient",
    "CredentialClient",
    "ResearchClient",
    "IamClient",
    "SharingClient",
    "AgentClient",
]
```

- [ ] **Step 2: Create AiravataClient entry point**

Create `airavata-python-sdk/airavata_sdk/client.py`:

```python
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import json
import logging
from typing import Optional

import grpc

log = logging.getLogger(__name__)


class AiravataClient:
    """Transport-agnostic entry point for the Airavata Python SDK.

    Groups all operations by airavata-api module:
        client.compute      — HPC resources, profiles, scheduling
        client.storage      — Storage resources, data movement
        client.credential   — SSH keys, passwords
        client.research     — Experiments, projects, apps, parsers, data products
        client.iam          — Gateways, users, identity
        client.sharing      — Permissions, groups, entities
        client.agent        — Interactive agents, plans
    """

    def __init__(
        self,
        host: str,
        port: int,
        token: str,
        gateway_id: str,
        secure: bool = False,
        claims: Optional[dict] = None,
    ):
        target = f"{host}:{port}"
        if secure:
            self._channel = grpc.secure_channel(target, grpc.ssl_channel_credentials())
        else:
            self._channel = grpc.insecure_channel(target)

        self._metadata: list[tuple[str, str]] = []
        if token:
            self._metadata.append(("authorization", f"Bearer {token}"))
        if claims:
            self._metadata.append(("x-claims", json.dumps(claims)))

        self._gateway_id = gateway_id

        from airavata_sdk.facade import (
            AgentClient,
            ComputeClient,
            CredentialClient,
            IamClient,
            ResearchClient,
            SharingClient,
            StorageClient,
        )

        self.compute = ComputeClient(self._channel, self._metadata, self._gateway_id)
        self.storage = StorageClient(self._channel, self._metadata, self._gateway_id)
        self.credential = CredentialClient(self._channel, self._metadata, self._gateway_id)
        self.research = ResearchClient(self._channel, self._metadata, self._gateway_id)
        self.iam = IamClient(self._channel, self._metadata, self._gateway_id)
        self.sharing = SharingClient(self._channel, self._metadata, self._gateway_id)
        self.agent = AgentClient(self._channel, self._metadata, self._gateway_id)

        log.debug(f"AiravataClient connected to {target} (gateway={gateway_id})")

    def close(self):
        """Close the gRPC channel."""
        self._channel.close()

    def __enter__(self):
        return self

    def __exit__(self, *args):
        self.close()
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: add AiravataClient facade entry point and package structure"
```

---

### Task 3: Implement ComputeClient sub-client

**Files:**
- Create: `airavata-python-sdk/airavata_sdk/facade/compute.py`

- [ ] **Step 1: Create ComputeClient**

This wraps: ResourceService (compute resource ops + job submissions + data movements), GatewayResourceProfileService, GroupResourceProfileService, UserResourceProfileService.

Read the existing `APIServerClient` methods for these 4 services to understand the exact method signatures and stub calls. Then create `ComputeClient` that delegates to the same stubs but receives channel/metadata from the parent.

The class structure:

```python
import importlib

from airavata_sdk.transport.utils import (
    create_gateway_resource_profile_service_stub,
    create_group_resource_profile_service_stub,
    create_resource_service_stub,
    create_user_resource_profile_service_stub,
)


class ComputeClient:
    """Compute module: HPC resources, profiles, job submissions, data movements, queue statuses."""

    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._resource = create_resource_service_stub(channel)
        self._gw_profile = create_gateway_resource_profile_service_stub(channel)
        self._grp_profile = create_group_resource_profile_service_stub(channel)
        self._user_profile = create_user_resource_profile_service_stub(channel)

    @staticmethod
    def _svc(name):
        return importlib.import_module(f"airavata_sdk.generated.services.{name}")

    # --- Compute Resources ---
    def register_compute_resource(self, compute_resource):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.RegisterComputeResource(
            pb2.RegisterComputeResourceRequest(compute_resource=compute_resource),
            metadata=self._metadata,
        )

    # ... (all ResourceService compute methods)
    # ... (all job submission methods)
    # ... (all GatewayResourceProfile methods)
    # ... (all GroupResourceProfile methods)
    # ... (all UserResourceProfile methods)
    # ... (queue status methods)
```

Copy ALL compute/resource/profile methods from `APIServerClient`, adjusting only the `self._metadata` reference (no need to change — it's the same pattern).

Include ALL methods from these 4 services. Do NOT skip any.

- [ ] **Step 2: Verify import**

```bash
cd airavata-python-sdk && python3 -c "from airavata_sdk.facade.compute import ComputeClient; print('OK')"
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: add ComputeClient facade sub-client"
```

---

### Task 4: Implement StorageClient sub-client

**Files:**
- Create: `airavata-python-sdk/airavata_sdk/facade/storage.py`

- [ ] **Step 1: Create StorageClient**

Wraps storage-related RPCs from ResourceService: RegisterStorageResource, GetStorageResource, UpdateStorageResource, DeleteStorageResource, GetAllStorageResourceNames, plus data movement operations (AddLocalDataMovement, UpdateLocalDataMovement, GetLocalDataMovement, AddSCPDataMovement, UpdateSCPDataMovement, GetSCPDataMovement, AddGridFTPDataMovement, UpdateGridFTPDataMovement, GetGridFTPDataMovement, DeleteDataMovementInterface).

Uses the same `create_resource_service_stub` since these RPCs are in resource_service.proto.

```python
import importlib
from airavata_sdk.transport.utils import create_resource_service_stub


class StorageClient:
    """Storage module: storage resources, data movement interfaces and protocols."""

    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._resource = create_resource_service_stub(channel)

    @staticmethod
    def _svc(name):
        return importlib.import_module(f"airavata_sdk.generated.services.{name}")

    # --- Storage Resources ---
    def register_storage_resource(self, storage_resource):
        pb2 = self._svc("resource_service_pb2")
        return self._resource.RegisterStorageResource(
            pb2.RegisterStorageResourceRequest(storage_resource=storage_resource),
            metadata=self._metadata,
        )

    # ... (all storage resource + data movement methods from APIServerClient)
```

- [ ] **Step 2: Commit**

```bash
git add -A && git commit -m "feat: add StorageClient facade sub-client"
```

---

### Task 5: Implement CredentialClient sub-client

**Files:**
- Create: `airavata-python-sdk/airavata_sdk/facade/credential.py`

- [ ] **Step 1: Create CredentialClient**

Wraps CredentialService. Copy all methods from `CredentialStoreClient` but accept channel/metadata from parent instead of creating its own.

```python
import importlib
from airavata_sdk.transport.utils import create_credential_service_stub


class CredentialClient:
    """Credential module: SSH keys, passwords, credential store."""

    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._stub = create_credential_service_stub(channel)

    @staticmethod
    def _svc(name):
        return importlib.import_module(f"airavata_sdk.generated.services.{name}")

    # ... (all CredentialService methods from CredentialStoreClient)
```

- [ ] **Step 2: Commit**

```bash
git add -A && git commit -m "feat: add CredentialClient facade sub-client"
```

---

### Task 6: Implement ResearchClient sub-client

**Files:**
- Create: `airavata-python-sdk/airavata_sdk/facade/research.py`

- [ ] **Step 1: Create ResearchClient**

This is the largest sub-client. Wraps: ExperimentService, ProjectService, ApplicationCatalogService, ParserService, DataProductService, NotificationService, ExperimentManagementService, ResearchHubService, ResearchProjectService, ResearchResourceService, ResearchSessionService.

```python
import importlib
from airavata_sdk.transport.utils import (
    create_application_catalog_service_stub,
    create_data_product_service_stub,
    create_experiment_management_service_stub,
    create_experiment_service_stub,
    create_notification_service_stub,
    create_parser_service_stub,
    create_project_service_stub,
    create_research_hub_service_stub,
    create_research_project_service_stub,
    create_research_resource_service_stub,
    create_research_session_service_stub,
)


class ResearchClient:
    """Research module: experiments, projects, app catalog, parsers, data products, notifications, research hub."""

    def __init__(self, channel, metadata, gateway_id):
        self._metadata = metadata
        self._gateway_id = gateway_id
        self._experiment = create_experiment_service_stub(channel)
        self._project = create_project_service_stub(channel)
        self._app_catalog = create_application_catalog_service_stub(channel)
        self._parser = create_parser_service_stub(channel)
        self._data_product = create_data_product_service_stub(channel)
        self._notification = create_notification_service_stub(channel)
        self._exp_mgmt = create_experiment_management_service_stub(channel)
        self._research_hub = create_research_hub_service_stub(channel)
        self._research_project = create_research_project_service_stub(channel)
        self._research_resource = create_research_resource_service_stub(channel)
        self._research_session = create_research_session_service_stub(channel)

    # ... (ALL methods from: ExperimentService, ProjectService,
    #      ApplicationCatalogService, ParserService, DataProductService,
    #      NotificationService sections of APIServerClient,
    #      PLUS all methods from ExperimentManagementClient,
    #      ResearchHubClient, ResearchProjectClient,
    #      ResearchResourceClient, ResearchSessionClient)
```

Copy methods from `APIServerClient` (experiment, project, app catalog, parser, data product, notification sections) AND from the 5 new research client classes.

Include the 5 new DataProduct methods from Task 1.

- [ ] **Step 2: Commit**

```bash
git add -A && git commit -m "feat: add ResearchClient facade sub-client"
```

---

### Task 7: Implement IamClient, SharingClient, AgentClient sub-clients

**Files:**
- Create: `airavata-python-sdk/airavata_sdk/facade/iam.py`
- Create: `airavata-python-sdk/airavata_sdk/facade/sharing.py`
- Create: `airavata-python-sdk/airavata_sdk/facade/agent.py`

- [ ] **Step 1: Create IamClient**

Wraps GatewayService, IamAdminService, UserProfileService.

Copy methods from:
- `APIServerClient` gateway section
- `IAMAdminClient` 
- `UserProfileClient`

```python
from airavata_sdk.transport.utils import (
    create_gateway_service_stub,
    create_iam_admin_service_stub,
    create_user_profile_service_stub,
)
```

- [ ] **Step 2: Create SharingClient**

Wraps SharingService, GroupManagerService.

Copy methods from:
- `SharingRegistryClient` (65+ methods)
- `GroupManagerClient` (13 methods)

Note: Some methods overlap between SharingService and GroupManagerService (group CRUD). Include both sets — the facade consumer picks the one they need. Prefix overlapping methods if needed (e.g., `sharing_create_group` vs `group_manager_create_group`) or just expose both stubs' methods with their natural names if they differ.

```python
from airavata_sdk.transport.utils import (
    create_sharing_service_stub,
    create_group_manager_service_stub,
)
```

- [ ] **Step 3: Create AgentClient**

Wraps AgentInteractionService, PlanService.

Copy methods from:
- `AgentInteractionClient` (20 methods)
- `PlanClient` (4 methods)

```python
from airavata_sdk.transport.utils import (
    create_agent_interaction_service_stub,
    create_plan_service_stub,
)
```

- [ ] **Step 4: Verify all imports**

```bash
cd airavata-python-sdk && python3 -c "
from airavata_sdk.client import AiravataClient
print('AiravataClient OK')
"
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: add IamClient, SharingClient, AgentClient facade sub-clients"
```

---

### Task 8: Export facade from package top-level

**Files:**
- Modify: `airavata-python-sdk/airavata_sdk/__init__.py`

- [ ] **Step 1: Add AiravataClient to top-level exports**

Add to the end of `airavata_sdk/__init__.py`:

```python
from airavata_sdk.client import AiravataClient

__all__ = ["Settings", "AiravataClient"]
```

This allows:
```python
from airavata_sdk import AiravataClient
```

- [ ] **Step 2: Verify end-to-end import**

```bash
cd airavata-python-sdk && python3 -c "
from airavata_sdk import AiravataClient
print('Top-level import OK')
print('Sub-clients:', ['compute', 'storage', 'credential', 'research', 'iam', 'sharing', 'agent'])
"
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: export AiravataClient from airavata_sdk top-level package"
```

---

## Final Verification

- [ ] **Import check**: `python3 -c "from airavata_sdk import AiravataClient; print('OK')"`
- [ ] **Java build** (unchanged): `cd airavata-api && mvn compile -T4`
- [ ] **Java tests** (unchanged): `cd airavata-api && mvn test -T4`
