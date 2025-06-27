# Apache Airavata
![image](https://github.com/user-attachments/assets/6d908819-cf5e-48d0-bbf7-f031c95adf94)

[![Build Status](https://github.com/apache/airavata/actions/workflows/maven-build.yml/badge.svg)](https://github.com/apache/airavata/actions/workflows/maven-build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Contributors](https://img.shields.io/github/contributors/apache/airavata.svg)](https://github.com/apache/airavata/graphs/contributors)

Apache Airavata is a software framework for executing and managing computational jobs on distributed computing resources including local clusters, supercomputers, national grids, academic and commercial clouds. Airavata builds on general concepts of service oriented computing, distributed messaging, and workflow composition and orchestration. Airavata bundles a server package with an API, client software development Kits and a general purpose reference UI implementation.

**Key Features:**
- 🔧 Service-oriented architecture with distributed messaging
- 🔄 Fully-managed task lifecycle (environment setup, data staging, execution, and output retrieval)
- ☁️ Multi-cloud and hybrid cloud support
- 🖥️ Comprehensive API and SDK ecosystem
- 🌐 [Reference UI Implementations](https://github.com/apache/airavata-portals)

![image](assets/airavata-components.png)

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

### Core Airavata Services

![image](assets/airavata-dataflow.png)


#### API Server
> `org.apache.airavata.server.ServerMain`

The API Server bootstraps 7 services - each implementing the `org.apache.airavata.common.utils.IServer` interface - and provides the main entrypoint to Airavata.

1. **API** - public-facing API consumed by Airavata SDKs and dashboards. It bridges external clients and internal services, and is served over Thrift.
   (`org.apache.airavata.api.server.AiravataAPIServer`)
2. **DB Event Manager** - Monitors task execution events (launch, transitions, completion/failure) and syncs them to the Airavata DB via pub/sub hooks.
   (`org.apache.airavata.db.event.manager.DBEventManagerRunner`)
3. **Registry** - Manages metadata and definitions for executable tasks and applications.
   (`org.apache.airavata.registry.api.service.RegistryAPIServer`)
4. **Credential Store** - Manages secure storage and retrieval of credentials for accessing registered compute resources.
   (`org.apache.airavata.credential.store.server.CredentialStoreServer`)
5. **Sharing Registry** - Handles sharing and permissioning of Airavata resources between users and groups.
   (`org.apache.airavata.sharing.registry.server.SharingRegistryServer`)
6. **Orchestrator** - Constructs workflow DAGs, assigns unique IDs to tasks, and hands them off to the workflow manager.
   (`org.apache.airavata.orchestrator.server.OrchestratorServer`)
7. **Profile** - Manages users, tenants, compute resources, and group profiles.
   (`org.apache.airavata.service.profile.server.ProfileServiceServer`)

#### Pre Workflow Manager
> `org.apache.airavata.helix.impl.workflow.PreWorkflowManager`

The pre-workflow manager listens on the internal MQ (KafkaConsumer) to inbound tasks at **pre-execution** phase. When a task DAG is received, it handles the environment setup and data staging phases of the DAG in a robust manner, which includes fault-handling. All these happen BEFORE the task DAG is submitted to the controller, and subsequently to the participant.

#### Controller
> `org.apache.airavata.helix.impl.controller.HelixController`

The Controller manages the step-by-step transition of task state on *helix-side*. It uses Apache Helix to track step start, completion, and failure paths, ensuring the next step starts upon successful completion or retrying the current step on failure.

#### Participant
> `org.apache.airavata.helix.impl.participant.GlobalParticipant`

The participant synchronizes the *helix-side* state transition of a task with its concrete execution at *airavata-side*. The currently registered steps are: `EnvSetupTask`, `InputDataStagingTask`, `OutputDataStagingTask`, `JobVerificationTask`, `CompletingTask`, `ForkJobSubmissionTask`, `DefaultJobSubmissionTask`, `LocalJobSubmissionTask`, `ArchiveTask`, `WorkflowCancellationTask`, `RemoteJobCancellationTask`, `CancelCompletingTask`, `DataParsingTask`, `ParsingTriggeringTask`, and `MockTask`.

#### Post Workflow Manager
> `org.apache.airavata.helix.impl.workflow.PostWorkflowManager`

The post-workflow listens on the internal MQ (KafkaConsumer) to inbound tasks at **post-execution** phase. Once a task is received, it handles the cleanup and output fetching phases of the task DAG in a robust manner, which includes fault-handling. Once the main task completes executing, this is announced to the realtime monitor, upon which the post-workflow phase is triggered. Once triggered, it submits this state change to the controller.

#### Parser Workflow Manager (Deprecated)
> `org.apache.airavata.helix.impl.workflow.ParserWorkflowManager`

The parser-workflow listens on the internal MQ (KafkaConsumer) to inbound tasks at **post-completion** phase., which includes transforming generated outputs into different formats. This component is not actively used in airavata.

![image](assets/airavata-state-transitions.png)

#### Email Monitor
> Class Name: `org.apache.airavata.monitor.email.EmailBasedMonitor`

The email monitor periodically checks an email inbox for job status updates sent via email. If it reads a new email with a job status update, it relays that state-change to the internal MQ (KafkaProducer).

#### Realtime Monitor
> Class Name: `org.apache.airavata.monitor.realtime.RealtimeMonitor`

The realtime monitor listens to incoming state-change messages on the internal MQ (KafkaConsumer), and relays that state-change to the internal MQ (KafkaProducer). When a task is completed at the compute resource, the realtime monitor is notified of this.

#### Agent Service
> Class Name: `org.apache.airavata.agent.connection.service.AgentServiceApplication`

The agent service is the backend for launching interactive jobs using Airavata.
It provide constructs to launch a custom "Agent" on a compute resource, that connects back to the Agent Service through a bi-directional gRPC channel.
The Airavata Python SDK primarily utilizes the Agent Service (gRPC) and the Airavata API (Thrift) to submit and execute interactive jobs, spawn subprocesses, and create network tunnels to subprocesses, even if they are behind NAT.

#### Research Service
> Class Name: `org.apache.airavata.research.service.ResearchServiceApplication`

The research service is the backend for the Airavata research catalog. It provides the API to add, list, modify, and publish notebooks, repositories, datasets, and computational models in cybershuttle, and launch interactive remote sessions to utilize them in a research setting.


## 🏗️ Building from Source

### Prerequisites

Before building Apache Airavata, ensure you have:

| Requirement | Version | Check Using |
|-------------|---------|-------|
| **Java SDK** | 17+ | `java --version` |
| **Apache Maven** | 3.8+ | `mvn -v` |
| **Git** | Latest | `git -v` |

### Quick Start

1. Clone the project repository
```bash
git clone git@github.com:apache/airavata.git
cd airavata
```

2. Build the project
```bash
# with tests (slower, but safer)
mvn clean install
# OR without tests (faster)
mvn clean install -DskipTests
```

Once the build completes, the service bundles will be generated in the `./distributions` folder.
```bash
├── airavata-sharing-registry-distribution-0.21-SNAPSHOT.tar.gz
├── apache-airavata-agent-service-0.21-SNAPSHOT.tar.gz
├── apache-airavata-api-server-0.21-SNAPSHOT.tar.gz
├── apache-airavata-controller-0.21-SNAPSHOT.tar.gz
├── apache-airavata-email-monitor-0.21-SNAPSHOT.tar.gz
├── apache-airavata-file-server-0.21-SNAPSHOT.tar.gz
├── apache-airavata-parser-wm-0.21-SNAPSHOT.tar.gz
├── apache-airavata-participant-0.21-SNAPSHOT.tar.gz
├── apache-airavata-post-wm-0.21-SNAPSHOT.tar.gz
├── apache-airavata-pre-wm-0.21-SNAPSHOT.tar.gz
├── apache-airavata-realtime-monitor-0.21-SNAPSHOT.tar.gz
└── apache-airavata-research-service-0.21-SNAPSHOT.tar.gz

1 directory, 12 files
```


### 🐳 Docker Development (Experimental)

> ⚠️ **Note:** Docker deployment is experimental and not recommended for production use.

**Prerequisites:**
- Docker Engine 20.10+
- Docker Compose 2.0+

**Build and Deploy:**

```bash
# 1. Build source and Docker images
git clone https://github.com/apache/airavata.git
cd airavata
mvn clean install
mvn docker:build -pl modules/distribution

# 2. Start all services
##Start all supporting services and Airavata microservices (API Server, Helix components, and Job Monitors)

docker-compose \
  -f modules/distribution/src/main/docker/docker-compose.yml \
  up -d

# 3. Verify services are running
docker-compose ps
```

**Service Endpoints:**
- **API Server:** `airavata.host:8960`
- **Profile Service:** `airavata.host:8962`
- **Keycloak:** `airavata.host:8443`

**Host Configuration:**
Add to your `/etc/hosts` file:
```
127.0.0.1    airavata.host
```

**Stop Services:**
```bash
docker-compose \
  -f modules/ide-integration/src/main/containers/docker-compose.yml \
  -f modules/distribution/src/main/docker/docker-compose.yml \
  down
```

## 🚀 Getting Started

The easiest way to get started with running Airavata locally and setting up a development environment is to follow the instructions in the [ide-integration README](./modules/ide-integration/README.md).Those instructions will guide you on setting up a development environment with IntelliJ IDEA.

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
