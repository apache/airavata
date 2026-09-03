# Airavata HTTP API

Base URL: `http://localhost:9095` (default `SERVER_PORT` is `9095`; override via the `SERVER_PORT` env var).

All request/response bodies are JSON (`Content-Type: application/json`). Writes require an `Authorization: Bearer <token>` header for a principal with `ADMIN` or `SUPER_ADMIN` authority; catalog reads (`GET`) are open without a token. [Groups](#groups), [data products](#data-products) and [SCP data storages](#scp-data-storages) are the exception on both counts — they are reached through ownership and sharing rules rather than platform roles, so any authenticated caller may create them, and none are readable anonymously. See INSTALL.md for how to obtain the root token.

## Error responses

Every failure — validation, authorization, missing record — comes back in the same envelope:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Cluster not found: c1d2e3f4-5678-4abc-9def-0123456789ab"
}
```

`error` is the standard reason phrase for `status`, and `message` is the human-readable detail. Request-body validation adds a `fieldErrors` array listing every constraint that failed, so a client can report all of them at once rather than one per round trip:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "clusterName", "message": "Cluster name cannot be blank" }
  ]
}
```

`fieldErrors` is omitted on every other kind of failure. Field names are the JSON paths of the request body, including indexes and nesting — `inputs[0].inputType`, `defaultBatchJobConfig.wallTimeMinutes`.

The statuses in use:

