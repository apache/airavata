# Apache Airavata

  ![image](https://github.com/user-attachments/assets/6d908819-cf5e-48d0-bbf7-f031c95adf94)

  
  [![Build Status](https://travis-ci.org/apache/airavata.svg?branch=master)](https://travis-ci.org/apache/airavata)
  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.airavata/airavata/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.airavata%22)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
  [![Contributors](https://img.shields.io/github/contributors/apache/airavata.svg)](https://github.com/apache/airavata/graphs/contributors)
</div>

## 🚀 About

Apache Airavata is a modern, scalable software framework designed for executing and managing computational jobs across distributed computing resources. Whether you're working with local clusters, supercomputers, national grids, or cloud platforms, Airavata provides a unified interface for computational workflow management.

**Key Features:**
- 🔧 Service-oriented architecture with distributed messaging
- 🔄 Workflow composition and orchestration
- ☁️ Multi-cloud and hybrid cloud support
- 🖥️ Comprehensive API and SDK ecosystem
- 🌐 Reference UI implementation via [Apache Airavata Django Portal](https://github.com/apache/airavata-django-portal)

> Learn more at [airavata.apache.org](https://airavata.apache.org)

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
git clone https://github.com/apache/airavata.git
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
docker-compose \
  -f modules/ide-integration/src/main/containers/docker-compose.yml \
  -f modules/distribution/src/main/docker/docker-compose.yml \
  up -d

# 3. Verify services are running
docker-compose ps
```

**Service Endpoints:**
- **API Server:** `airavata.host:8930`
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

The fastest way to set up a development environment is through our IDE integration:

1. **Follow our [IDE Integration Guide](./modules/ide-integration/README.md)**
2. **IntelliJ IDEA setup instructions included**
3. **Automated development environment configuration**

## 📚 Documentation & Resources

| Resource | Description | Link |
|----------|-------------|------|
| 📖 **Documentation** | Complete user and developer guides | [docs.airavata.org](https://docs.airavata.org/en/master/) |
| 🔧 **Developer Wiki** | Technical specifications and architecture | [Apache Confluence](https://cwiki.apache.org/confluence/display/AIRAVATA) |
| 🐛 **Issue Tracker** | Bug reports and feature requests | [JIRA](https://issues.apache.org/jira/projects/AIRAVATA) |
| 💬 **Mailing Lists** | Community discussions | [airavata.apache.org/mailing-list](https://airavata.apache.org/mailing-list.html) |

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

**Stay Connected:**
- 🌟 Star this repository
- 👀 Watch for updates
- 🐦 Follow [@ApacheAiravata](https://twitter.com/ApacheAiravata)

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
