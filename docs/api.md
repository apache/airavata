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

`fieldErrors` is omitted on every other kind of failure. Field names are the JSON paths of the request body, including indexes and nesting — `inputs[0].inputType`, `batchJobConfig.wallTimeMinutes`.

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

A cluster is a Slurm-managed HPC resource that batch deployments submit jobs to. It is reached through an [SSH endpoint](#ssh-endpoints), named by id.

### Create Cluster

```
POST /api/v1/clusters
```

Requires `ADMIN` or `SUPER_ADMIN` authority.

**curl example**

Uses `$SSH_ENDPOINT_ID` from [Create SSH Endpoint](#create-ssh-endpoint) above, and captures `clusterId` into `$CLUSTER_ID` (requires `jq`) for the batch deployment step further down.

```bash
CLUSTER_ID=$(curl -s -X POST localhost:9095/api/v1/clusters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clusterName": "expanse",
    "clusterDescription": "SDSC Expanse HPC cluster",
    "sshEndpointId": "'"$SSH_ENDPOINT_ID"'",
    "slurmHome": "/usr/bin"
  }' | jq -r '.clusterId')

echo "$CLUSTER_ID"
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `clusterName` | string | required, cannot be blank |
| `clusterDescription` | string \| null | optional |
| `sshEndpointId` | string | required, must reference an existing SSH endpoint |
| `slurmHome` | string | required, cannot be blank |

```json
{
  "clusterName": "expanse",
  "clusterDescription": "SDSC Expanse HPC cluster",
  "sshEndpointId": "3f2a1b0c-9d8e-4f7a-8b6c-5d4e3f2a1b0c",
  "slurmHome": "/usr/bin"
}
```

**Response — `201 Created`**

`clusterId` is server-generated (UUID). The endpoint is inlined, since every caller that wants a cluster wants the host it lives on. `partitions` is empty until partitions are added via `/api/v1/clusters/{clusterId}/partitions`.

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
  "slurmHome": "/usr/bin",
  "partitions": []
}
```

**Validation errors — `400 Bad Request`**

Returned when `clusterName`, `sshEndpointId` or `slurmHome` is blank. An `sshEndpointId` that does not resolve to an existing endpoint is returned as `404 Not Found` instead.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "clusterName", "message": "Cluster name cannot be blank" },
    { "field": "sshEndpointId", "message": "SSH endpoint id cannot be blank" }
  ]
}
```

### Create Cluster Partition

```
POST /api/v1/clusters/{clusterId}/partitions
```

Requires `ADMIN` or `SUPER_ADMIN` authority.

**curl example**

Uses `$CLUSTER_ID` from [Create Cluster](#create-cluster) above.

```bash
curl -s -X POST localhost:9095/api/v1/clusters/"$CLUSTER_ID"/partitions \
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
  }'
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