| Status | Meaning |
|---|---|
| `400 Bad Request` | malformed JSON, or a body that failed its constraints |
| `401 Unauthorized` | no token where one is required, or a token that is present but unusable — the caller can fix this by presenting credentials. An invalid token also carries `WWW-Authenticate: Bearer error="invalid_token"` |
| `403 Forbidden` | authenticated, but lacking the authority for this call |
| `404 Not Found` | no such record — or, for [groups](#groups), a record the caller has no standing to know exists |
| `409 Conflict` | the request collides with existing state, e.g. deleting an SSH key still in use |
| `502 Bad Gateway` | the identity provider could not be reached to validate the token; not the caller's fault |

Internal failures return `500` with a fixed `"Internal server error"` message: the underlying detail is logged rather than returned, since it may name internal state.

## SSH Endpoints

An SSH endpoint is a host reachable over SSH. It was split out of the cluster, which used to carry a bare host name: separating it lets several clusters share one login host, and lets a credential be held against the host itself rather than against one cluster's view of it.

Endpoints are deployment topology and hold no secret, so reads are open and writes require `ADMIN` or `SUPER_ADMIN`.

### Create SSH Endpoint

```
POST /api/v1/ssh-endpoints
```

**curl example**

The example below captures `sshEndpointId` into `$SSH_ENDPOINT_ID` (requires `jq`), for use when creating a cluster and a credential binding below.

```bash
TOKEN='<the token printed at startup>'

SSH_ENDPOINT_ID=$(curl -s -X POST localhost:9095/api/v1/ssh-endpoints \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "expanse-login",
    "hostName": "login.expanse.sdsc.edu",
    "port": 22
  }' | jq -r '.sshEndpointId')

echo "$SSH_ENDPOINT_ID"
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `name` | string | required, cannot be blank |
| `hostName` | string | required, cannot be blank |
| `port` | integer \| null | optional, 1–65535; defaults to `22` |

**Response — `201 Created`**

```json
{
  "sshEndpointId": "3f2a1b0c-9d8e-4f7a-8b6c-5d4e3f2a1b0c",
  "name": "expanse-login",
  "hostName": "login.expanse.sdsc.edu",
  "port": 22
}
```

### Read, Update and Delete SSH Endpoints

```
GET    /api/v1/ssh-endpoints
GET    /api/v1/ssh-endpoints/{sshEndpointId}
PUT    /api/v1/ssh-endpoints/{sshEndpointId}
DELETE /api/v1/ssh-endpoints/{sshEndpointId}
```

Updating an endpoint silently redirects every cluster and credential that names it, which is what moving a login node should do — hence the admin requirement.

`DELETE` returns `204 No Content`, or `409 Conflict` naming how many clusters or credential bindings still reference it. Detach those first; the foreign keys are `RESTRICT`, so the delete is refused rather than cascading into resources people are using.

## Clusters

A cluster is a Slurm-managed HPC resource that batch deployments submit jobs to. It is reached through an [SSH endpoint](#ssh-endpoints) and stages files through an [SCP data storage](#scp-data-storages), both named by id.

The storage is registered first, since a cluster names one as it is created — its create call is documented immediately below, out of the [SCP Data Storages](#scp-data-storages) section further down, which carries the rest of the resource: its routes, its sharing rules and how it is read back.

A storage in turn names an SSH credential, so [Create SSH Key](#create-ssh-key) and [Create SSH Credential](#create-ssh-credential) — documented further down, since they belong to the credential resources rather than to a cluster — are the two calls to run before the one below.

### Create SCP Data Storage

```
POST /api/v1/scp-data-storages
```

Requires an authenticated principal with a `users` row. The owner is taken from the token — there is no owner field in the body — and is not transferable afterwards. Uses `$SSH_CREDENTIAL_ID` from [Create SSH Credential](#create-ssh-credential), and captures `dataId` into `$STORAGE_ID` (requires `jq`) for the cluster below.

A storage names the **account** its data is reached as — an SSH credential, which is a username paired with a key — and not a host. The host comes from the [endpoint credential](#create-ssh-endpoint-credential) whoever moves the data acts under, which is why one account can back both a storage on the login host and another on a lab data host, as the FASTA host in [Register the Run's Data Products](#register-the-runs-data-products) does. A cluster's storage is the login host's own filesystem: where a run's work directory lives, where its file inputs are staged to and its outputs staged from.

```bash
STORAGE_ID=$(curl -s -X POST localhost:9095/api/v1/scp-data-storages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dataName": "expanse-scratch",
    "sshCredentialId": "'"$SSH_CREDENTIAL_ID"'"
  }' | jq -r '.dataId')
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `dataName` | string | required, cannot be blank |
| `sshCredentialId` | string | required, must reference an existing [SSH credential](#create-ssh-credential) |

**Response — `201 Created`**

The credential is inlined, nesting the safe (public-only) summary of the key behind it — the private material is never returned. `permission` is what the calling principal may do with the storage.

```json
{
  "dataId": "d1e2f3a4-5b6c-4d7e-8f90-1a2b3c4d5e6f",
  "dataName": "expanse-scratch",
  "ownerId": "cilogon:12345",
  "sshCredentialId": "7f8e9d0c-1b2a-4c3d-8e4f-5a6b7c8d9e0f",
  "sshCredential": {
    "sshCredentialId": "7f8e9d0c-1b2a-4c3d-8e4f-5a6b7c8d9e0f",
    "username": "airavata",
    "sshKey": {
      "sshKeyId": "8b1a9953-c461-4a3d-9d2f-0a1b2c3d4e5f",
      "sshKeyName": "expanse-key",
      "publicKey": "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIExampleKeyDoNotUse airavata@expanse"
    }
  },
  "permission": "WRITE"
}
```

`DELETE` returns `204 No Content`, or `409 Conflict` when data products are still registered on the storage or a cluster still stages through it. Deleting one takes its shares with it.

### Create Cluster

```
POST /api/v1/clusters
```

Requires `ADMIN` or `SUPER_ADMIN` authority.

**curl example**

Uses `$SSH_ENDPOINT_ID` from [Create SSH Endpoint](#create-ssh-endpoint) and `$STORAGE_ID` from [Create SCP Data Storage](#create-scp-data-storage) above, and captures `clusterId` into `$CLUSTER_ID` (requires `jq`) for the batch deployment step further down.

A cluster whose layout is already known can be carved into partitions in the same call, by carrying them in `partitions`. They are written in the same transaction as the cluster, so a cluster is never visible carrying half the layout it was registered with. A cluster that gains partitions later adds them one at a time through [Create Cluster Partition](#create-cluster-partition) instead.

```bash
CLUSTER_ID=$(curl -s -X POST localhost:9095/api/v1/clusters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clusterName": "expanse",
    "clusterDescription": "SDSC Expanse HPC cluster",
    "sshEndpointId": "'"$SSH_ENDPOINT_ID"'",
    "scpDataStorageId": "'"$STORAGE_ID"'",
    "slurmHome": "/usr/bin",
    "partitions": [
      {
        "name": "compute",
        "description": "General-purpose CPU partition",
        "maxRunTime": 2880,
        "maxNodes": 32,
        "cpuPerNode": 128,
        "defaultNodeCount": 1,
        "defaultCpuCount": 16,
        "defaultWalltime": 240,
        "isDefaultQueue": true
      }
    ]
  }' | jq -r '.clusterId')

echo "$CLUSTER_ID"
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `clusterName` | string | required, cannot be blank |
| `clusterDescription` | string \| null | optional |
| `sshEndpointId` | string | required, must reference an existing SSH endpoint |
| `scpDataStorageId` | string | required, must reference an existing [SCP data storage](#create-scp-data-storage) the caller can reach |
| `slurmHome` | string | required, cannot be blank |
| `partitions` | array \| null | optional, create only. Each element takes the same shape as a [Create Cluster Partition](#create-cluster-partition) body. A `PUT` carrying this field is rejected — see [Read, Update and Delete Clusters](#read-update-and-delete-clusters) |

```json
{
  "clusterName": "expanse",
  "clusterDescription": "SDSC Expanse HPC cluster",
  "sshEndpointId": "3f2a1b0c-9d8e-4f7a-8b6c-5d4e3f2a1b0c",
  "scpDataStorageId": "d1e2f3a4-5b6c-4d7e-8f90-1a2b3c4d5e6f",
  "slurmHome": "/usr/bin",
  "partitions": [
    {
      "name": "compute",
      "description": "General-purpose CPU partition",
      "maxRunTime": 2880,
      "maxNodes": 32,
      "cpuPerNode": 128,
      "defaultNodeCount": 1,
      "defaultCpuCount": 16,
      "defaultWalltime": 240,
      "isDefaultQueue": true
    }
  ]
}
```

**Response — `201 Created`**

`clusterId` is server-generated (UUID), as is every `partitionId`. The endpoint is inlined, since every caller that wants a cluster wants the host it lives on. The storage is not: it is named by id and read through its own endpoint, since it carries an owner and a permission of its own that a cluster read has no business asserting. `partitions` carries whatever the request asked for, and is `[]` for a cluster registered without any — those are added afterwards via `/api/v1/clusters/{clusterId}/partitions`.

```json
{
  "clusterId": "c1d2e3f4-5678-4abc-9def-0123456789ab",
  "clusterName": "expanse",
  "clusterDescription": "SDSC Expanse HPC cluster",
  "sshEndpointId": "3f2a1b0c-9d8e-4f7a-8b6c-5d4e3f2a1b0c",
  "sshEndpoint": {
    "sshEndpointId": "3f2a1b0c-9d8e-4f7a-8b6c-5d4e3f2a1b0c",
    "name": "expanse-login",
    "hostName": "login.expanse.sdsc.edu",
    "port": 22
  },
  "scpDataStorageId": "d1e2f3a4-5b6c-4d7e-8f90-1a2b3c4d5e6f",
  "slurmHome": "/usr/bin",
  "partitions": [
    {
      "partitionId": "4b3a2c1d-0e9f-4876-a5b4-c3d2e1f0a9b8",
      "clusterId": "c1d2e3f4-5678-4abc-9def-0123456789ab",
      "name": "compute",
      "description": "General-purpose CPU partition",
      "maxRunTime": 2880,
      "maxNodes": 32,
      "maxProcessors": null,
      "maxJobsInQueue": null,
      "maxMemory": null,
      "cpuPerNode": 128,
      "defaultNodeCount": 1,
      "defaultCpuCount": 16,
      "defaultWalltime": 240,
      "gres": null,
      "nodes": null,
      "isDefaultQueue": true,
      "isCheckpointable": null
    }
  ]
}
```

**Validation errors — `400 Bad Request`**

Returned when `clusterName`, `sshEndpointId`, `scpDataStorageId` or `slurmHome` is blank, or when an inline partition fails its own constraints — those are reported under the element that carries them, as `partitions[0].name`. An `sshEndpointId` or `scpDataStorageId` that does not resolve to an existing record is returned as `404 Not Found` instead, and a storage the caller cannot reach as `403 Forbidden`.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "clusterName", "message": "Cluster name cannot be blank" },
    { "field": "sshEndpointId", "message": "SSH endpoint id cannot be blank" },
    { "field": "scpDataStorageId", "message": "SCP data storage id cannot be blank" }
  ]
}
```

### Read, Update and Delete Clusters

```
GET    /api/v1/clusters
GET    /api/v1/clusters/{clusterId}
PUT    /api/v1/clusters/{clusterId}
DELETE /api/v1/clusters/{clusterId}
```

Reads are open; `PUT` and `DELETE` require `ADMIN` or `SUPER_ADMIN`. Every read carries the cluster's partitions and its SSH endpoint inline, so a caller that wants the layout never needs a second call.

`PUT` changes a cluster's own fields — including repointing it at a different SSH endpoint or SCP data storage, each re-resolved the way a create resolves it — and leaves its partitions alone. A body carrying `partitions` is **rejected** rather than obeyed:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "partitions", "message": "Partitions are only accepted when a cluster is created; use /api/v1/clusters/{clusterId}/partitions to change them" }
  ]
}
```

The elements of that array carry no ids, so there is nothing to match an incoming partition to an existing row by. Obeying the field would mean replacing the collection wholesale and deleting partitions the caller never mentioned; ignoring it would silently discard what they asked for. Rejecting it says which endpoint to use instead.

`DELETE` returns `204 No Content` and takes the cluster's partitions with it — that foreign key cascades, unlike the `RESTRICT` ones guarding endpoints, credentials and data storages.


### Create Cluster Partition

```
POST /api/v1/clusters/{clusterId}/partitions
```

Requires `ADMIN` or `SUPER_ADMIN` authority.

**curl example**

Uses `$CLUSTER_ID` from [Create Cluster](#create-cluster) above, and captures
`partitionId` into `$PARTITION_ID` (requires `jq`).

```bash
PARTITION_ID=$(curl -s -X POST localhost:9095/api/v1/clusters/"$CLUSTER_ID"/partitions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "gpu",
    "description": "GPU partition for AlphaFold and other accelerated workloads",
    "maxRunTime": 2880,
    "maxNodes": 4,
    "maxProcessors": 128,
    "maxJobsInQueue": 8,
    "maxMemory": 512000,
    "cpuPerNode": 32,
    "defaultNodeCount": 1,
    "defaultCpuCount": 8,
    "defaultWalltime": 720,
    "gres": "gpu:4",
    "nodes": "gpu-node-[01-04]",
    "isDefaultQueue": false,
    "isCheckpointable": true
  }' | jq -r '.partitionId')

echo "$PARTITION_ID"
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `name` | string | required, cannot be blank |
| `description` | string \| null | optional |
| `maxRunTime` | integer \| null | optional, minutes |
| `maxNodes` | integer \| null | optional |
| `maxProcessors` | integer \| null | optional |
| `maxJobsInQueue` | integer \| null | optional |
| `maxMemory` | integer \| null | optional, MB |
| `cpuPerNode` | integer \| null | optional |
| `defaultNodeCount` | integer \| null | optional |
| `defaultCpuCount` | integer \| null | optional |
| `defaultWalltime` | integer \| null | optional, minutes |
| `gres` | string \| null | optional |
| `nodes` | string \| null | optional, Slurm nodelist expression |
| `isDefaultQueue` | boolean \| null | optional |
| `isCheckpointable` | boolean \| null | optional |

```json
{
  "name": "gpu",
  "description": "GPU partition for AlphaFold and other accelerated workloads",
  "maxRunTime": 2880,
  "maxNodes": 4,
  "maxProcessors": 128,
  "maxJobsInQueue": 8,
  "maxMemory": 512000,
  "cpuPerNode": 32,
  "defaultNodeCount": 1,
  "defaultCpuCount": 8,
  "defaultWalltime": 720,
  "gres": "gpu:4",
  "nodes": "gpu-node-[01-04]",
  "isDefaultQueue": false,
  "isCheckpointable": true
}
```

**Response — `201 Created`**

`partitionId` is server-generated (UUID); `clusterId` echoes the path parameter.

```json
{
  "partitionId": "9e8d7c6b-5a4f-4321-8c9d-0e1f2a3b4c5d",
  "clusterId": "c1d2e3f4-5678-4abc-9def-0123456789ab",
  "name": "gpu",
  "description": "GPU partition for AlphaFold and other accelerated workloads",
  "maxRunTime": 2880,
  "maxNodes": 4,
  "maxProcessors": 128,
  "maxJobsInQueue": 8,
  "maxMemory": 512000,
  "cpuPerNode": 32,
  "defaultNodeCount": 1,
  "defaultCpuCount": 8,
  "defaultWalltime": 720,
  "gres": "gpu:4",
  "nodes": "gpu-node-[01-04]",
  "isDefaultQueue": false,
  "isCheckpointable": true
}
```

**Validation errors — `400 Bad Request`**

Returned when `name` is blank.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "name", "message": "Partition name cannot be blank" }
  ]
}
```

### Read, Update and Delete Cluster Partitions

```
GET    /api/v1/clusters/{clusterId}/partitions
GET    /api/v1/clusters/{clusterId}/partitions/{partitionId}
PUT    /api/v1/clusters/{clusterId}/partitions/{partitionId}
DELETE /api/v1/clusters/{clusterId}/partitions/{partitionId}
```

Reads are open; `PUT` and `DELETE` require `ADMIN` or `SUPER_ADMIN`. `PUT` takes the same body as [Create Cluster Partition](#create-cluster-partition).

**Adding partitions to a cluster that already exists.** This is the ordinary case for a cluster that gains a partition after it was registered — a new queue, a GPU rack added later — and it is what [Create Cluster Partition](#create-cluster-partition) is for. A partition belongs to its cluster from the moment it is created: the `clusterId` in the path is the assignment, so there is no separate step that attaches one afterwards, and each call adds a partition without disturbing the ones already there.

```bash
# $CLUSTER_ID was registered earlier, with or without partitions of its own.
curl -s -X POST localhost:9095/api/v1/clusters/"$CLUSTER_ID"/partitions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "name": "largemem", "maxMemory": 2048000, "maxNodes": 2 }'

# The cluster now carries every partition it has been given.
curl -s localhost:9095/api/v1/clusters/"$CLUSTER_ID" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.partitions[].name'
```

Every path is scoped by its cluster, so a `partitionId` belonging to one cluster is a `404` when reached through another — an id alone is never enough to move or read a partition across clusters.

`DELETE` returns `204 No Content`. Nothing holds a foreign key to a partition, so removing one never conflicts; note that a deployment names its default partition by name rather than by id, and deleting the partition it names is not detected here.

### Create SSH Key

```
POST /api/v1/ssh-keys
```

Requires `ADMIN` or `SUPER_ADMIN` authority. Stores the keypair used to authenticate to a cluster; `privateKey` and `passphrase` are write-only — they are never returned by any read endpoint.

**curl example**

The example below captures `sshKeyId` from the response into `$SSH_KEY_ID` (requires `jq`), for use when creating the SSH credential below.

```bash
SSH_KEY_ID=$(curl -s -X POST localhost:9095/api/v1/ssh-keys \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sshKeyName": "expanse-key",
    "publicKey": "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIExampleKeyDoNotUse airavata@expanse",
    "privateKey": "-----BEGIN OPENSSH PRIVATE KEY-----\nExampleKeyDoNotUse\n-----END OPENSSH PRIVATE KEY-----",
    "passphrase": null
  }' | jq -r '.sshKeyId')

