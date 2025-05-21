# Apache Airavata

[![Build Status](https://travis-ci.org/apache/airavata.svg?branch=master)](https://travis-ci.org/apache/airavata)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.airavata/airavata/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.airavata%22)

Apache Airavata is a powerful, extensible software framework for orchestrating and managing computational workflows across distributed computing resources — including local clusters, HPC systems, grids, and cloud environments. Airavata brings together the strengths of service-oriented architecture, messaging frameworks, and scientific workflow management.

- 🧠 Workflow orchestration for scientific computing
- ⚙️ API-based modular architecture
- 🎛️ Pluggable components and microservices
- 🌐 Web-based reference UI using Django

📘 Learn more at: [https://airavata.apache.org](https://airavata.apache.org)  
🧑‍💻 Reference UI: [Apache Airavata Django Portal](https://github.com/apache/airavata-django-portal)

---

## 🚀 Features

- Distributed job execution
- Pluggable workflow engine
- gRPC/Thrift APIs and SDKs
- Docker-based local setup for developers
- Integration-ready with Keycloak and Django Portal

---

## 📦 Prerequisites

To build and run Apache Airavata locally:

- Java 17+
- Apache Maven 3.6+
- Git
- Docker & Docker Compose (for local test deployment)

Set the `JAVA_HOME` environment variable:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

---

## 🛠️ Building from Source

Clone the repository and compile the source:

```bash
git clone https://github.com/apache/airavata.git
cd airavata
mvn clean install
```

> ⏭️ To skip tests:

```bash
mvn clean install -Dmaven.test.skip=true
```

The final distribution will be available at:

```
modules/distribution/target/
```

---

## 🐳 Docker-Based Local Deployment (Dev Only)

> ⚠️ This is experimental and not for production.

### 🧱 Build Docker Images

```bash
git clone https://github.com/apache/airavata.git
cd airavata
mvn clean install
mvn docker:build -pl modules/distribution
```

### ▶️ Start Docker Services

```bash
docker-compose -f modules/ide-integration/src/main/containers/docker-compose.yml \
               -f modules/distribution/src/main/docker/docker-compose.yml up
```

### ⛔ Stop Docker Services

```bash
docker-compose -f modules/ide-integration/src/main/containers/docker-compose.yml \
               -f modules/distribution/src/main/docker/docker-compose.yml down
```

> After code changes, stop the containers, rebuild the images, and restart.

---

## 🧪 Local Development Setup (with IntelliJ)

The easiest way to start developing is to follow the [IDE Integration README](./modules/ide-integration/README.md). It walks you through setting up IntelliJ IDEA for working on Apache Airavata.

---

## 🔗 Local Service Endpoints

When using Docker, the following services are exposed:

| Service            | URL                                |
|--------------------|-------------------------------------|
| API Server         | http://airavata.host:8930          |
| Profile Service    | http://airavata.host:8962          |
| Keycloak Auth      | https://airavata.host:8443         |

Add to `/etc/hosts`:

```text
127.0.0.1 airavata.host
```

---

## 🤝 Contributing

We welcome contributions from the community! To get involved:

- 📄 Read our [Contributor Guide](http://airavata.apache.org/get-involved.html)
- 📧 Join the [mailing lists](https://airavata.apache.org/mailing-list.html)
- 🐛 View or file issues on our [JIRA Board](https://issues.apache.org/jira/projects/AIRAVATA)

---

## 📚 Resources

- [Official Documentation](https://docs.airavata.org/en/master/)
- [Developer Wiki](https://cwiki.apache.org/confluence/display/AIRAVATA)
- [Issue Tracker (JIRA)](https://issues.apache.org/jira/projects/AIRAVATA)
- [Mailing Lists](https://airavata.apache.org/mailing-list.html)

---

## ⚖️ License

Apache Airavata is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Please refer to the [LICENSE](./LICENSE) and [NOTICE](./NOTICE) files for more details.

---

### 💡 Tip

Need help fast? Reach out on the dev mailing list or file an issue. We're here to support you.
