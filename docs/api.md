# Airavata HTTP API

Base URL: `http://localhost:9095` (default `SERVER_PORT` is `9095`; override via the `SERVER_PORT` env var).

All request/response bodies are JSON (`Content-Type: application/json`). Writes require an `Authorization: Bearer <token>` header for a principal with `ADMIN` or `SUPER_ADMIN` authority; catalog reads (`GET`) are open without a token. [Groups](#groups) are the exception on both counts — they are owned by ordinary users, so any authenticated caller may create one and none of them are readable anonymously. See INSTALL.md for how to obtain the root token.

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

## Clusters

A cluster is a Slurm-managed HPC resource that batch deployments submit jobs to.

### Create Cluster

```
POST /api/v1/clusters
```

Requires `ADMIN` or `SUPER_ADMIN` authority.

**curl example**

The example below captures `clusterId` from the response into `$CLUSTER_ID` (requires `jq`), so it can be reused when creating a batch deployment further down.

```bash
TOKEN='<the token printed at startup>'

CLUSTER_ID=$(curl -s -X POST localhost:9095/api/v1/clusters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clusterName": "expanse",
    "clusterDescription": "SDSC Expanse HPC cluster",
    "hostName": "login.expanse.sdsc.edu",
    "slurmHome": "/usr/bin"
  }' | jq -r '.clusterId')

echo "$CLUSTER_ID"
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `clusterName` | string | required, cannot be blank |
| `clusterDescription` | string \| null | optional |
| `hostName` | string | required, cannot be blank |
| `slurmHome` | string | required, cannot be blank |

```json
{
  "clusterName": "expanse",
  "clusterDescription": "SDSC Expanse HPC cluster",
  "hostName": "login.expanse.sdsc.edu",
  "slurmHome": "/usr/bin"
}
```

**Response — `201 Created`**

`clusterId` is server-generated (UUID). `partitions` is empty until partitions are added via `/api/v1/clusters/{clusterId}/partitions`.

```json
{
  "clusterId": "c1d2e3f4-5678-4abc-9def-0123456789ab",
  "clusterName": "expanse",
  "clusterDescription": "SDSC Expanse HPC cluster",
  "hostName": "login.expanse.sdsc.edu",
  "slurmHome": "/usr/bin",
  "partitions": []
}
```

**Validation errors — `400 Bad Request`**

Returned when `clusterName`, `hostName` or `slurmHome` is blank.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "clusterName", "message": "Cluster name cannot be blank" },
    { "field": "hostName", "message": "Host name cannot be blank" }
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

Requires `ADMIN` or `SUPER_ADMIN` authority. Pairs a login username with an SSH key, producing the id that [Create Cluster Credential](#create-cluster-credential) below binds to a cluster.

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

### Create Cluster Credential

```
POST /api/v1/cluster-credentials
```

Binds an SSH credential to a cluster so the caller can submit jobs there. Requires only an authenticated principal — not admin — but that principal must have a matching row in `users` (see INSTALL.md's "Owning resources requires a matching `users` row"), since the binding's owner is resolved from the token rather than accepted as a request field.

The resulting binding id is what [Create Batch Deployment](#create-batch-deployment) below uses as `defaultSubmissionCredentialId`: a deployment submits under a specific cluster credential, not a bare SSH credential, since the binding is what ties the submitting identity to both a cluster and an owner.

**curl example**

Uses `$CLUSTER_ID` from [Create Cluster](#create-cluster) and `$SSH_CREDENTIAL_ID` from [Create SSH Credential](#create-ssh-credential) above, and captures `clusterCredentialId` into `$CLUSTER_CREDENTIAL_ID` for the batch deployment step.

```bash
CLUSTER_CREDENTIAL_ID=$(curl -s -X POST localhost:9095/api/v1/cluster-credentials \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clusterId": "'"$CLUSTER_ID"'",
    "sshCredentialId": "'"$SSH_CREDENTIAL_ID"'"
  }' | jq -r '.clusterCredentialId')

echo "$CLUSTER_CREDENTIAL_ID"
```

**Request body**

| Field | Type | Notes |
|---|---|---|
| `clusterId` | string | required, must reference an existing cluster |
| `sshCredentialId` | string | required, must reference an existing SSH credential |

```json
{
  "clusterId": "c1d2e3f4-5678-4abc-9def-0123456789ab",
  "sshCredentialId": "7f8e9d0c-1b2a-4c3d-8e4f-5a6b7c8d9e0f"
}
```

**Response — `201 Created`**

`clusterCredentialId` is server-generated (UUID); `userId` is the owner resolved from the caller's token, not a request field.

```json
{
  "clusterCredentialId": "0a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d",
  "clusterId": "c1d2e3f4-5678-4abc-9def-0123456789ab",
  "sshCredentialId": "7f8e9d0c-1b2a-4c3d-8e4f-5a6b7c8d9e0f",
  "userId": "root"
}
```

**Validation errors**

- `400 Bad Request` when `clusterId` or `sshCredentialId` is blank.
- `404 Not Found` when the caller has no matching `users` row, or `clusterId`/`sshCredentialId` does not resolve to an existing record.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "clusterId", "message": "Cluster id cannot be blank" },
    { "field": "sshCredentialId", "message": "SSH credential id cannot be blank" }
  ]
}
```

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

Uses `$TEMPLATE_ID` from [Create Application Template](#create-application-template), `$CLUSTER_ID` from [Create Cluster](#create-cluster), and `$CLUSTER_CREDENTIAL_ID` from [Create Cluster Credential](#create-cluster-credential) above.

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
    "defaultSubmissionCredentialId": "'"$CLUSTER_CREDENTIAL_ID"'",
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
| `defaultSubmissionCredentialId` | string | required, must reference an existing cluster credential binding (see [Create Cluster Credential](#create-cluster-credential)), not a bare SSH credential |
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