echo "$SSH_KEY_ID"
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `sshKeyName` | string | required, cannot be blank |
| `publicKey` | string | required, cannot be blank |
| `privateKey` | string \| null | required on create; omit on update to keep the stored secret |
| `passphrase` | string \| null | optional |

```json
{
  "sshKeyName": "expanse-key",
  "publicKey": "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIExampleKeyDoNotUse airavata@expanse",
  "privateKey": "-----BEGIN OPENSSH PRIVATE KEY-----\nExampleKeyDoNotUse\n-----END OPENSSH PRIVATE KEY-----",
  "passphrase": null
}
```

**Response — `201 Created`**

`sshKeyId` is server-generated (UUID). Note `privateKey` and `passphrase` are absent from the response — they cannot leak through this endpoint.

```json
{
  "sshKeyId": "8b1a9953-c461-4a3d-9d2f-0a1b2c3d4e5f",
  "sshKeyName": "expanse-key",
  "publicKey": "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIExampleKeyDoNotUse airavata@expanse"
}
```

**Validation errors — `400 Bad Request`**

Returned when `sshKeyName` or `publicKey` is blank.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "sshKeyName", "message": "SSH key name cannot be blank" },
    { "field": "publicKey", "message": "Public key cannot be blank" }
  ]
}
```

### Create SSH Credential

```
POST /api/v1/ssh-credentials
```

Requires `ADMIN` or `SUPER_ADMIN` authority. Pairs a login username with an SSH key, producing the id that [Create SSH Endpoint Credential](#create-ssh-endpoint-credential) below binds to a host and that [Create SCP Data Storage](#create-scp-data-storage) registers a storage under.

**curl example**

Uses `$SSH_KEY_ID` from [Create SSH Key](#create-ssh-key) above, and captures `sshCredentialId` into `$SSH_CREDENTIAL_ID` for the next step.

```bash
SSH_CREDENTIAL_ID=$(curl -s -X POST localhost:9095/api/v1/ssh-credentials \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "airavata",
    "sshKeyId": "'"$SSH_KEY_ID"'"
  }' | jq -r '.sshCredentialId')

echo "$SSH_CREDENTIAL_ID"
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `username` | string | required, cannot be blank |
| `sshKeyId` | string | required, must reference an existing SSH key |

```json
{
  "username": "airavata",
  "sshKeyId": "8b1a9953-c461-4a3d-9d2f-0a1b2c3d4e5f"
}
```

**Response — `201 Created`**

`sshCredentialId` is server-generated (UUID); `sshKey` nests the safe (public-only) summary of the key it uses.

```json
{
  "sshCredentialId": "7f8e9d0c-1b2a-4c3d-8e4f-5a6b7c8d9e0f",
  "username": "airavata",
  "sshKey": {
    "sshKeyId": "8b1a9953-c461-4a3d-9d2f-0a1b2c3d4e5f",
    "sshKeyName": "expanse-key",
    "publicKey": "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIExampleKeyDoNotUse airavata@expanse"
  }
}
```

**Validation errors — `400 Bad Request`**

Returned when `username` or `sshKeyId` is blank. An `sshKeyId` that does not resolve to an existing key is returned as `404 Not Found` instead.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "username", "message": "Username cannot be blank" },
    { "field": "sshKeyId", "message": "SSH key id cannot be blank" }
  ]
}
```

### Create SSH Endpoint Credential

```
POST /api/v1/ssh-endpoint-credentials
```

Binds an SSH credential to an [SSH endpoint](#ssh-endpoints) so the caller can act on that host. Requires only an authenticated principal — not admin — but that principal must have a matching row in `users` (see INSTALL.md's "Owning resources requires a matching `users` row"), since the binding's owner is resolved from the token rather than accepted as a request field.

The resulting binding id is what [Create Process](#create-process) below uses as `submissionCredentialId`: a run submits under a specific endpoint credential, not a bare SSH credential, since the binding is what ties the submitting identity to both a host and an owner.

**curl example**

Uses `$SSH_ENDPOINT_ID` from [Create SSH Endpoint](#create-ssh-endpoint) and `$SSH_CREDENTIAL_ID` from [Create SSH Credential](#create-ssh-credential) above, and captures `sshEndpointCredentialId` into `$ENDPOINT_CREDENTIAL_ID` for the process submission step.

```bash
ENDPOINT_CREDENTIAL_ID=$(curl -s -X POST localhost:9095/api/v1/ssh-endpoint-credentials \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sshEndpointId": "'"$SSH_ENDPOINT_ID"'",
    "sshCredentialId": "'"$SSH_CREDENTIAL_ID"'"
  }' | jq -r '.sshEndpointCredentialId')

echo "$ENDPOINT_CREDENTIAL_ID"
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `sshEndpointId` | string | required, must reference an existing SSH endpoint |
| `sshCredentialId` | string | required, must reference an existing SSH credential |

**Response — `201 Created`**

`sshEndpointCredentialId` is server-generated (UUID); `userId` is the owner resolved from the caller's token, not a request field. `permission` is what the *calling* principal may do with it — a property of the request rather than of the record.

```json
{
  "sshEndpointCredentialId": "0a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d",
  "sshEndpointId": "3f2a1b0c-9d8e-4f7a-8b6c-5d4e3f2a1b0c",
  "sshCredentialId": "7f8e9d0c-1b2a-4c3d-8e4f-5a6b7c8d9e0f",
  "userId": "root",
  "permission": "WRITE"
}
```

**Validation errors**

- `400 Bad Request` when `sshEndpointId` or `sshCredentialId` is blank.
- `404 Not Found` when the caller has no matching `users` row, or `sshEndpointId`/`sshCredentialId` does not resolve to an existing record.

### Read, Update and Delete SSH Endpoint Credentials

```
GET    /api/v1/ssh-endpoint-credentials
GET    /api/v1/ssh-endpoint-credentials/me
GET    /api/v1/ssh-endpoint-credentials/shared-with-me
GET    /api/v1/ssh-endpoint-credentials/{id}
PUT    /api/v1/ssh-endpoint-credentials/{id}
DELETE /api/v1/ssh-endpoint-credentials/{id}
```

`GET /api/v1/ssh-endpoint-credentials` lists every binding across every user and requires `ADMIN` or `SUPER_ADMIN` — it exposes who can reach what. Both it and `/me` accept an optional `?sshEndpointId=` filter. `/me` returns the caller's own bindings; `/shared-with-me` returns the ones other users have shared with them, each carrying the permission it grants.

`PUT` repoints a binding at a different endpoint or SSH credential and needs `WRITE`. The owner is never re-derived from the caller's token, so an admin — or a grantee — editing someone's binding does not acquire it.

`DELETE` returns `204 No Content` and removes the binding's shares with it. It is refused for anyone but the owner and platform admins, even a grantee holding `WRITE`.

### Create Group

```
POST /api/v1/groups
```

Requires any authenticated principal. The owner is taken from the token — there is no owner field in the body — and is not transferable afterwards.

**curl example**

The example below captures `groupId` into `$GROUP_ID` (requires `jq`), so it can be reused when managing members further down.

```bash
TOKEN='<a user token>'

GROUP_ID=$(curl -s -X POST localhost:9095/api/v1/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "groupName": "molecular-dynamics" }' | jq -r '.groupId')

echo "$GROUP_ID"
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `groupName` | string | required, cannot be blank |

**Response — `201 Created`**

`groupId` is server-generated (UUID) and `createdAt` is epoch milliseconds. The creator is admitted as an `ACTIVE` member with group role `ADMIN` in the same transaction, so a group is never left with nobody able to administer it.

```json
{
  "groupId": "9f8e7d6c-5b4a-4392-8180-7f6e5d4c3b2a",
  "groupName": "molecular-dynamics",
  "ownerId": "cilogon:12345",
  "createdAt": 1755043200000
}
```

**Validation errors — `400 Bad Request`**

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "groupName", "message": "Group name cannot be blank" }
  ]
}
```

### Read and Update Groups

```
GET    /api/v1/groups
GET    /api/v1/groups/me
GET    /api/v1/groups/{groupId}
PUT    /api/v1/groups/{groupId}
DELETE /api/v1/groups/{groupId}
```

`GET /api/v1/groups` lists every group in the deployment and requires `ADMIN` or `SUPER_ADMIN`; `GET /api/v1/groups/me` is the ordinary caller's equivalent and returns the groups they own or hold a membership in (including suspended ones — a suspended member can still see that the group exists).

`PUT` takes the same body as create and renames the group; `DELETE` returns `204 No Content` and takes the group's membership rows with it.

```bash
curl -s localhost:9095/api/v1/groups/me -H "Authorization: Bearer $TOKEN"
```

### Add Group Member

```
POST /api/v1/groups/{groupId}/members
```

Requires member-manager standing. The user must already be registered.