Requires `ADMIN` or `SUPER_ADMIN` authority. Pairs a login username with an SSH key, producing the id that [Create SSH Endpoint Credential](#create-ssh-endpoint-credential) below binds to a host.

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

The resulting binding id is what [Create Batch Deployment](#create-batch-deployment) below uses as `defaultSubmissionCredentialId`: a deployment submits under a specific endpoint credential, not a bare SSH credential, since the binding is what ties the submitting identity to both a host and an owner.

**curl example**

Uses `$SSH_ENDPOINT_ID` from [Create SSH Endpoint](#create-ssh-endpoint) and `$SSH_CREDENTIAL_ID` from [Create SSH Credential](#create-ssh-credential) above, and captures `sshEndpointCredentialId` into `$ENDPOINT_CREDENTIAL_ID` for the batch deployment step.

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

A batch deployment binds an application template to a Slurm cluster: the run script section, resource request (walltime, nodes, GPUs, ...) and the credential used to submit jobs. Deployments reference a template by id (see [Create Application Template](#create-application-template)).

### Create Batch Deployment

```
POST /api/v1/slurm-deployments
```

Requires `ADMIN` or `SUPER_ADMIN` authority.

**curl example**

Uses `$TEMPLATE_ID` from [Create Application Template](#create-application-template), `$CLUSTER_ID` from [Create Cluster](#create-cluster), and `$ENDPOINT_CREDENTIAL_ID` from [Create SSH Endpoint Credential](#create-ssh-endpoint-credential) above.

```bash
TOKEN='<the token printed at startup>'

curl -s -X POST localhost:9095/api/v1/slurm-deployments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": "'"$TEMPLATE_ID"'",
    "slurmClusterId": "'"$CLUSTER_ID"'",
    "slurmRunSection": "module load alphafold\nrun_alphafold.sh --fasta_paths=$fastaFile --model_preset=$modelPreset",
    "batchJobConfig": {
      "wallTimeMinutes": 720,
      "allocation": "TG-BIO210001",
      "cpus": 8,
      "mem": "64G",
      "nodes": 1,
      "ntasks": 1,
      "gres": "gpu:1",
      "gpus": 1
    },
    "defaultSubmissionCredentialId": "'"$ENDPOINT_CREDENTIAL_ID"'",
    "workDir": "/scratch/$USER/alphafold",
    "partition": "gpu"
  }'
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `templateId` | string | required, must reference an existing application template |
| `slurmClusterId` | string \| null | optional; if supplied, must reference an existing cluster — a typo is an error, not a silent unbind |
| `slurmRunSection` | string | required, cannot be blank |
| `batchJobConfig` | object | required |
| `batchJobConfig.wallTimeMinutes` | integer | required, must be positive |
| `batchJobConfig.allocation` | string | required, cannot be blank |
| `batchJobConfig.cpus` | integer \| null | optional |
| `batchJobConfig.mem` | string \| null | optional |
| `batchJobConfig.memPerCpu` | string \| null | optional |
| `batchJobConfig.ntasksPerNode` | integer \| null | optional |
| `batchJobConfig.cpusPerTask` | integer \| null | optional |
| `batchJobConfig.nodes` | integer \| null | optional |
| `batchJobConfig.ntasks` | integer \| null | optional |
| `batchJobConfig.gres` | string \| null | optional |
| `batchJobConfig.gpus` | integer \| null | optional |
| `batchJobConfig.memPerGpu` | string \| null | optional |
| `batchJobConfig.cpusPerGpu` | string \| null | optional |
| `batchJobConfig.gpusPerNode` | integer \| null | optional |
| `batchJobConfig.constraints` | string \| null | optional |
| `defaultSubmissionCredentialId` | string | required, must reference an existing SSH endpoint credential binding (see [Create SSH Endpoint Credential](#create-ssh-endpoint-credential)), not a bare SSH credential |
| `workDir` | string \| null | optional |
| `partition` | string \| null | optional |

```json
{
  "templateId": "b6f1c2de-3a4b-4c5d-8e6f-7a8b9c0d1e2f",
  "slurmClusterId": "c1d2e3f4-5678-4abc-9def-0123456789ab",
  "slurmRunSection": "module load alphafold\nrun_alphafold.sh --fasta_paths=$fastaFile --model_preset=$modelPreset",
  "batchJobConfig": {
    "wallTimeMinutes": 720,
    "allocation": "TG-BIO210001",
    "cpus": 8,
    "mem": "64G",
    "nodes": 1,
    "ntasks": 1,
    "gres": "gpu:1",
    "gpus": 1
  },
  "defaultSubmissionCredentialId": "0a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d",
  "workDir": "/scratch/$USER/alphafold",
  "partition": "gpu"
}
```

**Response — `201 Created`**

`deploymentId` and `batchJobConfig.batchJobConfigId` are server-generated (UUIDs).

```json
{
  "deploymentId": "e5f6a7b8-9cde-4f01-8234-56789abcdef0",
  "templateId": "b6f1c2de-3a4b-4c5d-8e6f-7a8b9c0d1e2f",
  "slurmClusterId": "c1d2e3f4-5678-4abc-9def-0123456789ab",
  "slurmRunSection": "module load alphafold\nrun_alphafold.sh --fasta_paths=$fastaFile --model_preset=$modelPreset",
  "batchJobConfig": {
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
  "defaultSubmissionCredentialId": "0a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d",
  "workDir": "/scratch/$USER/alphafold",
  "partition": "gpu"
}
```

**Validation errors — `400 Bad Request`**

Returned when e.g. `templateId`, `slurmRunSection` or `defaultSubmissionCredentialId` is blank, `batchJobConfig` is missing, or `batchJobConfig.wallTimeMinutes`/`allocation` fail their checks. A `templateId`, `slurmClusterId` or `defaultSubmissionCredentialId` that does not resolve to an existing record is returned as `404 Not Found` instead.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "templateId", "message": "Template id cannot be blank" },
    { "field": "batchJobConfig.wallTimeMinutes", "message": "Wall time must be positive" }
  ]
}
```


## Processes

A process is one run. It is the only resource in this part of the API: everything a run
carries hangs off it, and nothing that hangs off it is addressable on its own.

What a run needs beyond an owner and a type depends on what kind of run it is, and that
is carried in a **section** of the process body rather than in a resource of its own. A
`BATCH_JOB` carries a `batchProcess` section — the deployment being run and the
resources this run asks for. Every process may carry `inputMappings` and
`outputMappings`, the values it supplies for its template's declared inputs and
outputs.

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

### Create Process

```
POST /api/v1/processes
```

Uses `$DEPLOYMENT_ID` from [Create Batch Deployment](#create-batch-deployment), and the
`inputId`/`outputId` values returned by
[Create Application Template](#create-application-template).

```bash
curl -s -X POST localhost:9095/api/v1/processes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "processType": "BATCH_JOB",
    "batchProcess": {
      "deploymentId": "'"$DEPLOYMENT_ID"'",
      "jobName": "alphafold-run-1",
      "batchJobConfig": {
        "wallTimeMinutes": 120,
        "allocation": "MY-ALLOC",
        "nodes": 1,
        "gpus": 2
      }
    },
    "inputMappings": [
      { "templateInputId": "'"$INPUT_ID"'", "value": "{\"value\": \"/scratch/input.fasta\"}" }
    ],
    "outputMappings": [
      { "templateOutputId": "'"$OUTPUT_ID"'", "value": "{\"value\": \"/scratch/out/\"}" }
    ]
  }'
```

| Field | Type | Notes |
|---|---|---|
| `processType` | string | required, one of `BATCH_JOB`, `CLOUD_JOB` |
| `batchProcess` | object \| null | required when `processType` is `BATCH_JOB`, rejected otherwise |
| `inputMappings` | array | optional; replaced wholesale by a `PUT` |
| `outputMappings` | array | optional; replaced wholesale by a `PUT` |

There is no `userId`: ownership comes from the token.

**`batchProcess`**

| Field | Type | Notes |
|---|---|---|
| `deploymentId` | string | required, cannot be blank |
| `batchJobConfig` | object | required — the same shape a deployment carries |
| `jobName` | string \| null | optional |
| `jobId` | string \| null | optional. Writable rather than server-generated: it is the scheduler's identifier for the submitted job, learned at submission time and recorded afterwards |

The resource request is carried here rather than copied from the deployment's default,
which is what lets a caller ask for different resources for a particular run. Each run
owns its own `batchJobConfig` row, distinct from the deployment's, and deleting the
process deletes it.

**`inputMappings` / `outputMappings`**

| Field | Type | Notes |
|---|---|---|
| `templateInputId` / `templateOutputId` | string | required, cannot be blank. Names a declaration on the deployment's template |
| `value` | string \| null | a JSON document: `{"value": "…"}` for a single value, `{"values": [...]}` for a list |
| `templateInputMappingId` / `templateOutputMappingId` | string | echoed on reads, ignored on writes |

A `PUT` replaces each set wholesale rather than merging into it, the same way a
template's declarations are replaced — the mapping ids are not part of the request, so
there is nothing to match an incoming mapping to an existing row by. Omitting a set
empties it.

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
    "jobId": null,
    "jobName": "alphafold-run-1",
    "batchJobConfig": {
      "batchJobConfigId": "20e56e93-cf74-4834-a7b3-877df2663257",
      "wallTimeMinutes": 120,
      "allocation": "MY-ALLOC",
      "nodes": 1,
      "gpus": 2
    }
  },
  "inputMappings": [
    {
      "templateInputMappingId": "1a2b3c4d-5e6f-4708-8192-a3b4c5d6e7f8",
      "templateInputId": "d1e2f3a4-5b6c-4d7e-8f90-1a2b3c4d5e6f",
      "value": "{\"value\": \"/scratch/input.fasta\"}"
    }
  ],
  "outputMappings": []
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

An SCP data storage is a host and location that datasets are staged through. It points at an [SSH endpoint](#ssh-endpoints) and belongs to whoever registered it.

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

### Create SCP Data Storage

```
POST /api/v1/scp-data-storages
```

Requires an authenticated principal with a `users` row. The owner is taken from the token — there is no owner field in the body — and is not transferable afterwards. Uses `$SSH_ENDPOINT_ID` from [Create SSH Endpoint](#create-ssh-endpoint).

```bash
STORAGE_ID=$(curl -s -X POST localhost:9095/api/v1/scp-data-storages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dataName": "expanse-scratch",
    "sshEndpointId": "'"$SSH_ENDPOINT_ID"'"
  }' | jq -r '.dataId')
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `dataName` | string | required, cannot be blank |
| `sshEndpointId` | string | required, must reference an existing SSH endpoint |

**Response — `201 Created`**

The endpoint is inlined, for the same reason a cluster inlines it. `permission` is what the calling principal may do with the storage.

```json
{
  "dataId": "d1e2f3a4-5b6c-4d7e-8f90-1a2b3c4d5e6f",
  "dataName": "expanse-scratch",
  "ownerId": "cilogon:12345",
  "sshEndpointId": "3f2a1b0c-9d8e-4f7a-8b6c-5d4e3f2a1b0c",
  "sshEndpoint": {
    "sshEndpointId": "3f2a1b0c-9d8e-4f7a-8b6c-5d4e3f2a1b0c",
    "name": "expanse-login",
    "hostName": "login.expanse.sdsc.edu",
    "port": 22
  },
  "permission": "WRITE"
}
```

`DELETE` returns `204 No Content`, or `409 Conflict` when data products are still registered on the storage. Deleting one takes its shares with it.

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
| `credentialId` | string \| null | optional; the [SSH endpoint credential](#create-ssh-endpoint-credential) the data was staged under. Must be one the caller may use, and must be for the same host the storage stages through |

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

Neither `dataStorageId` nor `credentialId` carries a foreign key — the storage id is qualified by `dataStorageType` — so both are resolved by the service: an unknown id is `404`, one the caller cannot reach is `403`, and a credential for the wrong host is `400`.

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
