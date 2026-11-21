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

# Mock SSH & Slurm cluster for local development

A single container running OpenSSH and shell stand-ins for the Slurm commands
airavata-server calls, so partition/allocation discovery and job launch work locally
without a real cluster. Development only: jobs run as plain background processes
inside the container.

| Setting | Value |
|---|---|
| Host / port | `localhost:12222` from the host, `mock-slurm:22` inside the compose network |
| Login user | `slurmuser` (public key only; no passwords, no root login) |
| Key pair | `keys/id_ed25519`, generated locally by `bootstrap.sh` and gitignored |
| Work root | `/home/slurmuser/scratch` |

Mocked commands: `sbatch`, `squeue`, `scancel`, `scontrol show partition`, `sinfo`,
`sacctmgr show associations|account`, `sshare`, `sacct`. They are installed on `PATH`
and under `/opt/slurm/bin`, so a cluster works with or without `slurmHome`.

## Usage

From the repository root (the container only runs with the `slurm` profile):

```bash
docker compose -f dev-tools/compose/compose.yml --profile slurm up -d postgres mock-slurm
```

```bash
AIRAVATA_ROOT_ACCOUNT_TOKEN=dev-token ./bin/airavata-server
```

```bash
AIRAVATA_TOKEN=dev-token dev-tools/compose/mock-slurm/bootstrap.sh
```

`bootstrap.sh` generates the key pair on first run, checks SSH, then uses the public
REST API to register, and print the ids of:

- an SSH credential for the key (named by its fingerprint);
- a cluster `mock-slurm` with the partitions the server discovers over SSH (`debug`,
  and `compute` with 4 nodes and `gpu:1`), plus a cluster config for `slurmuser`;
- an SCP data storage on the container, and a data product `mock-slurm-message` at
  `/home/slurmuser/data/message.txt` whose content it uploads through the API;
- a template `mock-slurm-echo` with a `FILE` input `message.txt` and a `FILE` output
  `greeting.txt`, and its deployment.

Re-running is safe: records are found by name and reused, and a regenerated key gets a
new credential that the config and storage are switched to. Add `--launch` to also
run the template: the server stages `message.txt` in over SCP, submits the job, and
the script waits for `greeting.txt` (tracked in place) to appear.

| Variable | Default | Meaning |
|---|---|---|
| `AIRAVATA_API_URL` | `http://localhost:9095` | Server to register with. |
| `AIRAVATA_TOKEN` | *(required)* | Bearer token with cluster and template management permissions. |
| `MOCK_SLURM_SSH_HOST` / `_PORT` | `127.0.0.1` / `12222` | Where the script reaches the container. |
| `MOCK_SLURM_CLUSTER_HOST` / `_PORT` | same as above | Where the server reaches it; `mock-slurm` / `22` if the server runs in compose. |

Jobs reach `COMPLETED` in Airavata only when the email monitor is configured
(INSTALL.md "Job monitoring — email"); without it a launched process stops at the
submitted state even though the job's output appears in the container.