```bash
curl -s -X POST localhost:9095/api/v1/groups/"$GROUP_ID"/members \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "cilogon:67890",
    "groupRole": "MEMBER"
  }'
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `userId` | string | required, must reference a registered user |
| `groupRole` | string \| null | optional, one of `ADMIN`, `MODERATOR`, `MEMBER`; defaults to `MEMBER` |
| `groupMemberStatus` | string \| null | optional, one of `ACTIVE`, `INACTIVE`; defaults to `ACTIVE` |

**Response — `201 Created`**

```json
{
  "groupId": "9f8e7d6c-5b4a-4392-8180-7f6e5d4c3b2a",
  "userId": "cilogon:67890",
  "groupRole": "MEMBER",
  "groupMemberStatus": "ACTIVE"
}
```

**Errors**

| Status | Cause |
|---|---|
| `400 Bad Request` | `userId` blank, or an unrecognised `groupRole`/`groupMemberStatus` |
| `404 Not Found` | the group is not visible to the caller, or `userId` names no registered user |
| `409 Conflict` | the user is already a member — change the existing membership instead of re-adding it |

### Read, Update and Remove Group Members

```
GET    /api/v1/groups/{groupId}/members
GET    /api/v1/groups/{groupId}/members/{userId}
PUT    /api/v1/groups/{groupId}/members/{userId}
DELETE /api/v1/groups/{groupId}/members/{userId}
```

Reads need reader standing; `PUT` and `DELETE` need member-manager standing, except that any member may remove themselves — leaving a group needs nobody's permission, and works from a suspended membership too.

`PUT` accepts `groupRole` and `groupMemberStatus`, both optional; an omitted field is left as it stands, so a member can be suspended without restating their role. The membership's user comes from the path, so a membership can be changed but never moved to another user.

```bash
# Promote a member to moderator, leaving their status alone.
curl -s -X PUT localhost:9095/api/v1/groups/"$GROUP_ID"/members/cilogon:67890 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "groupRole": "MODERATOR" }'
```

`DELETE` returns `204 No Content`, or `409 Conflict` when the named user owns the group.


### Share an SSH Endpoint Credential

```
GET    /api/v1/ssh-endpoint-credentials/{credentialId}/group-shares
POST   /api/v1/ssh-endpoint-credentials/{credentialId}/group-shares
PUT    /api/v1/ssh-endpoint-credentials/{credentialId}/group-shares/{sharingId}
DELETE /api/v1/ssh-endpoint-credentials/{credentialId}/group-shares/{sharingId}

GET    /api/v1/ssh-endpoint-credentials/{credentialId}/user-shares
POST   /api/v1/ssh-endpoint-credentials/{credentialId}/user-shares
PUT    /api/v1/ssh-endpoint-credentials/{credentialId}/user-shares/{sharingId}
DELETE /api/v1/ssh-endpoint-credentials/{credentialId}/user-shares/{sharingId}
```

A binding can be shared with a [group](#groups) or with a named user. Every one of these routes — reads included — is restricted to the owner and platform admins: the share list names who can reach a host, which is more than a grantee needs to know.

What a share confers:

| Holding | May do |
|---|---|
| `READ` | read the binding, and see it under `/shared-with-me` |
| `WRITE` | the above, and repoint the binding at a different endpoint or SSH credential |
| Ownership | the above, and delete the binding, and manage its shares |

`WRITE` implies `READ`. Control is deliberately not reachable through a share at all: a share lets someone use a credential, while deciding who *else* gets it stays with the owner. Where several shares reach the same caller — say a `READ` user share and a `WRITE` group share — the strongest one applies.

A group share reaches a member only while their membership is `ACTIVE`. Suspending a member withdraws their access without touching the share; reinstating them restores it.

**curl example**

Uses `$ENDPOINT_CREDENTIAL_ID` from above and `$GROUP_ID` from [Create Group](#create-group).

```bash
curl -s -X POST localhost:9095/api/v1/ssh-endpoint-credentials/"$ENDPOINT_CREDENTIAL_ID"/group-shares \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "'"$GROUP_ID"'",
    "permission": "READ"
  }'
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `groupId` / `userId` | string | required; `groupId` for a group share, `userId` for a user share. Must reference an existing record |
| `permission` | string \| null | optional, `READ` or `WRITE`; defaults to `READ` |

`PUT` takes only `permission`, which is required there — the subject of a share is fixed at creation, so widening or narrowing it is the only edit.

**Response — `201 Created`**

```json
{
  "sshEndpointCredentialGroupSharingId": "5c4b3a29-1807-4f6e-9d5c-4b3a29180765",
  "sshEndpointCredentialId": "0a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d",
  "groupId": "9f8e7d6c-5b4a-4392-8180-7f6e5d4c3b2a",
  "permission": "READ"
}
```

A user share is the same shape with `sshEndpointCredentialUserSharingId` and `userId`.

**Errors**

| Status | Cause |
|---|---|
| `400 Bad Request` | `groupId`/`userId` blank, or an unrecognised `permission` |
| `403 Forbidden` | the caller is not the owner — a grantee cannot read or change the share list |
| `404 Not Found` | no such binding, group or user; or a `sharingId` that belongs to a different binding |
| `409 Conflict` | already shared with that group or user (widen the existing share instead), or shared with the owner, which would grant nothing |

## Application Templates

An application template declares an application's input/output contract, independent of where it runs. Deployments (e.g. Slurm) reference a template by id.

### Create Application Template

```
POST /api/v1/application-templates
```

Requires `ADMIN` or `SUPER_ADMIN` authority.

**curl example**

The example below captures `templateId` from the response into `$TEMPLATE_ID` (requires `jq`), for use when creating a batch deployment further down.

```bash
TOKEN='<the token printed at startup>'

TEMPLATE_ID=$(curl -s -X POST localhost:9095/api/v1/application-templates \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "templateName": "AlphaFold2",
    "templateDescription": "AlphaFold2 protein structure prediction application",
    "inputs": [
      {
        "inputName": "fastaFile",
        "displayName": "Protein Sequence",
        "inputDescription": "Target protein sequence in FASTA format",
        "inputType": "FILE",
        "required": true,
        "defaultValue": null
      },
      {
        "inputName": "modelPreset",
        "displayName": "Model Preset",
        "inputDescription": "AlphaFold model preset: monomer, monomer_casp14, monomer_ptm or multimer",
        "inputType": "STRING",
        "required": false,
        "defaultValue": "{\"value\":\"monomer\"}"
      }
    ],
    "outputs": [
      {
        "outputName": "rankedModels",
        "displayName": "Ranked Structures",
        "outputDescription": "Ranked predicted structure PDB files",
        "outputType": "FILE_LIST"
      },
      {
        "outputName": "confidenceScores",
        "displayName": "Confidence Scores",
        "outputDescription": "Per-residue confidence (pLDDT) scores for the top-ranked model",
        "outputType": "FILE"
      }
    ]
  }' | jq -r '.templateId')

echo "$TEMPLATE_ID"
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `templateName` | string | required, cannot be blank |
| `templateDescription` | string \| null | optional |
| `inputs[].inputName` | string | required, unique within the template |
| `inputs[].displayName` | string \| null | optional |
| `inputs[].inputDescription` | string \| null | optional |
| `inputs[].inputType` | string | one of `STRING`, `INTEGER`, `FLOAT`, `BOOLEAN`, `FILE`, `FILE_LIST`, `DIRECTORY` |
| `inputs[].required` | boolean | |
| `inputs[].defaultValue` | string \| null | JSON document: `{"value":"..."}` or `{"values":[...]}` |
| `outputs[].outputName` | string | required |
| `outputs[].displayName` | string \| null | optional |
| `outputs[].outputDescription` | string \| null | optional |
| `outputs[].outputType` | string | one of `FILE`, `FILE_LIST`, `DIRECTORY` |

```json
{
  "templateName": "AlphaFold2",
  "templateDescription": "AlphaFold2 protein structure prediction application",
  "inputs": [
    {
      "inputName": "fastaFile",
      "displayName": "Protein Sequence",
      "inputDescription": "Target protein sequence in FASTA format",
      "inputType": "FILE",
      "required": true,
      "defaultValue": null
    },
    {
      "inputName": "modelPreset",
      "displayName": "Model Preset",
      "inputDescription": "AlphaFold model preset: monomer, monomer_casp14, monomer_ptm or multimer",
      "inputType": "STRING",
      "required": false,
      "defaultValue": "{\"value\":\"monomer\"}"
    }
  ],
  "outputs": [
    {
      "outputName": "rankedModels",
      "displayName": "Ranked Structures",
      "outputDescription": "Ranked predicted structure PDB files",
      "outputType": "FILE_LIST"
    },
    {
      "outputName": "confidenceScores",
      "displayName": "Confidence Scores",
      "outputDescription": "Per-residue confidence (pLDDT) scores for the top-ranked model",
      "outputType": "FILE"
    }
  ]
}
```

**Response — `201 Created`**

`inputId`, `outputId`, and `templateId` are server-generated (UUIDs).

```json
{
  "templateId": "b6f1c2de-3a4b-4c5d-8e6f-7a8b9c0d1e2f",
  "templateName": "AlphaFold2",
  "templateDescription": "AlphaFold2 protein structure prediction application",
  "inputs": [
    {
      "inputId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
      "inputName": "fastaFile",
      "displayName": "Protein Sequence",
      "inputDescription": "Target protein sequence in FASTA format",
      "inputType": "FILE",
      "required": true,
      "defaultValue": null
    },
    {
      "inputId": "2b3c4d5e-6f7a-4b8c-9d0e-1f2a3b4c5d6e",
      "inputName": "modelPreset",
      "displayName": "Model Preset",
      "inputDescription": "AlphaFold model preset: monomer, monomer_casp14, monomer_ptm or multimer",
      "inputType": "STRING",
      "required": false,
      "defaultValue": "{\"value\":\"monomer\"}"
    }
  ],
  "outputs": [
    {
      "outputId": "3c4d5e6f-7a8b-4c9d-0e1f-2a3b4c5d6e7f",
      "outputName": "rankedModels",
      "displayName": "Ranked Structures",
      "outputDescription": "Ranked predicted structure PDB files",
      "outputType": "FILE_LIST"
    },
    {
      "outputId": "4d5e6f7a-8b9c-4d0e-1f2a-3b4c5d6e7f8a",
      "outputName": "confidenceScores",
      "displayName": "Confidence Scores",
      "outputDescription": "Per-residue confidence (pLDDT) scores for the top-ranked model",
      "outputType": "FILE"
    }
  ]
}
```

**Validation errors — `400 Bad Request`**

Returned when e.g. `templateName` is blank, an `inputType`/`outputType` is missing or unrecognized, or two inputs share the same `inputName`.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "templateName", "message": "Template name cannot be blank" },
    { "field": "inputs[0].inputType", "message": "Input type cannot be null" }
  ]
}
```

