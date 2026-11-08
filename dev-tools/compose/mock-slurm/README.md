<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Mock SSH & Slurm Cluster for Local Development

A lightweight SSH and Slurm cluster service for Docker Compose that enables the complete process launch lifecycle, cluster partition discovery, and allocation discovery locally without requiring a multi-node HPC cluster.

---

## Features

- **OpenSSH Server**: Listens on port `22` in container (`12222` on host).
- **Authentication**:
  - Public key authentication with pre-seeded dev key: `keys/id_ed25519`
  - Password authentication: user `slurmuser`, password `slurmuser`
  - Keyboard-interactive authentication supported.
- **Mock Slurm CLI Tools**:
  - `sbatch`: Validates `#SBATCH` directives, allocates auto-incrementing Job IDs, runs submitted scripts in the background, redirects stdout/stderr as requested by the script directives, and returns `Submitted batch job <id>`.
  - `squeue`: Reports live running/completed jobs, and supports formatting flags (e.g. `squeue -j <id> -o %T`).
  - `scancel`: Cancels running jobs and terminates their background processes.
  - `scontrol`: Supports `scontrol show partition -o` (used by Airavata partition discovery).
  - `sinfo`: Supports `sinfo -h -a -o '%R|%c|%m|%G'` (used by Airavata partition discovery).
  - `sacctmgr`: Supports `sacctmgr -nP show associations ...` and `show account ...` (used by Airavata allocation discovery).
  - `sshare`: Supports `sshare -h -P -U -u <user> -o Account`.
  - `sacct`: Supports job accounting queries.
- **Dual Path Availability**: Installed in `/usr/local/bin` and symlinked in `/opt/slurm/bin` and `/opt/slurm` so Airavata works with or without `slurmHome` configured on the cluster record.

---

## Quick Start

### 1. Start Services

From the repository root:

```bash
docker compose -f dev-tools/compose/compose.yml up -d
```

This brings up:
- PostgreSQL on `localhost:15432`
- Adminer on `localhost:18080`
- Mock SSH/Slurm cluster on `localhost:12222`

Verify SSH access with the dev key:

```bash
ssh -i dev-tools/compose/mock-slurm/keys/id_ed25519 -p 12222 -o StrictHostKeyChecking=no slurmuser@localhost "scontrol show partition -o"
```

---

## Connection & Credential Details

| Setting | Value |
|---|---|
| **Host** | `localhost` (from host) or `mock-slurm` (inside Docker network) |
| **Port** | `12222` (mapped from `22`) |
| **Username** | `slurmuser` |
| **Password** | `slurmuser` |
| **Private Key** | `dev-tools/compose/mock-slurm/keys/id_ed25519` |
| **Public Key** | `dev-tools/compose/mock-slurm/keys/id_ed25519.pub` |
| **Work / Scratch Root** | `/home/slurmuser/scratch` |

> [!NOTE]
> The private key in `keys/id_ed25519` must have `0600` permissions:
> `chmod 600 dev-tools/compose/mock-slurm/keys/id_ed25519`

---

## Testing the Process Launch Lifecycle

You can run the automated end-to-end verification script:

```bash
./dev-tools/compose/mock-slurm/demo-launch.sh
```

Or step through the API using curl:

### 1. Register SSH Credential

```bash
TOKEN="<root-token-from-server-startup>"
PRIVATE_KEY=$(cat dev-tools/compose/mock-slurm/keys/id_ed25519)

CRED_RESP=$(curl -s -X POST localhost:9095/api/v1/ssh-credentials \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sshCredentialName": "mock-slurm-key",
    "privateKey": "'"$(echo "$PRIVATE_KEY" | awk '{printf "%s\\n", $0}')"'"
  }')

CRED_ID=$(echo "$CRED_RESP" | jq -r .sshCredentialId)
```

### 2. Register Slurm Cluster & Discover Partitions

