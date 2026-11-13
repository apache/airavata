<p align="center">
  <img src="https://github.com/user-attachments/assets/6fac06b0-e31c-4f16-9b91-1a9c4926d7fd" alt="Apache Airavata" />
</p>

<p align="center">
  <a href="https://github.com/apache/airavata/actions/workflows/ci.yml"><img src="https://github.com/apache/airavata/actions/workflows/ci.yml/badge.svg?branch=master" alt="CI"></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License"></a>
  <a href="https://github.com/apache/airavata/graphs/contributors"><img src="https://img.shields.io/github/contributors/apache/airavata.svg" alt="Contributors"></a>
</p>

---

## Overview

Apache Airavata is an all-in-one platform for executing and managing computational jobs on distributed computing resources, including local clusters, supercomputers, national grids, and academic and commercial clouds.

Airavata is also a foundational layer for building higher-level tools on top of distributed resources, such as:

- **Inference engines** that serve models on whatever compute you have access to.
- **Heterogeneous training workflows** that span different clusters, schedulers and hardware.
- **AI automation tools** that safely integrate with your resources, acting only with the permissions of the signed-in user.
- **Unified access to multi-endpoint datasets**, so data spread across many hosts appears as one browsable, shareable layout.

Airavata is built around concepts to streamline research computing works in practice:

- **Federated compute and data.** Clusters, supercomputers, clouds and storage hosts are used together as one pool, without users having to deal with the access, scheduling and data-movement details of each system.
- **Focus on the domain, not the plumbing.** Researchers describe what they want to run and which data it needs. Airavata handles the orchestration of compute and storage (staging data, writing and submitting batch jobs, monitoring, and retrieving results), so time goes to the science rather than to learning each resource's quirks.
- **Bring your own resources.** Users register their own clusters, logins, allocations and data stores.
- **Everything has an owner and can be shared.** Credentials stay private to their owner. Cluster logins, data and datasets can be shared with individuals or groups, with read or write access.
- **Automation that works with MFA.** When a login needs a password, OTP or Duo push, Airavata pauses, asks you, and then continues the automated run.
- **Detected, not typed in.** Partitions, node limits, GPUs and Slurm allocations are read from the cluster itself.

## Key concepts

See the [introduction](docs/intro.md)

## Ways to use Airavata

| Interface | Best for |
|---|---|
| **Dashboard** | Day-to-day use in a browser: registering clusters, answering MFA prompts, launching and following runs. |
| **HTTP API** | Scripts, portals and integrations. Every feature is a REST endpoint ([API reference](docs/api.md), [OpenAPI spec](docs/openapi.yaml)). |
| **Jupyter** | Running notebooks on the compute node of a running job, with its files and kernels, from the dashboard ([docs](docs/notebooks.md)). |
| **Local VS Code** | Working from your own VS Code against the same running job, with the editor and tooling you already use. |
| **airavata-mcp** | Working through an AI assistant (Claude, Copilot, Cursor and others) that can turn an `sbatch` script into an application, plan runs and explain failures ([docs](docs/mcp.md)). |

Jupyter and local VS Code give you one familiar interface to your work, whatever kind of compute resource it runs on and whatever the network between you and it allows. Jobs dial out to Airavata over a single outbound connection, so compute nodes need no inbound ports and nothing extra runs on the login node. Whether the job is on an HPC cluster behind a firewall or on a cloud VM, you open it the same way.

## Quick start

Prerequisites: Go 1.25+, PostgreSQL 14+ (Docker Compose is the easiest way to run it locally), and optionally Node.js 20.9+ to build the dashboard.

```bash
# 1. Start PostgreSQL (host port 15432)
docker compose -f dev-tools/compose/compose.yml up -d postgres

# 2. Build the server
go build -o bin/airavata-server ./cmd/airavata-server

# 3. Build the dashboard (optional)
npm --prefix dashboard ci && npm --prefix dashboard run build

# 4. Run
./bin/airavata-server
```

On first start the server creates its schema and prints a **root account token**. Copy it, since it is how you make the first authenticated call.

```bash
curl -s localhost:9095/health
# {"status":"UP"}
```

Then open <http://localhost:9095/dashboard/> and sign in with the token.

The defaults suit local development. For configuration options, cross-compiling and production setup, see [INSTALL.md](INSTALL.md). Before a production deployment, configure CILogon and disable the root account as described in [Authentication and authorization](docs/auth.md).

## Repository layout

| Path | Contents |
|---|---|
| [`cmd/airavata-server`](cmd/airavata-server) | The API server (a single Go binary). |
| [`cmd/airavata-mcp`](cmd/airavata-mcp) | The Model Context Protocol server, a thin client of the REST API. |
| [`api/`](api) | HTTP API handlers, grouped by domain (application, compute, credentials, data, iam, process, sharing, audit). |
| [`internal/`](internal) | Core implementation: auth, orchestration, SSH, database, permissions, configuration and more. |
| [`dashboard/`](dashboard) | The web UI (Next.js), served by the API server under `/dashboard/`. |
| [`docs/`](docs) | User and API documentation. |
| [`website/`](website) | The Docusaurus site that renders `docs/` ([airavata.apache.org](https://airavata.apache.org)). |
| [`dev-tools/`](dev-tools) | Docker Compose files and development clients. |

## Documentation

- [Introduction](docs/intro.md)
- [Applications](docs/application.md)
- [Batch clusters and partitions](docs/batch-clusters.md)
- [Data products](docs/data-product.md) and [virtual data](docs/virtual-data.md)
- [Interactive authentication](docs/interactive-auth.md)
- [Authentication and authorization](docs/auth.md) and [sharing](docs/sharing.md)
- [Notebooks](docs/notebooks.md) and [audit](docs/audit.md)
- [API reference](docs/api.md) and [airavata-mcp](docs/mcp.md)

## Development

```bash
go build ./...                       # build everything
go test ./...                        # run the tests
go vet ./...                         # static checks
gofmt -l .                           # formatting (no output means clean)
go generate ./internal/mcp/client/   # regenerate the MCP client after editing docs/openapi.yaml
```

CI runs formatting, generated-code, vet, build and race-enabled test checks on every pull request. Please make sure these pass locally before opening one.

## Contributing

Contributions are welcome. To get started:

1. Fork the repository and create a branch from `master`.
2. Make your change, with tests where it makes sense.
3. Run the checks listed under [Development](#development).
4. Open a pull request describing what changed and why.

Apache Airavata is an Apache Software Foundation project, so contributors are expected to follow the [ASF Code of Conduct](https://www.apache.org/foundation/policies/conduct.html). To report a bug or request a feature, open an [issue](https://github.com/apache/airavata/issues).

## License

Apache Airavata is licensed under the [Apache License, Version 2.0](LICENSE).

Apache Airavata, Airavata, Apache, and the Apache feather logo are trademarks of The Apache Software Foundation.
