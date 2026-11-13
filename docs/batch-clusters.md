# Batch Clusters and Partitions

A **batch cluster** is a compute resource that Airavata submits jobs to through a batch scheduler. Slurm is the supported scheduler today. An [application deployment](application.md#application-deployment) names the cluster its application runs on, and each run of that deployment is submitted there as a batch job.

Airavata describes a cluster in three parts:

| Part | What it describes |
|---|---|
| **Cluster** | The machine: its head node, optional data transfer node and where Slurm is installed |
| **Partition** | One queue on the cluster (a Slurm partition) and its limits and defaults |
| **Cluster config** | One user's login on the cluster: account, SSH key and work directory |

```
Slurm cluster  ──1:N──▶  Partition          (the machine and its layout)
      ▲
      └──── Cluster config  ◀── SSH credential   (one login on it; owned by a user, shareable)
```

Any user can register the clusters they have access to. The cluster and its partitions describe only the machine. Logins are kept separately in **cluster configs**, so a cluster record never holds credentials. A run chooses which cluster config to submit under.

The easiest way to register all of this is the **dashboard**. It logs in to the cluster and reads the partitions, and later the allocations, directly from Slurm, so you don't have to type them in. See [Registering with the dashboard](#registering-with-the-dashboard).

---

## Cluster

| Field | Required | Meaning |
|---|---|---|
| `clusterName` | yes | A name for the cluster, e.g. `hpc`. |
| `clusterDescription` | no | Free text. |
| `headnodeHost` | yes | The login host jobs are submitted through, e.g. `login.hpc.example.edu`. |
| `headnodePort` | yes | Its SSH port, usually `22`. |
| `slurmHome` | no | The Slurm install prefix, e.g. `/opt/slurm`, if `sbatch` and `squeue` are not on the login shell's `PATH`. Commands are then run as `<slurmHome>/bin/sbatch`. |
| `dataHost` | no | A separate host for moving data, typically a DTN. If it is not set, data is staged through the head node. |
| `dataPort` | no | The data host's SSH port. |

Registering a cluster needs no SSH key. The cluster only says *where* the machine is.

## Partition

A partition records one Slurm partition's limits and defaults. Clients use them to suggest sensible resource requests and to warn about requests the partition would reject.

| Field | Unit | Meaning |
|---|---|---|
| `name` | | The Slurm partition name, e.g. `gpu`. Required. A job config's `partition` refers to a partition by this name. |
| `description` | | Free text. |
| `maxRunTime` | minutes | The longest wall time a job may request. |
| `defaultWalltime` | minutes | The wall time a job gets if it asks for none. |
| `maxNodes` | | The most nodes one job may use. |
| `maxProcessors` | | The total CPUs in the partition. |
| `cpuPerNode` | | CPUs on each node. |
| `maxMemory` | MB | Memory per node. |
| `gres` | | Generic resources on the nodes, e.g. `gpu:a100:4`. |
| `nodes` | | The Slurm node list, e.g. `gpu[01-08]`. |
| `defaultNodeCount`, `defaultCpuCount` | | Suggested defaults for a run. |
| `maxJobsInQueue` | | The most jobs one user may queue. |
| `isDefaultQueue` | | Whether this is the cluster's default partition. |
| `isCheckpointable` | | Whether jobs here can be checkpointed. |

Every field except `name` is optional.

## Cluster config

A cluster config is one login on a cluster. It is what a run submits jobs as.

| Field | Required | Meaning |
|---|---|---|
| `slurmClusterId` | yes | The cluster. |
| `loginUser` | yes | The account on the head node. |
| `sshCredentialId` | yes | An [SSH credential](api.md#ssh-credentials) **you own**, whose public key is installed for `loginUser`. |
| `workRoot` | yes | The directory runs work under. Each run gets its own `<workRoot>/<processId>` subdirectory unless it sets its own `baseWorkDir`. |
| `name`, `description` | no | Labels. |

Any user can register a cluster config, and it belongs to them. Sharing one with a user or a [group](api.md#groups) lets others submit under it without seeing the key. This is how, for example, a PI can let a team charge a group allocation. A `READ` share is enough to submit runs and discover allocations. A `WRITE` share also lets the grantee edit the config. Only the owner can delete it or manage its shares. See [sharing.md](sharing.md).

---

## Registering with the dashboard

The dashboard is at `http://localhost:3000/dashboard/` when it runs locally (see [INSTALL.md](https://github.com/apache/airavata/blob/master/INSTALL.md)). Cluster screens are under **Compute clusters**.

### Before you start

Register an **SSH credential** on the **SSH credentials** page, or create one from the **New…** button inside the cluster dialog. Its public key must be installed for the account you will log in as. Discovery opens a real SSH session.

### Add a cluster and detect its partitions

Open **Compute clusters → Clusters → Add cluster**. The dialog has three steps:

1. **Cluster.** Enter the name and head node host. The head node port defaults to 22. Fill in *Slurm home* if Slurm is not on the login `PATH`, and *Data host* if the cluster has a DTN.

2. **Cluster login.** Enter a *Login user*, choose an *SSH credential* and click **Discover partitions**. Airavata logs in to the head node and runs:

   - `scontrol show partition -o` to get each partition's name, time limits, node limits, total CPUs, node list and default flag
   - `sinfo -h -a -o '%R|%c|%m|%G'` to get the per-node CPU count, memory and GPUs/GRES, which `scontrol` reports only as partition totals

   Both commands run under *Slurm home* when it is set. A successful discovery therefore also shows that job submission will find its binaries.

   If the credential is **interactive** (for example, one that needs an MFA code), a login prompt appears on the **Interactive auth** page. Answer it there. Discovery waits up to five minutes.

   Leave **Also save this login as my cluster config** checked and enter a *Work root* to save the login as your own cluster config in the same step. You won't need to enter it again later.

3. **Partitions.** The discovered partitions appear here as editable rows. Review them: fix values, remove queues you don't want users to target, or add more with **+ Add partition**. You can also skip step 2 and fill this section in by hand.

Click **Add cluster**. The cluster and all its partitions are saved together, so the cluster never appears with only some of its partitions. If the cluster saves but the cluster config doesn't, the button changes to **Retry saving cluster config**. It retries only the config, without registering the cluster again.

To see a registered cluster's head node, data host and partitions, click its row.

How discovered values map to partition fields:

| Partition field | Detected from |
|---|---|
| `maxRunTime`, `defaultWalltime` | `MaxTime`, `DefaultTime`, converted to minutes. Left empty for `UNLIMITED` / `NONE`. |
| `maxNodes` | `MaxNodes`, or `TotalNodes` when unlimited |
| `maxProcessors` | `TotalCPUs` |
| `cpuPerNode`, `maxMemory` | the largest node `sinfo` reports. `maxMemory` uses `MaxMemPerNode` if the partition sets one. |
| `gres` | all GRES on the partition's nodes, without socket bindings |
| `nodes`, `isDefaultQueue` | `Nodes`, `Default=YES` |

If `sinfo` fails, the per-node fields are left empty and discovery still succeeds. `description`, `defaultNodeCount`, `defaultCpuCount`, `maxJobsInQueue` and `isCheckpointable` are not detected. Fill them in by hand if you want them.

### Add your login to an existing cluster

Open **Compute clusters → Cluster configs → Add cluster config**, then choose the cluster, enter your login user and work root, and pick your SSH credential. Configs you own are listed under **My cluster configs**. Configs others have shared with you are under **Shared with me**, with the permission each one grants.

### Detect allocations

The account a job is charged to (`allocation`, the Slurm account) is also detected rather than typed. Wherever the dashboard asks for a resource request (when creating an application deployment, and when starting a run), the **Allocation** field has a **Discover** button. It logs in using the selected cluster config and runs:

- `sacctmgr -nP show associations where user=<loginUser> format=Account,Partition,QOS`
- `sshare -h -P -U -u <loginUser> -o Account` if `sacctmgr` is closed to ordinary users. This lists the same accounts without their limits.

Each account the login may charge appears as a button you can click to choose it. Hovering shows its description, the partitions it is limited to and its QOS. If an account is limited to partitions other than the requested partition, it is marked **other partitions** and a warning appears when you select it. If only one account is found and the field is empty, it is filled in automatically.

On the deployment form, **Discover allocations as** picks which of your logins on the cluster to ask.

---

## Registering with the API

The same steps through the API. Full request and response bodies are in [api.md](api.md#slurm-clusters).

```bash
# 1. Detect partitions (uses an SSH credential you own)
PARTITIONS=$(curl -s -X POST localhost:9095/api/v1/slurm-clusters/discover-partitions \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{
    "headnodeHost": "login.hpc.example.edu",
    "headnodePort": 22,
    "slurmHome": null,
    "loginUser": "airavata",
    "sshCredentialId": "'"$SSH_CREDENTIAL_ID"'"
  }' | jq '.partitions')

# 2. Register the cluster with those partitions
CLUSTER_ID=$(curl -s -X POST localhost:9095/api/v1/slurm-clusters \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{
    "clusterName": "hpc",
    "headnodeHost": "login.hpc.example.edu",
    "headnodePort": 22,
    "dataHost": "dtn.hpc.example.edu",
    "dataPort": 22,
    "partitions": '"$PARTITIONS"'
  }' | jq -r '.slurmClusterId')

# 3. Register your login
CLUSTER_CONFIG_ID=$(curl -s -X POST localhost:9095/api/v1/slurm-cluster-configs \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{
    "slurmClusterId": "'"$CLUSTER_ID"'",
    "loginUser": "airavata",
    "workRoot": "/scratch/airavata/jobs",
    "sshCredentialId": "'"$SSH_CREDENTIAL_ID"'"
  }' | jq -r '.slurmClusterConfigId')

# 4. Detect the allocations that login may charge (owner, or anyone the config is shared with)
curl -s -X POST localhost:9095/api/v1/slurm-cluster-configs/"$CLUSTER_CONFIG_ID"/discover-allocations \
  -H "Authorization: Bearer $TOKEN"
```

### Changing partitions later

Partitions can be included in the body only when a cluster is **created**. A `PUT` on a cluster that includes `partitions` is rejected with `400`. To change partitions afterwards, use the partition endpoints:

```
POST   /api/v1/slurm-clusters/{clusterId}/partitions                  # add one (e.g. a new GPU queue)
PUT    /api/v1/slurm-clusters/{clusterId}/partitions/{partitionId}    # change one
DELETE /api/v1/slurm-clusters/{clusterId}/partitions/{partitionId}    # remove one
```

To refresh a cluster's layout after the site changes it, run **discover-partitions** again and compare the result with the stored partitions. Then add, update or remove partitions one at a time.

Deployments refer to a partition by **name**, not by id. Deleting or renaming a partition is not checked against the deployments that use it, so update those deployments' `defaultBatchJobConfig.partition` as well.

### Deleting

- Deleting a **partition** never conflicts.
- Deleting a **cluster** removes its partitions with it. It is refused (`409 Conflict`) while any cluster config still logs in to the cluster. The owners of those configs have to delete them first.
- Deleting a **cluster config** is limited to its owner (or an admin) and removes its shares with it.

---

## Errors you may see during discovery

| Status | Usually means |
|---|---|
| `403 Forbidden` | The SSH credential used for partition discovery belongs to someone else. For allocation discovery, the config isn't yours and hasn't been shared with you. |
| `409 Conflict` | `scontrol` returned no partitions, or the config is missing its cluster or credential. |
| `502 Bad Gateway` | The login or Slurm command failed. The message says why: the key was refused, the host could not be reached, or Slurm was not found (set *Slurm home*). |

---

## How runs use this

When a run is launched, Airavata logs in to the cluster's head node under the run's **cluster config**. It creates the work directory under the config's work root, stages the run's data into it, writes the batch script and submits it with `sbatch` to the run's partition. The `#SBATCH` header is built from the run's resource request, including its allocation. See [application.md](application.md) for how deployments and runs are put together.
