---
slug: /
sidebar_label: Introduction
---

# Introduction

**Apache Airavata** runs research applications on remote compute resources such as HPC clusters on your behalf. You describe an application once, point it at the clusters and data you already have access to, and Airavata does the rest of each run: it copies inputs to the cluster, writes and submits the batch job, watches it until it finishes, and copies the results back.

Airavata is designed around how research computing actually works:

- **You bring your own resources.** Clusters, logins, allocations and data stores are registered by the people who use them. Airavata doesn't depend on a central catalogue or a shared service account.
- **Everything belongs to someone and can be shared.** Credentials stay private to their owner. Cluster logins, data and datasets can be shared with individual collaborators or groups, with read or write access.
- **Automation works with multi-factor logins.** Modern HPC systems ask for a password, OTP or Duo push. Airavata pauses the login, asks you, and then carries on with the automated run.
- **Metadata is detected, not typed in.** Partitions, node limits, GPUs and Slurm allocations are read from the cluster itself.

---

## How the pieces fit together

```
                         ┌──────────────────────── Application ────────────────────────┐
                         │                                                              │
  Application template ──┤  inputs / outputs     ──▶  Application deployment  ──▶  Process (one run)
  (what goes in and out) │                           (how it runs on a cluster)     values, data products,
                         └──────────────────────────────────────────────────────────────┘  resources, login
                                                                │                               │
                                                                ▼                               ▼
  SSH credential ──▶ Cluster config ──▶ Batch cluster + partitions               Data product ──▶ Data storage
  (your key;          (your login,       (the machine and its queues)            (a file or       (host + login
   optionally MFA)     work directory)                                            directory)       to reach it)
                                                                                       │
                                                                                       ▼
                                                                                 Virtual dataset
                                                                                 (folders of products)
```

| Concept | In one line | Read more |
|---|---|---|
| **Application** | A program and its typed inputs and outputs (template), how it runs on a cluster (deployment), and individual runs (processes). | [Applications](application.md) |
| **Data product** | A registered file or directory on a storage host. Runs read their inputs from data products and write their outputs to them. | [Data products](data-product.md) |
| **Virtual dataset** | A folder tree that arranges data products from different hosts into one browsable, shareable layout. | [Virtual data](virtual-data.md) |
| **Batch cluster** | A Slurm cluster, its partitions and your login on it, with partitions and allocations detected automatically. | [Batch clusters and partitions](batch-clusters.md) |
| **Interactive authentication** | Answering MFA prompts for logins that need a person, without breaking automation. | [Interactive authentication](interactive-auth.md) |
| **Authentication and authorization** | CILogon sign-in, the bootstrap root account, roles and ownership. | [Authentication and authorization](auth.md) |
| **Sharing** | Granting users and groups `READ` or `WRITE` access to what you own. | [Sharing](sharing.md) |

---

## What a run looks like

1. **Sign in** with CILogon, through the dashboard or a token ([Authentication](auth.md)).
2. **Register an SSH credential** for the cluster. Mark it interactive if the cluster asks for MFA ([Interactive authentication](interactive-auth.md)).
3. **Add the cluster** and let Airavata detect its partitions, then save your login on it as a cluster config ([Batch clusters](batch-clusters.md)).
4. **Register your data.** Add a data storage for each host, then a data product for each input file or directory and for each place results should go ([Data products](data-product.md)).
5. **Describe the application** as a template with typed inputs and outputs, then **deploy** it on the cluster with a run section and default resources ([Applications](application.md)).
6. **Launch a run.** Choose the deployment and your cluster config, map each input to a value or data product, and map each output to a destination product. Airavata stages the data in, submits the job, monitors it and stages the results out. If a step fails, for example because an MFA prompt went unanswered, **retry** the run and it continues from the step that failed.

---

## Ways to use Airavata

| Interface | Best for |
|---|---|
| **Dashboard** | Day-to-day use in a browser: registering clusters with automatic detection, answering MFA prompts as they appear, launching and following runs. |
| **HTTP API** | Scripts, portals and integrations. Every feature is available as a REST endpoint. See the [API reference](api.md). |
| **airavata-mcp** | Working through an AI assistant, which can turn an existing `sbatch` script into an application, plan runs and explain failures. See [airavata-mcp](mcp.md). |

---

## Installing

To build and run the server, set up its database and start the dashboard, see [INSTALL.md](https://github.com/apache/airavata/blob/master/INSTALL.md). For a production deployment, configure CILogon and disable the root account as described in [Authentication and authorization](auth.md#production-checklist).
