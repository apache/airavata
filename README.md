# Apache Airavata
![image](https://github.com/user-attachments/assets/6d908819-cf5e-48d0-bbf7-f031c95adf94)

[![Build Status](https://github.com/apache/airavata/actions/workflows/maven-build.yml/badge.svg)](https://github.com/apache/airavata/actions/workflows/maven-build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Contributors](https://img.shields.io/github/contributors/apache/airavata.svg)](https://github.com/apache/airavata/graphs/contributors)

Apache Airavata is a software framework for executing and managing computational jobs on distributed computing resources including local clusters, supercomputers, national grids, academic and commercial clouds. Airavata builds on general concepts of service oriented computing, distributed messaging, and workflow composition and orchestration. Airavata bundles a server package with an API, client software development Kits and a general purpose reference UI implementation.

## Key Features

- 🔧 Service-oriented architecture with distributed messaging
- 🔄 Fully-managed task lifecycle (environment setup, data staging, execution, and output retrieval)
- ☁️ Multi-cloud and hybrid cloud support
- 🖥️ Comprehensive API and SDK ecosystem
- 🌐 [Reference UI Implementations](https://github.com/apache/airavata-portals)

## Using Airavata

If you’re a researcher, Airavata offers several ways to streamline your workflows:

1. Submit Batch Jobs via the **Airavata Application Portal** -- [Example](https://admin.cybershuttle.org)
2. Launch Interactive Experiments through the **Airavata Research Portal** -- [Example](https://cybershuttle.org)
3. Explore and run published experiments from the **Airavata Research Catalog** -- [Example](https://cybershuttle.org/resources?resourceTypes=REPOSITORY)
4. Run interactive computational jobs directly from your IDE using **Airavata Python SDK** -- [PyPI](https://pypi.org/project/airavata-python-sdk/)


## 🧱 The Airavata Ecosystem

Apache Airavata is composed of modular components spanning core services, data management, user interfaces, and developer tooling.

### 🔧 Core Services
- [`airavata`](https://github.com/apache/airavata) – Main resource management and task orchestration middleware
- [`airavata-custos`](https://github.com/apache/airavata-custos) – Identity and access management framework
- [`airavata-mft`](https://github.com/apache/airavata-mft) – Managed file transfer services
- [`airavata-portals`](https://github.com/apache/airavata-portals) – All frontends for airavata

### 📦 Data & Metadata Services
- [`airavata-data-lake`](https://github.com/apache/airavata-data-lake) – Data lake and storage backend
- [`airavata-data-catalog`](https://github.com/apache/airavata-data-catalog) – Metadata and search services

### 📚 Documentation & Branding
- [`airavata-docs`](https://github.com/apache/airavata-docs) – Developer documentation
- [`airavata-user-docs`](https://github.com/apache/airavata-user-docs) – End-user guides
- [`airavata-admin-user-docs`](https://github.com/apache/airavata-admin-user-docs) – Admin-focused documentation
- [`airavata-custos-docs`](https://github.com/apache/airavata-custos-docs) – Custos documentation
- [`airavata-site`](https://github.com/apache/airavata-site) – Project website

### 🧪 Experimental & Research
- [`airavata-sandbox`](https://github.com/apache/airavata-sandbox) – Prototypes and early-stage work
- [`airavata-labs`](https://github.com/apache/airavata-labs) – Experimental projects
- [`airavata-jupyter-kernel`](https://github.com/apache/airavata-jupyter-kernel) – Jupyter integration
- [`airavata-cerebrum`](https://github.com/apache/airavata-cerebrum) – Airavata for Neuroscience


## 🔄 How Airavata Works

Airavata is composed of a consolidated JVM server plus three Spring Boot microservices that together facilitate the full lifecycle of computational jobs.

```mermaid
graph TB
    subgraph "Docker Infrastructure"
        DB[(MariaDB<br/>:13306)]
        RMQ[RabbitMQ<br/>:5672]
        ZK[ZooKeeper<br/>:2181]
        KFK[Kafka<br/>:9092]
        KC[Keycloak<br/>:18080]
    end

    subgraph "AiravataServer (single JVM, :8930)"
        direction TB
        MUX["TMultiplexedProcessor<br/>9 Thrift services"]
        BG["Background Services<br/>12 IServer workers"]
    end

    subgraph "Spring Boot Modules"
        AS[Agent Service<br/>:18880]
        FS[File Server<br/>:8050]
        RS[Research Service<br/>:18889]
        RP[REST Proxy<br/>:8082]
    end

    SDK[Python SDK] -->|TMultiplexedProtocol| MUX
    Portal[Web Portal] -->|REST| RP
    MUX --> DB
    MUX --> RMQ
    BG --> ZK
    BG --> KFK
    BG --> RMQ
    AS --> DB
    RS --> DB
    MUX -.->|auth| KC
```

### 1. Airavata Server `(apache-airavata-api-server)`
> Entry point: `org.apache.airavata.api.server.AiravataServer`
> Started via: `./scripts/start.sh`

`AiravataServer` is the single JVM that hosts all Thrift services and background workers. On startup it:

1. Initializes the unified `airavata` database via Flyway
2. Registers 9 Thrift services on a single `TMultiplexedProcessor` (port **8930**)
3. Starts background `IServer` workers after the Thrift server is ready

#### Architecture: Service Layer + Thrift Transport

All business logic lives in a transport-agnostic **service layer** (`org.apache.airavata.service.*`). Thrift handlers are thin adapters that delegate to services via `ThriftAdapter`, translating between Thrift types and service exceptions. This decoupling enables future REST/gRPC transports without duplicating logic.

| Service | Responsibility |
|---|---|
| `ExperimentService` | Experiment lifecycle (create, launch, clone, terminate, intermediate outputs) |
| `ProjectService` | Project CRUD and search |
| `GatewayService` | Gateway management and sharing domain setup |
| `ApplicationCatalogService` | App modules, deployments, and interfaces |
| `ResourceService` | Compute/storage resources, job submission, data movement, storage info |
| `CredentialService` | SSH/password credential management with sharing |
| `ResourceSharingService` | Resource sharing and access control |
| `GroupResourceProfileService` | Group resource profiles and policies |
| `GatewayResourceProfileService` | Gateway resource preferences |
| `UserResourceProfileService` | User resource preferences |
| `SSHAccountService` | SSH account provisioning and validation |
| `DataProductService` | Data products and replicas |
| `NotificationService` | Notification CRUD |
| `ParserService` | Parsers and parsing templates |

Shared utilities: `SharingHelper` (sharing operations), `EventPublisher` (messaging), `RequestContext` (transport-agnostic identity).

#### Thrift Services (all on port 8930, multiplexed)

| Service name | Handler | Responsibility |
|---|---|---|
| `Airavata` | `AiravataServerHandler` | Public API — delegates to service layer |
| `RegistryService` | `RegistryServerHandler` | Metadata and definitions for tasks and applications |
| `CredentialStore` | `CredentialStoreServerHandler` | Secure storage and retrieval of compute credentials |
| `SharingRegistry` | `SharingRegistryServerHandler` | Sharing and permissioning of Airavata resources |
| `UserProfile` | `UserProfileServiceHandler` | User profile management |
| `TenantProfile` | `TenantProfileServiceHandler` | Tenant/gateway management |
| `IamAdminServices` | `IamAdminServicesHandler` | IAM administration |
| `GroupManager` | `GroupManagerServiceHandler` | Group and role management |
| `Orchestrator` | `OrchestratorServerHandler` | Workflow DAG construction and task dispatch |

#### Background Services (IServer lifecycle, same JVM)

| Service | Responsibility |
|---|---|
| `DBEventManagerRunner` | Syncs task execution events to the DB via pub/sub |
| `MonitoringServer` | Prometheus metrics endpoint (optional) |
| `ComputationalResourceMonitoringService` | Polls compute resource queue status |
| `DataInterpreterService` | Metadata analysis for submitted jobs |
| `ProcessReschedulingService` | Retries and reschedules failed processes |
| `HelixController` | Manages Helix cluster state transitions |
| `GlobalParticipant` | Executes task steps (`EnvSetupTask`, `InputDataStagingTask`, `OutputDataStagingTask`, `JobVerificationTask`, `CompletingTask`, `ForkJobSubmissionTask`, `DefaultJobSubmissionTask`, `LocalJobSubmissionTask`, `ArchiveTask`, `WorkflowCancellationTask`, `RemoteJobCancellationTask`, `CancelCompletingTask`, `DataParsingTask`, `ParsingTriggeringTask`) |
| `PreWorkflowManager` | Handles pre-execution phase (env setup, data staging) |
| `PostWorkflowManager` | Handles post-execution phase (cleanup, output fetching) |
| `ParserWorkflowManager` | Handles parsing/data-interpretation workflow phase |
| `EmailBasedMonitor` | Polls email inbox for job status updates (optional) |
| `RealtimeMonitor` | Listens on Kafka for real-time job state changes |

```mermaid
stateDiagram-v2
    [*] --> CREATED
    CREATED --> VALIDATED : validateExperiment
    VALIDATED --> LAUNCHED : launchExperiment
    LAUNCHED --> EXECUTING : PreWorkflowManager
    EXECUTING --> MONITORING : job submitted
    MONITORING --> COMPLETED : job finished
    MONITORING --> FAILED : job error
    EXECUTING --> CANCELLED : terminateExperiment
    COMPLETED --> [*]
    FAILED --> [*]
    CANCELLED --> [*]
```

```mermaid
graph LR
    subgraph "Startup Sequence"
        direction TB
        A[1. DB Init] --> B[2. Thrift Handlers]
        B --> C[3. TMultiplexedProcessor :8930]
        C --> D[4. DBEventManager]
        D --> E[5. MonitoringServer :9097]
        E --> F[6. HelixController]
        F --> G{waitForHelixCluster}
        G --> H[7. HelixParticipant]
        H --> I[8. Pre/Post/Parser WF Managers]
        I --> J[9. Email/Realtime Monitors]
    end
```


### 2. Airavata File Server `(apache-airavata-file-server)`
> Class Name: `org.apache.airavata.file.server.FileServerApplication`
> Command: `bin/

The Airavata File Server is a lightweight SFTP wrapper running on storage nodes integrated with Airavata. It lets users securely access storage via SFTP, using Airavata authentication tokens as ephemeral passwords.


### 3. Airavata Agent Service `(apache-airavata-agent-service)` [NEW]
> Class Name: `org.apache.airavata.agent.connection.service.AgentServiceApplication`

The Airavata Agent Service is the backend for launching **interactive** jobs using Airavata.
It provide constructs to launch a custom "Agent" on a compute resource, that connects back to the Agent Service through a bi-directional gRPC channel.
The Airavata Python SDK primarily utilizes the Agent Service (gRPC) and the Airavata API (Thrift) to submit and execute interactive jobs, spawn subprocesses, and create network tunnels to subprocesses, even if they are behind NAT.


### 4. Airavata Research Service `(apache-airavata-research-service)` [NEW]
> Class Name: `org.apache.airavata.research.service.ResearchServiceApplication`

The Airavata Research Service is the backend for the **research catalog** in Airavata. It provides the API to add, list, modify, and publish notebooks, repositories, datasets, and computational models in cybershuttle, and launch interactive remote sessions to utilize them in a research setting.


## 🏗️ Getting Started

### Prerequisites

| Requirement | Version | Check Using |
|-------------|---------|-------|
| **Java SDK** | 17+ | `java --version` |
| **Apache Maven** | 3.8+ | `mvn -v` |
| **Docker Engine** | 20.10+ | `docker -v` |
| **Docker Compose** | 2.0+ | `docker compose version` |

### Quick Start

First, clone the repository and start the infrastructure (MariaDB, RabbitMQ, ZooKeeper, Kafka, Keycloak):

```bash
git clone https://github.com/apache/airavata.git
cd airavata
docker compose up -d
```

Next, build Airavata and set up the database:

```bash
./scripts/setup.sh
```

Finally, start the server:

```bash
./scripts/start.sh
```

The Airavata Server will be available on port **8930** (Thrift, multiplexed). All 9 Thrift services are served on this single port.

### Configuration

The main configuration file is `airavata-api/src/main/resources/airavata-server.properties`. Key settings:

* `apiserver.port` — Thrift port (default: `8930`)
* `spring.datasource.url` — JDBC URL for the unified `airavata` database
* `email.based.monitoring.enabled` — Enable/disable email-based job monitor
* `api.server.monitoring.enabled` — Enable/disable Prometheus metrics endpoint

### Spring Boot Microservices

The following services are separate Spring Boot applications, packaged independently:

| Service | Entry point | Purpose |
|---|---|---|
| **Agent Service** | `AgentServiceApplication` | Backend for interactive jobs via gRPC |
| **File Server** | `FileServerApplication` | SFTP wrapper for storage nodes |
| **Research Service** | `ResearchServiceApplication` | Research catalog API |
| **REST Proxy** | *(restproxy module)* | REST-to-Thrift proxy for the API |


## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

1. **🍴 Fork the repository**
2. **🌿 Create a feature branch**
3. **✨ Make your changes**
4. **🧪 Add tests if applicable**
5. **📝 Submit a pull request**

**Learn More:**
- [Contributing Guidelines](http://airavata.apache.org/get-involved.html)
- [Code of Conduct](https://www.apache.org/foundation/policies/conduct.html)
- [Developer Resources](https://cwiki.apache.org/confluence/display/AIRAVATA)

### Setting up your IDE

The easiest way to setup a development environment is to follow the instructions in the [ide-integration README](./modules/ide-integration/README.md).Those instructions will guide you on setting up a development environment with IntelliJ IDEA.

### Additional Tools

* `org.apache.airavata.sharing.registry.migrator.airavata.AiravataDataMigrator`
* `modules/deployment-scripts`
* `modules/load-client`


## 💬 Community & Support

**Get Help:**
- 📧 **User Mailing List:** [users@airavata.apache.org](mailto:users@airavata.apache.org)
- 👨‍💻 **Developer Mailing List:** [dev@airavata.apache.org](mailto:dev@airavata.apache.org)
- 🔗 **All Mailing Lists:** [airavata.apache.org/mailing-list](https://airavata.apache.org/mailing-list.html)

## 📄 License

```
Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements. See the NOTICE file distributed with this work for
additional information regarding copyright ownership.

The ASF licenses this file to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
```

See the [LICENSE](LICENSE) file for complete license details.

---

<div align="center">
  <strong>Made with ❤️ by the Apache Airavata Community</strong>
</div>