```bash
CLUSTER_RESP=$(curl -s -X POST localhost:9095/api/v1/slurm-clusters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clusterName": "local-mock-cluster",
    "headnodeHost": "localhost",
    "headnodePort": 12222
  }')

CLUSTER_ID=$(echo "$CLUSTER_RESP" | jq -r .slurmClusterId)

# Discover partitions directly from the cluster over SSH:
curl -s -X POST localhost:9095/api/v1/slurm-clusters/discover-partitions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "headnodeHost": "localhost",
    "headnodePort": 12222,
    "loginUser": "slurmuser",
    "sshCredentialId": "'"$CRED_ID"'"
  }'

# Register the default partition:
curl -s -X POST "localhost:9095/api/v1/slurm-clusters/$CLUSTER_ID/partitions" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "debug",
    "isDefaultQueue": true,
    "maxRunTime": 60,
    "maxNodes": 1,
    "maxProcessors": 4,
    "cpuPerNode": 4
  }'
```

### 3. Register Cluster Config & Discover Allocations

```bash
CONFIG_RESP=$(curl -s -X POST localhost:9095/api/v1/slurm-cluster-configs \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "slurmClusterId": "'"$CLUSTER_ID"'",
    "loginUser": "slurmuser",
    "workRoot": "/home/slurmuser/scratch",
    "sshCredentialId": "'"$CRED_ID"'"
  }')

CONFIG_ID=$(echo "$CONFIG_RESP" | jq -r .slurmClusterConfigId)

# Discover allocations over SSH:
curl -s -X POST "localhost:9095/api/v1/slurm-cluster-configs/$CONFIG_ID/discover-allocations" \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Register Template & Deployment

```bash
TMPL_RESP=$(curl -s -X POST localhost:9095/api/v1/application-templates \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "templateName": "Echo App",
    "templateDescription": "Demonstration echo application"
  }')

TMPL_ID=$(echo "$TMPL_RESP" | jq -r .templateId)

DEP_RESP=$(curl -s -X POST localhost:9095/api/v1/slurm-deployments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": "'"$TMPL_ID"'",
    "slurmClusterId": "'"$CLUSTER_ID"'",
    "defaultPartition": "debug",
    "slurmRunSection": "echo \"Hello from Airavata process {{ process_id }}!\" && echo \"computation output\" > output.txt",
    "defaultBatchJobConfig": {
      "wallTimeMinutes": 10,
      "nodes": 1,
      "cpus": 1,
      "allocation": "default"
    }
  }')

DEP_ID=$(echo "$DEP_RESP" | jq -r .deploymentId)
```

### 5. Create & Launch Process

```bash
PROC_RESP=$(curl -s -X POST localhost:9095/api/v1/processes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "processType": "BATCH_JOB",
    "batchProcess": {
      "deploymentId": "'"$DEP_ID"'",
      "slurmClusterConfigId": "'"$CONFIG_ID"'",
      "baseWorkDir": "/home/slurmuser/scratch",
      "batchJobConfig": {
        "wallTimeMinutes": 10,
        "nodes": 1,
        "cpus": 1,
        "allocation": "default"
      }
    }
  }')

PROC_ID=$(echo "$PROC_RESP" | jq -r .processId)

# Launch the process:
curl -s -X POST "localhost:9095/api/v1/processes/$PROC_ID/launch" \
  -H "Authorization: Bearer $TOKEN"
```

Upon launching:
1. `LaunchProcess` plans input staging, job submission, and job monitoring tasks.
2. The submission workflow connects via SSH to `localhost:12222`.
3. Working directory `/home/slurmuser/scratch/<process-id>` is created.
4. `script.slurm` is uploaded to the remote directory via SCP.
5. `sbatch` is executed via SSH, returning `Submitted batch job <job-id>`.
6. Batch job status is recorded as `SUBMITTED` with the assigned job ID.
7. The script executes locally inside the container, producing `stdout.txt`, `stderr.txt`, and `output.txt`!
