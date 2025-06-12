# Apache Airavata

  ![image](https://github.com/user-attachments/assets/6d908819-cf5e-48d0-bbf7-f031c95adf94)

  [![Build Status](https://github.com/apache/airavata/actions/workflows/maven-build.yml/badge.svg)](https://github.com/apache/airavata/actions/workflows/maven-build.yml)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
  [![Contributors](https://img.shields.io/github/contributors/apache/airavata.svg)](https://github.com/apache/airavata/graphs/contributors)
</div>

## 🚀 About

Apache Airavata is a software framework for executing and managing computational jobs on distributed computing resources including local clusters, supercomputers, national grids, academic and commercial clouds. Airavata builds on general concepts of service oriented computing, distributed messaging, and workflow composition and orchestration. Airavata bundles a server package with an API, client software development Kits and a general purpose reference UI implementation.

**Key Features:**
- 🔧 Service-oriented architecture with distributed messaging
- 🔄 Workflow composition and orchestration
- ☁️ Multi-cloud and hybrid cloud support
- 🖥️ Comprehensive API and SDK ecosystem
- 🌐 Reference UI implementation via [Apache Airavata Django Portal](https://github.com/apache/airavata-django-portal)

> Learn more at [airavata.apache.org](https://airavata.apache.org)

## 🧱 The Airavata Ecosystem

Apache Airavata is composed of modular components spanning core services, data management, user interfaces, and developer tooling.

### 🔧 Core Services
- [`airavata`](https://github.com/apache/airavata) – Main microservices and orchestration engine
- [`airavata-custos`](https://github.com/apache/airavata-custos) – Identity and access management framework
- [`airavata-mft`](https://github.com/apache/airavata-mft) – Managed file transfer services

### 🖥️ User Interfaces
- [`airavata-django-portal`](https://github.com/apache/airavata-django-portal) – Primary reference UI
- [`airavata-django-portal-sdk`](https://github.com/apache/airavata-django-portal-sdk) – SDK for custom portal development
- [`airavata-django-portal-commons`](https://github.com/apache/airavata-django-portal-commons) – Shared Django utilities
- [`airavata-cookiecutter-django-app`](https://github.com/apache/airavata-cookiecutter-django-app) – Starter template for apps
- [`airavata-cookiecutter-django-output-view`](https://github.com/apache/airavata-cookiecutter-django-output-view) – Template for output views
- [`airavata-custos-portal`](https://github.com/apache/airavata-custos-portal) – UI for Custos
- [`airavata-php-gateway`](https://github.com/apache/airavata-php-gateway) – Legacy PHP gateway
- [`airavata-jupyter-kernel`](https://github.com/apache/airavata-jupyter-kernel) – Jupyter integration

### 📦 Data & Metadata Services
- [`airavata-data-lake`](https://github.com/apache/airavata-data-lake) – Data lake and storage backend
- [`airavata-data-catalog`](https://github.com/apache/airavata-data-catalog) – Metadata and search services
- [`airavata-mft-portal`](https://github.com/apache/airavata-mft-portal) – Web UI for file transfers

### 📚 Documentation & Branding
- [`airavata-docs`](https://github.com/apache/airavata-docs) – Developer documentation
- [`airavata-user-docs`](https://github.com/apache/airavata-user-docs) – End-user guides
- [`airavata-admin-user-docs`](https://github.com/apache/airavata-admin-user-docs) – Admin-focused documentation
- [`airavata-custos-docs`](https://github.com/apache/airavata-custos-docs) – Custos documentation
- [`airavata-site`](https://github.com/apache/airavata-site) – Project website

### 🧪 Experimental & Research
- [`airavata-sandbox`](https://github.com/apache/airavata-sandbox) – Prototypes and early-stage work
- [`airavata-labs`](https://github.com/apache/airavata-labs) – Experimental projects
- [`airavata-cerebrum`](https://github.com/apache/airavata-cerebrum) – Airavata for Neuroscience

## 📋 Prerequisites

Before building Apache Airavata, ensure you have:

| Requirement | Version | Notes |
|-------------|---------|-------|
| **Java SDK** | 17+ | Set `JAVA_HOME` environment variable |
| **Apache Maven** | 3.8+ | Build automation tool |
| **Git** | Latest | Version control |

### 🔧 Environment Setup

**Ubuntu/Debian:**
```bash
export JAVA_HOME=/usr/lib/jvm/openjdk-17-jdk
```

**macOS:**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

**Windows:**
```powershell
set JAVA_HOME=C:\Program Files\OpenJDK\openjdk-17
```

## 🏗️ Building from Source

### Quick Start

```bash
# Clone the repository
git clone git@github.com:apache/airavata.git
cd airavata

# Build with tests
mvn clean install

# Build without tests (faster)
mvn clean install -DskipTests

# Find your distribution
ls -la modules/distribution/target/
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