## Batch Deployments

A batch deployment binds an application template to a Slurm cluster: the run script section and the resource request (walltime, nodes, GPUs, ...) a run of it starts from. Which identity a job submits under is a property of the run, not of the deployment, so it is named on the process instead. Deployments reference a template by id (see [Create Application Template](#create-application-template)).

### Create Batch Deployment

```
POST /api/v1/slurm-deployments
```

Requires `ADMIN` or `SUPER_ADMIN` authority.

**curl example**

Uses `$TEMPLATE_ID` from [Create Application Template](#create-application-template) and `$CLUSTER_ID` from [Create Cluster](#create-cluster) above.

```bash
TOKEN='<the token printed at startup>'

curl -s -X POST localhost:9095/api/v1/slurm-deployments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": "'"$TEMPLATE_ID"'",
    "slurmClusterId": "'"$CLUSTER_ID"'",
    "slurmRunSection": "module load alphafold\nrun_alphafold.sh --fasta_paths=$fastaFile --model_preset=$modelPreset",
    "defaultBatchJobConfig": {
      "wallTimeMinutes": 720,
      "allocation": "TG-BIO210001",
      "cpus": 8,
      "mem": "64G",
      "nodes": 1,
      "ntasks": 1,
      "gres": "gpu:1",
      "gpus": 1
    },
    "defaultPartition": "gpu"
  }'
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `templateId` | string | required, must reference an existing application template |
| `slurmClusterId` | string \| null | optional; if supplied, must reference an existing cluster — a typo is an error, not a silent unbind |
| `slurmRunSection` | string | required, cannot be blank |
| `defaultBatchJobConfig` | object | required — the resource request a run of this deployment starts from |
| `defaultBatchJobConfig.wallTimeMinutes` | integer | required, must be positive |
| `defaultBatchJobConfig.allocation` | string | required, cannot be blank |
| `defaultBatchJobConfig.cpus` | integer \| null | optional |
| `defaultBatchJobConfig.mem` | string \| null | optional |
| `defaultBatchJobConfig.memPerCpu` | string \| null | optional |
| `defaultBatchJobConfig.ntasksPerNode` | integer \| null | optional |
| `defaultBatchJobConfig.cpusPerTask` | integer \| null | optional |
| `defaultBatchJobConfig.nodes` | integer \| null | optional |
| `defaultBatchJobConfig.ntasks` | integer \| null | optional |
| `defaultBatchJobConfig.gres` | string \| null | optional |
| `defaultBatchJobConfig.gpus` | integer \| null | optional |
| `defaultBatchJobConfig.memPerGpu` | string \| null | optional |
| `defaultBatchJobConfig.cpusPerGpu` | string \| null | optional |
| `defaultBatchJobConfig.gpusPerNode` | integer \| null | optional |
| `defaultBatchJobConfig.constraints` | string \| null | optional |
| `defaultPartition` | string \| null | optional |

```json
{
  "templateId": "b6f1c2de-3a4b-4c5d-8e6f-7a8b9c0d1e2f",
  "slurmClusterId": "c1d2e3f4-5678-4abc-9def-0123456789ab",
  "slurmRunSection": "module load alphafold\nrun_alphafold.sh --fasta_paths=$fastaFile --model_preset=$modelPreset",
  "defaultBatchJobConfig": {
    "wallTimeMinutes": 720,
    "allocation": "TG-BIO210001",
    "cpus": 8,
    "mem": "64G",
    "nodes": 1,
    "ntasks": 1,
    "gres": "gpu:1",
    "gpus": 1
  },
  "defaultPartition": "gpu"
}
```

**Response — `201 Created`**

`deploymentId` and `defaultBatchJobConfig.batchJobConfigId` are server-generated (UUIDs).

```json
{
  "deploymentId": "e5f6a7b8-9cde-4f01-8234-56789abcdef0",
  "templateId": "b6f1c2de-3a4b-4c5d-8e6f-7a8b9c0d1e2f",
  "slurmClusterId": "c1d2e3f4-5678-4abc-9def-0123456789ab",
  "slurmRunSection": "module load alphafold\nrun_alphafold.sh --fasta_paths=$fastaFile --model_preset=$modelPreset",
  "defaultBatchJobConfig": {
    "batchJobConfigId": "f6a7b8c9-de01-4f23-9456-789abcdef012",
    "wallTimeMinutes": 720,
    "allocation": "TG-BIO210001",
    "cpus": 8,
    "mem": "64G",
    "memPerCpu": null,
    "ntasksPerNode": null,
    "cpusPerTask": null,
    "nodes": 1,
    "ntasks": 1,
    "gres": "gpu:1",
    "gpus": 1,
    "memPerGpu": null,
    "cpusPerGpu": null,
    "gpusPerNode": null,
    "constraints": null
  },
  "defaultPartition": "gpu"
}
```

**Validation errors — `400 Bad Request`**

Returned when e.g. `templateId` or `slurmRunSection` is blank, `defaultBatchJobConfig` is missing, or `defaultBatchJobConfig.wallTimeMinutes`/`allocation` fail their checks. A `templateId` or `slurmClusterId` that does not resolve to an existing record is returned as `404 Not Found` instead.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "templateId", "message": "Template id cannot be blank" },
    { "field": "defaultBatchJobConfig.wallTimeMinutes", "message": "Wall time must be positive" }
  ]
}
```


## Processes

A process is one run. It is the only resource in this part of the API: everything a run
carries hangs off it, and nothing that hangs off it is addressable on its own.

What a run needs beyond an owner and a type depends on what kind of run it is, and that
is carried in a **section** of the process body rather than in a resource of its own. A
`BATCH_JOB` carries a `batchProcess` section — the deployment being run, the credential
it submits under, the resources this run asks for, and the values it supplies for the
deployment template's declared inputs and outputs. Those `inputMappings` and `outputMappings` sit inside the section
rather than beside it, since the declarations they name come from the deployment's
template, which only a `BATCH_JOB` has. A process carrying no `batchProcess` therefore
has nowhere to carry mappings at all.

So there is no `POST /api/v1/batch-processes`, and no batch process id to address: a
batch process is created with its process, read back nested inside it, corrected by
updating the process, and deleted when the process is deleted.

```
GET    /api/v1/processes                 # every process — admin only
GET    /api/v1/processes?deploymentId=…  # the runs of one deployment
POST   /api/v1/processes
GET    /api/v1/processes/{processId}
PUT    /api/v1/processes/{processId}
DELETE /api/v1/processes/{processId}
```

**Authorization.** Submitting is self-service: any authenticated caller may create a
process, and the owner is taken from the token rather than the body. The unfiltered
listing, `PUT` and `DELETE` require `ADMIN` or `SUPER_ADMIN`. Reading one process, and
listing the runs of a deployment, carry no authorization at all — carried over from the
Java service, which did the same.

### Register the Run's Data Products

A run does not carry paths for its file-typed inputs and outputs: it carries
[data products](#data-products). Launching resolves each one into a data staging task —
an input product is copied onto the cluster before the job runs, an output product is
where the job's result is copied to afterwards — which is why a bare path would not do:
a transfer needs the host, the storage and the credential to reach them under, and a
product is what holds all three together.

So the three file declarations of the [AlphaFold2 template](#create-application-template)
need a registered product each before a run can name them: `fastaFile` in, and
`rankedModels` and `confidenceScores` out. They do not all live in the same place. The
FASTA sequence sits on a lab data host the group already stages sequences through, while
the two outputs are written by the job itself and stay on the cluster, so their products
are registered against the cluster's own storage.

**The FASTA host.** A dataset on another host means a second [SSH endpoint](#create-ssh-endpoint)
and a [credential binding](#create-ssh-endpoint-credential) on it — the binding is what
carries the host, so the cluster binding from earlier cannot be reused here. The SSH
credential itself is reused: `$SSH_CREDENTIAL_ID` is a username and key, binding it to a
second endpoint is what gives the caller standing on that host, and registering the
storage under it is what says the sequences are reached as that account. A product's
credential must name the SSH credential its storage stages under, which is what makes
`$FASTA_CREDENTIAL_ID` and `$FASTA_STORAGE_ID` go together below.

```bash
FASTA_ENDPOINT_ID=$(curl -s -X POST localhost:9095/api/v1/ssh-endpoints \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "lab-data",
    "hostName": "data.lab.example.edu",
    "port": 22
  }' | jq -r '.sshEndpointId')

FASTA_CREDENTIAL_ID=$(curl -s -X POST localhost:9095/api/v1/ssh-endpoint-credentials \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sshEndpointId": "'"$FASTA_ENDPOINT_ID"'",
    "sshCredentialId": "'"$SSH_CREDENTIAL_ID"'"
  }' | jq -r '.sshEndpointCredentialId')

FASTA_STORAGE_ID=$(curl -s -X POST localhost:9095/api/v1/scp-data-storages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dataName": "lab-sequences",
    "sshCredentialId": "'"$SSH_CREDENTIAL_ID"'"
  }' | jq -r '.dataId')
```

**The cluster.** The outputs land on the login host the cluster is reached through, which
is the storage registered in [Create SCP Data Storage](#create-scp-data-storage) —
`expanse-scratch`, staged under `$SSH_CREDENTIAL_ID` — and they are reached under the same
binding the run submits with:

```bash
CLUSTER_STORAGE_ID="$STORAGE_ID"
```

**The three products.** Each is a [data product](#create-data-product) on one of those two
storages. `fastaFile` and `confidenceScores` are single files; `rankedModels` is the
template's one `FILE_LIST` declaration, so its product is the directory the ranked
structures are collected into rather than any one PDB file — hence `"isFile": false`.

```bash
FASTA_DATA_ID=$(curl -s -X POST localhost:9095/api/v1/data-products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dataName": "T1050-sequence",
    "dataDescription": "Target protein sequence in FASTA format",
    "isFile": true,
    "path": "/data/sequences/T1050.fasta",
    "dataStorageId": "'"$FASTA_STORAGE_ID"'",
    "credentialId": "'"$FASTA_CREDENTIAL_ID"'"
  }' | jq -r '.dataId')

RANKED_MODELS_DATA_ID=$(curl -s -X POST localhost:9095/api/v1/data-products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dataName": "alphafold-run-1-ranked-models",
    "dataDescription": "Ranked predicted structure PDB files for run 1",
    "isFile": false,
    "path": "/scratch/airavata/alphafold/results/run-1/ranked_models",
    "dataStorageId": "'"$CLUSTER_STORAGE_ID"'",
    "credentialId": "'"$ENDPOINT_CREDENTIAL_ID"'"
  }' | jq -r '.dataId')

CONFIDENCE_SCORES_DATA_ID=$(curl -s -X POST localhost:9095/api/v1/data-products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dataName": "alphafold-run-1-confidence-scores",
    "dataDescription": "Per-residue pLDDT scores for the top-ranked model of run 1",
    "isFile": true,
    "path": "/scratch/airavata/alphafold/results/run-1/confidence_scores.json",
    "dataStorageId": "'"$CLUSTER_STORAGE_ID"'",
    "credentialId": "'"$ENDPOINT_CREDENTIAL_ID"'"
  }' | jq -r '.dataId')
```

| Declaration | Type | Product | Storage | Registered path |
|---|---|---|---|---|
| `fastaFile` (input) | `FILE` | `$FASTA_DATA_ID` | `lab-sequences` on `data.lab.example.edu` | `/data/sequences/T1050.fasta` |
| `rankedModels` (output) | `FILE_LIST` | `$RANKED_MODELS_DATA_ID` | `expanse-scratch` on the cluster's login host | `/scratch/airavata/alphafold/results/run-1/ranked_models` |
| `confidenceScores` (output) | `FILE` | `$CONFIDENCE_SCORES_DATA_ID` | `expanse-scratch` on the cluster's login host | `/scratch/airavata/alphafold/results/run-1/confidence_scores.json` |

Note that launching currently builds staging tasks for the single-`FILE` declarations
only: the `FILE_LIST` and `DIRECTORY` branches are stubs, so `rankedModels` is registered
and carried by the run but not yet staged off the cluster by it.

Registering a product records where a dataset is or will be; it does not touch the host,
and an output product's path need not exist yet. The three `dataId` values are what the
process below carries as its file mapping values — the run's `baseWorkDir` still decides
where the job actually reads and writes during execution, and the input product is staged
into it while the output products are staged out of it.

### Create Process

```
POST /api/v1/processes
```

Uses `$DEPLOYMENT_ID` from [Create Batch Deployment](#create-batch-deployment),
`$ENDPOINT_CREDENTIAL_ID` from [Create SSH Endpoint Credential](#create-ssh-endpoint-credential),
the `inputId`/`outputId` values returned by
[Create Application Template](#create-application-template), and the three `dataId`
values from [Register the Run's Data Products](#register-the-runs-data-products) above.
The declaration ids can be read back off the template rather than kept from its creation:

```bash
TEMPLATE=$(curl -s localhost:9095/api/v1/application-templates/"$TEMPLATE_ID")

FASTA_INPUT_ID=$(echo "$TEMPLATE" | jq -r '.inputs[] | select(.inputName=="fastaFile") | .inputId')
PRESET_INPUT_ID=$(echo "$TEMPLATE" | jq -r '.inputs[] | select(.inputName=="modelPreset") | .inputId')
RANKED_MODELS_OUTPUT_ID=$(echo "$TEMPLATE" | jq -r '.outputs[] | select(.outputName=="rankedModels") | .outputId')
CONFIDENCE_SCORES_OUTPUT_ID=$(echo "$TEMPLATE" | jq -r '.outputs[] | select(.outputName=="confidenceScores") | .outputId')
```

```bash
curl -s -X POST localhost:9095/api/v1/processes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "processType": "BATCH_JOB",
    "batchProcess": {
      "deploymentId": "'"$DEPLOYMENT_ID"'",
      "submissionCredentialId": "'"$ENDPOINT_CREDENTIAL_ID"'",
      "jobName": "alphafold-run-1",
      "baseWorkDir": "/scratch/$USER/alphafold",
      "batchJobConfig": {
        "wallTimeMinutes": 120,
        "allocation": "MY-ALLOC",
        "nodes": 1,
        "gpus": 2
      },
      "inputMappings": [
        { "templateInputId": "'"$FASTA_INPUT_ID"'", "value": "'"$FASTA_DATA_ID"'" },
        { "templateInputId": "'"$PRESET_INPUT_ID"'", "value": "{\"value\": \"monomer\"}" }
      ],
      "outputMappings": [
        { "templateOutputId": "'"$RANKED_MODELS_OUTPUT_ID"'", "value": "'"$RANKED_MODELS_DATA_ID"'" },
        { "templateOutputId": "'"$CONFIDENCE_SCORES_OUTPUT_ID"'", "value": "'"$CONFIDENCE_SCORES_DATA_ID"'" }
      ]
    }
  }'
```

| Field | Type | Notes |
|---|---|---|
| `processType` | string | required, one of `BATCH_JOB`, `CLOUD_JOB` |
| `batchProcess` | object \| null | required when `processType` is `BATCH_JOB`, rejected otherwise |

There is no `userId`: ownership comes from the token.

**`batchProcess`**

| Field | Type | Notes |
|---|---|---|
| `deploymentId` | string | required, cannot be blank |
| `batchJobConfig` | object | required — the same shape a deployment's `defaultBatchJobConfig` carries |
| `submissionCredentialId` | string | required, cannot be blank; the SSH endpoint credential binding this run submits under |
| `jobName` | string \| null | optional |
| `jobId` | string \| null | optional. Writable rather than server-generated: it is the scheduler's identifier for the submitted job, learned at submission time and recorded afterwards |
| `baseWorkDir` | string \| null | optional; the parent directory on the cluster this run works under. Launching a run that omits it fails, since there is nowhere to stage its files |
| `inputMappings` | array | optional; replaced wholesale by a `PUT` |
| `outputMappings` | array | optional; replaced wholesale by a `PUT` |

The resource request is carried here rather than copied from the deployment's
`defaultBatchJobConfig`, which is what lets a caller ask for different resources for a
particular run. Each run owns its own `batchJobConfig` row, distinct from the
deployment's, and deleting the process deletes it.

`baseWorkDir` is carried here for the same reason, and used the same way: the run works
in a subdirectory of it named for its `processId`, which is where the template's file
inputs are staged to and its file outputs staged from. It replaces the deployment's
former `workDir`, so two runs of one deployment can work under different directories.

`submissionCredentialId` is the one field of a self-service submission that names an
identity to act under, so it is authorized against the caller: a binding that does not
exist is `404`, and one that is neither theirs nor shared with them is `403`. A
deployment carries no credential to fall back on, which is why the field is required
rather than optional. A `PUT` re-resolves it the same way, so a body that omits it is
rejected rather than keeping what the run was created with.

**`batchProcess.inputMappings` / `batchProcess.outputMappings`**

| Field | Type | Notes |
|---|---|---|
| `templateInputId` / `templateOutputId` | string | required, cannot be blank. Names a declaration on the deployment's template |
| `value` | string \| null | for a `STRING`, `INTEGER`, `FLOAT` or `BOOLEAN` declaration, a JSON document: `{"value": "…"}` for a single value, `{"values": [...]}` for a list. For a `FILE`, `FILE_LIST` or `DIRECTORY` declaration, the `dataId` of a registered [data product](#register-the-runs-data-products) |
| `templateInputMappingId` / `templateOutputMappingId` | string | echoed on reads, ignored on writes |

Which of those two a mapping carries follows from the type of the declaration it names,
not from the mapping itself: nothing on the wire distinguishes them, and the value is
stored as given either way. A file mapping is resolved when the run is launched, so a
`dataId` that names no product fails there rather than at submission time.

A `PUT` replaces each set wholesale rather than merging into it, the same way a
template's declarations are replaced — the mapping ids are not part of the request, so
there is nothing to match an incoming mapping to an existing row by. Omitting a set
empties it. Validation errors are reported under the section that carries them, as
`batchProcess.inputMappings[0].templateInputId`.

Deleting the process deletes the batch section, and the mappings with it.

**Response — `201 Created`**

```json
{
  "processId": "a1b2c3d4-e5f6-4708-9a1b-2c3d4e5f6a7b",
  "userId": "cilogon:sub:1234",
  "processType": "BATCH_JOB",
  "lastStatusId": "9f8e7d6c-5b4a-4392-8172-6a5b4c3d2e1f",
  "batchProcess": {
    "batchProcessId": "7e6d5c4b-3a29-4187-9605-4b3a2c1d0e9f",
    "deploymentId": "c3d4e5f6-a7b8-4901-a2b3-c4d5e6f7a8b9",
    "submissionCredentialId": "5f4e3d2c-1b0a-4998-8776-6a5b4c3d2e1f",
    "jobId": null,
    "jobName": "alphafold-run-1",
    "baseWorkDir": "/scratch/$USER/alphafold",
    "batchJobConfig": {
      "batchJobConfigId": "20e56e93-cf74-4834-a7b3-877df2663257",
      "wallTimeMinutes": 120,
      "allocation": "MY-ALLOC",
      "nodes": 1,
      "gpus": 2
    },
    "inputMappings": [
      {
        "templateInputMappingId": "1a2b3c4d-5e6f-4708-8192-a3b4c5d6e7f8",
        "templateInputId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
        "value": "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d"
      },
      {
        "templateInputMappingId": "2b3c4d5e-6f70-4819-92a3-b4c5d6e7f8a9",
        "templateInputId": "2b3c4d5e-6f7a-4b8c-9d0e-1f2a3b4c5d6e",
        "value": "{\"value\": \"monomer\"}"
      }
    ],
    "outputMappings": [
      {
        "templateOutputMappingId": "3c4d5e6f-7081-492a-b3c4-d5e6f7a8b9c0",
        "templateOutputId": "3c4d5e6f-7a8b-4c9d-0e1f-2a3b4c5d6e7f",
        "value": "8b9c0d1e-2f3a-4b4c-9d5e-6f7a8b9c0d1e"
      },
      {
        "templateOutputMappingId": "4d5e6f70-8192-4a3b-84c5-d6e7f8a9b0c1",
        "templateOutputId": "4d5e6f7a-8b9c-4d0e-1f2a-3b4c5d6e7f8a",
        "value": "9c0d1e2f-3a4b-4c5d-8e6f-7a8b9c0d1e2f"
      }
    ]
  }
}
```

Submitting also records the run's first status, `CREATED`, in the same transaction — so
a caller never observes a process that exists but has no status history yet. See
[Process Statuses](#process-statuses).

### Read, Update and Delete Processes

```
GET    /api/v1/processes/{processId}
PUT    /api/v1/processes/{processId}
DELETE /api/v1/processes/{processId}
```

`PUT` is an administrative correction — of the deployment, the resources the run asked
for, or its template mappings. It takes the same body as `POST`. Two things are
deliberately immutable:

- **the owner**, because re-deriving it from the caller's token would reassign the
  process to whichever admin issued the request;
- **`processType`**, because which sections a process carries follows from it. A
  `BATCH_JOB` turned into something else would strand its batch process with no way to
  reach it, so a changed type is rejected with `409 Conflict`.

An update mutates the `batchJobConfig` the run already owns rather than replacing it: a
new row on every update would leave the previous one orphaned with nothing pointing at
it.

`DELETE` removes the process and everything it owns — the batch process and its resource
request, the status history, the tasks and the mappings.

**Validation errors — `400 Bad Request`**

Returned when `processType` is missing or unrecognised, when a `BATCH_JOB` carries no
`batchProcess`, when a `batchProcess` is sent for any other kind of process, or when a
section fails its own constraints. A `deploymentId` or `templateInputId` that does not
resolve to an existing record is returned as `404 Not Found` instead.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "batchProcess", "message": "Batch process cannot be null for a BATCH_JOB process" },
    { "field": "batchProcess.batchJobConfig.wallTimeMinutes", "message": "Wall time must be positive" }
  ]
}
```

## Process Statuses

A run's status history, read-only and nested under the process:

```
GET /api/v1/processes/{processId}/statuses
GET /api/v1/processes/{processId}/statuses/{statusId}
```

There is deliberately no `POST` or `PUT` — both return `405 Method Not Allowed`.
Statuses are recorded internally, never accepted from a client request body: `CREATED`
when the process is submitted, and later transitions by whatever submits and monitors
the actual job. A client polls the listing for progress.

Statuses come back oldest first. The newest one is also cached on the process itself as
`lastStatusId`.

```json
[
  {
    "processStatusId": "9f8e7d6c-5b4a-4392-8172-6a5b4c3d2e1f",
    "processId": "a1b2c3d4-e5f6-4708-9a1b-2c3d4e5f6a7b",
    "status": "CREATED",
    "log": null,
    "timestamp": 1755780000000
  }
]
```

`status` is one of `CREATED`, `SUBMITTED`, `RUNNING`, `COMPLETED`, `FAILED`.

A `statusId` from one process is not reachable through another process's path: the
mismatch is a `404`, not another run's status.

## Process Tasks

A [process](#processes) is carried out as an ordered list of tasks. There are four kinds, each its own collection under the process:

| Collection | Carries |
|---|---|
| `data-staging-tasks` | a source and destination storage, credential and path — a transfer |
| `job-submission-tasks` | the scheduler job id the submission produced |
| `job-monitoring-tasks` | the job id being watched |
| `interactive-command-tasks` | a command to run on the remote host, and its output |

All four share the same five routes, the same shape of body, and the same rules:

```
GET    /api/v1/processes/{processId}/{collection}
POST   /api/v1/processes/{processId}/{collection}
GET    /api/v1/processes/{processId}/{collection}/{taskId}
PUT    /api/v1/processes/{processId}/{collection}/{taskId}
DELETE /api/v1/processes/{processId}/{collection}/{taskId}
```

**These endpoints are owner-scoped**, unlike the process and status reads above: the owner of the process and platform admins may use them, everyone else gets `403 Forbidden`. Tasks carry filesystem paths and shell commands, so there is no reason to expose them more widely than the run they belong to.

`processId` is not accepted in a body — it comes from the path, so a task cannot be moved to another run by editing it. Nor is there a `processType`: a task names its [process](#processes) through a real foreign key, so there is nothing to discriminate, and any kind of process may carry any kind of task.

**Fields shared by every kind**

| Field | Type | Notes |
|---|---|---|
| `onFailure` | string \| null | optional, one of `RETRY`, `SKIP`, `EXIT` |
| `retryCount` | integer \| null | optional, cannot be negative |
| `taskOrder` | integer \| null | optional, cannot be negative — see ordering below |

Listings come back in execution order: lower `taskOrder` first, tasks sharing an order run in parallel, and tasks with no order sort last rather than ahead of an explicitly ordered step.

### Create Data Staging Task

```
POST /api/v1/processes/{processId}/data-staging-tasks
```

```bash
curl -s -X POST localhost:9095/api/v1/processes/"$PROCESS_ID"/data-staging-tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceDataStorageId": "'"$STORAGE_ID"'",
    "sourceCredentialId": "'"$ENDPOINT_CREDENTIAL_ID"'",
    "sourceDataStorageType": "SCP",
    "sourcePath": "/scratch/alphafold/run-1/input.fasta",
    "destinationPath": "/scratch/alphafold/run-1/staged/",
    "onFailure": "RETRY",
    "retryCount": 3,
    "taskOrder": 1
  }'
```

| Field | Type | Notes |
|---|---|---|
| `sourcePath` | string | required, cannot be blank. A single path, or a JSON array of paths |
| `destinationPath` | string | required, cannot be blank |
| `sourceDataStorageId` / `destinationDataStorageId` | string \| null | optional |
| `sourceCredentialId` / `destinationCredentialId` | string \| null | optional |
| `sourceDataStorageType` / `destinationDataStorageType` | string \| null | optional, one of `SCP`, `S3` |

**Response — `201 Created`**

```json
{
  "taskId": "b4c5d6e7-f809-4a1b-8c2d-3e4f5a6b7c8d",
  "processId": "a1b2c3d4-e5f6-4708-9a1b-2c3d4e5f6a7b",
  "sourceDataStorageId": "d1e2f3a4-5b6c-4d7e-8f90-1a2b3c4d5e6f",
  "sourceCredentialId": "0a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d",
  "sourceDataStorageType": "SCP",
  "destinationDataStorageId": null,
  "destinationCredentialId": null,
  "destinationDataStorageType": null,
  "sourcePath": "/scratch/alphafold/run-1/input.fasta",
  "destinationPath": "/scratch/alphafold/run-1/staged/",
  "onFailure": "RETRY",
  "retryCount": 3,
  "taskOrder": 1
}
```

### Create Job Submission and Job Monitoring Tasks

```
POST /api/v1/processes/{processId}/job-submission-tasks
POST /api/v1/processes/{processId}/job-monitoring-tasks
```

Both take the shared fields plus an optional `jobId`. It is writable rather than server-generated: it is the scheduler's identifier for the submitted job, learned at submission time and recorded here afterwards with a `PUT`.

```bash
curl -s -X PUT localhost:9095/api/v1/processes/"$PROCESS_ID"/job-submission-tasks/"$TASK_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "jobId": "4821577", "onFailure": "EXIT" }'
```

### Create Interactive Command Task

```
POST /api/v1/processes/{processId}/interactive-command-tasks
```

Runs a command on the remote host — filtering the output of a running job, for instance.

| Field | Type | Notes |
|---|---|---|
| `command` | string | required, cannot be blank |
| `output` | string \| null | optional; the result, recorded once whatever ran the command knows it |

```json
{
  "taskId": "c5d6e7f8-091a-4b2c-8d3e-4f5a6b7c8d9e",
  "processId": "a1b2c3d4-e5f6-4708-9a1b-2c3d4e5f6a7b",
  "command": "squeue -j 4821577 -o %T",
  "output": "RUNNING",
  "onFailure": "SKIP",
  "retryCount": null,
  "taskOrder": 4
}
```

**Errors (all four kinds)**

| Status | Cause |
|---|---|
| `400 Bad Request` | a required field blank, an unrecognised `onFailure` or storage type, or a negative `retryCount`/`taskOrder` |
| `401 Unauthorized` | no token — these routes are never anonymous |
| `403 Forbidden` | the process is not the caller's |
| `404 Not Found` | no such process, or a `taskId` that belongs to a different process |

## SCP Data Storages

An SCP data storage is a location that datasets are staged through. It points at an [SSH credential](#create-ssh-credential) — the account its data is reached as — and belongs to whoever registered it. It names no host of its own: the host comes from the [endpoint credential](#create-ssh-endpoint-credential) a transfer runs under. A [cluster](#clusters) names one as the filesystem its runs work in, and a [data product](#data-products) names one as where its dataset lives.

Registering one is documented up front, under [Create SCP Data Storage](#create-scp-data-storage), since a cluster cannot be created without one. What follows here is the rest of the resource.

| Standing | May do |
|---|---|
| `READ` share | read the storage, and register data products on it |
| `WRITE` share | the above, and edit the storage |
| Owner (or admin) | the above, and delete it, and manage its shares |

`WRITE` implies `READ`, control is not reachable through a share, and where several shares reach the same caller the strongest applies — the same rules as [data products](#data-products) and [SSH endpoint credentials](#share-an-ssh-endpoint-credential). A group share applies only while the member's group membership is `ACTIVE`.

Anyone else gets `403 Forbidden` on every read, including the unfiltered listing: `GET /api/v1/scp-data-storages` is admin-only, `/me` returns what the caller owns, and `/shared-with-me` what has been shared with them.

```
GET    /api/v1/scp-data-storages                                   (admin)
GET    /api/v1/scp-data-storages/me
GET    /api/v1/scp-data-storages/shared-with-me
POST   /api/v1/scp-data-storages
GET    /api/v1/scp-data-storages/{dataStorageId}                   (READ)
PUT    /api/v1/scp-data-storages/{dataStorageId}                   (WRITE)
DELETE /api/v1/scp-data-storages/{dataStorageId}                   (owner)

GET    /api/v1/scp-data-storages/{dataStorageId}/group-shares      (owner)
POST   /api/v1/scp-data-storages/{dataStorageId}/group-shares      (owner)
PUT    /api/v1/scp-data-storages/{dataStorageId}/group-shares/{sharingId}
DELETE /api/v1/scp-data-storages/{dataStorageId}/group-shares/{sharingId}

GET    /api/v1/scp-data-storages/{dataStorageId}/user-shares       (owner)
POST   /api/v1/scp-data-storages/{dataStorageId}/user-shares       (owner)
PUT    /api/v1/scp-data-storages/{dataStorageId}/user-shares/{sharingId}
DELETE /api/v1/scp-data-storages/{dataStorageId}/user-shares/{sharingId}
```

### Share an SCP Data Storage

```
POST /api/v1/scp-data-storages/{dataStorageId}/user-shares
POST /api/v1/scp-data-storages/{dataStorageId}/group-shares
```

Bodies and errors match [Share a Data Product](#share-a-data-product) below: `userId`/`groupId` plus an optional `permission` defaulting to `READ`, restricted to the storage's owner and platform admins.

```bash
curl -s -X POST localhost:9095/api/v1/scp-data-storages/"$STORAGE_ID"/group-shares \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "groupId": "'"$GROUP_ID"'", "permission": "READ" }'
```

## Data Products

A data product is a registered dataset: a path on an [SCP data storage](#scp-data-storages), owned by whoever registered it.

| Standing | May do |
|---|---|
| `READ` share | read the product, and see it under `/shared-with-me` |
| `WRITE` share | the above, and edit the product |
| Owner (or admin) | the above, and delete it, and manage its shares |

`WRITE` implies `READ`, control is not reachable through a share, and where several shares reach the same caller the strongest applies — the same rules as [SSH endpoint credentials](#share-an-ssh-endpoint-credential). A group share applies only while the member's group membership is `ACTIVE`.

Anyone else gets `403 Forbidden`, and no listing leaks a product: `GET /api/v1/data-products` is admin-only, `/me` returns what the caller owns, and `/shared-with-me` what has been shared with them.

### Create Data Product

```
POST /api/v1/data-products
```

Requires an authenticated principal with a `users` row. **The storage it names must already be reachable by the caller** — registering data into a storage nobody shared with them would be a way to have the platform touch a host they have no standing on.

[Register the Run's Data Products](#register-the-runs-data-products) works a full example through: the products a run's file inputs and outputs are named by, across two storages.

```bash
curl -s -X POST localhost:9095/api/v1/data-products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dataName": "alphafold-run-1",
    "dataDescription": "Predicted structures for run 1",
    "isFile": false,
    "path": "/scratch/alphafold/run-1",
    "dataStorageId": "'"$STORAGE_ID"'",
    "credentialId": "'"$ENDPOINT_CREDENTIAL_ID"'"
  }'
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `dataName` | string | required, cannot be blank |
| `dataDescription` | string \| null | optional |
| `isFile` | boolean | required; `false` for a directory |
| `path` | string | required, cannot be blank |
| `dataStorageId` | string | required, must reference a storage the caller can reach |
| `dataStorageType` | string \| null | optional, `SCP`; defaults to `SCP` |
| `credentialId` | string \| null | optional; the [SSH endpoint credential](#create-ssh-endpoint-credential) the data was staged under. Must be one the caller may use, and must name the SSH credential the storage stages under |

There is no `ownerId` and no `provisionStatus`: ownership comes from the token, and the lifecycle state is the server's to move. A body carrying either is accepted and ignored.

**Response — `201 Created`**

```json
{
  "dataId": "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d",
  "dataName": "alphafold-run-1",
  "dataDescription": "Predicted structures for run 1",
  "isFile": false,
  "path": "/scratch/alphafold/run-1",
  "provisionStatus": "REGISTERD",
  "ownerId": "cilogon:12345",
  "dataStorageId": "d1e2f3a4-5b6c-4d7e-8f90-1a2b3c4d5e6f",
  "dataStorageType": "SCP",
  "credentialId": "0a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d",
  "createdAt": 1755043200000,
  "permission": "WRITE"
}
```

`PUT` takes the same body and needs `WRITE` **plus** access to the storage and credential it names; the owner, provision status and creation time are never rewritten from a request.

Neither `dataStorageId` nor `credentialId` carries a foreign key — the storage id is qualified by `dataStorageType` — so both are resolved by the service: an unknown id is `404`, one the caller cannot reach is `403`, and a binding under an account other than the storage's own is `400`. The binding's *host* is not constrained: the storage names an account, not a host, so the same account may be reached on whichever endpoint the binding points at.

`DELETE` returns `204 No Content`, is refused for anyone but the owner and admins, and takes the product's shares with it.

### Share a Data Product

```
GET    /api/v1/data-products/{dataProductId}/group-shares
POST   /api/v1/data-products/{dataProductId}/group-shares
PUT    /api/v1/data-products/{dataProductId}/group-shares/{sharingId}
DELETE /api/v1/data-products/{dataProductId}/group-shares/{sharingId}

GET    /api/v1/data-products/{dataProductId}/user-shares
POST   /api/v1/data-products/{dataProductId}/user-shares
PUT    /api/v1/data-products/{dataProductId}/user-shares/{sharingId}
DELETE /api/v1/data-products/{dataProductId}/user-shares/{sharingId}
```

Every one of these — reads included — is restricted to the owner and platform admins: the share list names who holds a dataset, which is more than a grantee needs to know.

**Request body**

| Field | Type | Notes |
|---|---|---|
| `groupId` / `userId` | string | required; must reference an existing record |
| `permission` | string \| null | optional, `READ` or `WRITE`; defaults to `READ` |

`PUT` takes only `permission`, which is required there — the subject of a share is fixed at creation.

**Response — `201 Created`**

```json
{
  "dataProductUserSharingId": "2b3c4d5e-6f70-4a8b-9c0d-1e2f3a4b5c6d",
  "dataProductId": "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d",
  "userId": "cilogon:67890",
  "permission": "READ"
}
```

A group share is the same shape with `dataProductGroupSharingId` and `groupId`.

**Errors**

| Status | Cause |
|---|---|
| `400 Bad Request` | `groupId`/`userId` blank, or an unrecognised `permission` |
| `403 Forbidden` | the caller is not the owner |
| `404 Not Found` | no such product, group or user; or a `sharingId` belonging to a different product |
| `409 Conflict` | already shared with that group or user, or shared with the owner |

## Groups

A group is a named collection of users that resources can be shared with. Unlike the catalogs above, a group belongs to the user who created it rather than to the deployment: any authenticated caller may create one, and what a caller may then do with it comes from the group's own owner field and membership rows, not from platform roles.

| Standing | Who holds it | May do |
|---|---|---|
| Reader | the owner, any `ACTIVE` member, and platform admins | read the group and its membership list |
| Member manager | the owner, members holding `ADMIN` or `MODERATOR`, and platform admins | add, change and remove memberships |
| Owner | the owner and platform admins | rename and delete the group |

A caller with no standing at all gets `404 Not Found` rather than `403 Forbidden`: group names are chosen by users and may say who is working with whom, so an outsider cannot confirm that a given group id exists.

Two rules keep a group from being taken over through its own membership list. The owner's membership can only be changed by the owner (or a platform admin), so a moderator cannot suspend them out of their own group; and the owner's membership cannot be removed at all — delete the group instead.