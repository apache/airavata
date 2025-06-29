# Apache Airavata - IDE Integration Setup

<div align="center">
  <h3>🚀 Complete Development Environment Setup for IntelliJ IDEA</h3>
  <p>Set up a full Airavata installation inside IntelliJ IDEA for seamless development</p>
</div>

---

## 📋 Prerequisites

Before starting, ensure you have the following installed on your system:

| Tool | Version | Purpose | Installation Link |
|------|---------|---------|-------------------|
| 🐳 **Docker & Docker Compose** | Latest | Container orchestration | [Get Docker](https://docs.docker.com/compose/) |
| 💡 **IntelliJ IDEA** | Latest | IDE with Java 17+ | [Download IDEA](https://www.jetbrains.com/idea/download/) |
| ☕ **Java JDK** | 17+ | Runtime environment | [OpenJDK 17](https://openjdk.org/projects/jdk/17/) |
| 🔧 **Apache Maven** | 3.8+ | Build tool | [Install Maven](https://maven.apache.org/install.html) |
| 📝 **Git** | Latest | Version control | [Install Git](https://git-scm.com/downloads) |
| 🐍 **Python** | 3.8+ | Django portal | [Python.org](https://www.python.org/downloads/) |
| 📦 **Node.js & npm** | Latest LTS | Frontend build tools | [Node.js](https://nodejs.org/) |

## 🏗️ Development Environment Setup

### 1️⃣ Clone and Prepare Repository

```bash
# Clone the main repository
git clone https://github.com/apache/airavata.git
cd airavata

# Build the project (this may take a few minutes)
mvn clean install -DskipTests
```

### 2️⃣ Open in IntelliJ IDEA

1. **Launch IntelliJ IDEA**
2. **Open Project** → Navigate to your cloned `airavata` directory
3. **Navigate to:** `modules` → `ide-integration` module

## 🐳 Backend Services Setup

### 3️⃣ Configure Host Resolution

Add the following entry to your system's hosts file:

**Linux/macOS:** `/etc/hosts`
**Windows:** `C:\Windows\System32\drivers\etc\hosts`

```bash
127.0.0.1    airavata.host
```

### 4️⃣ Start Backend Services

Navigate to the containers directory and start all required services:

```bash
cd modules/ide-integration/src/main/containers
docker-compose up -d
```

**Services Started:**
- 🗄️ **MySQL Database**
- 🔐 **Keycloak** (Authentication)
- 📨 **Apache Kafka** (Messaging)
- 🐰 **RabbitMQ** (Message Queue)
- 🔒 **SSHD Server** (Secure connections)

### 5️⃣ Initialize Database

Apply database migrations:

```bash
cd modules/ide-integration/src/main/containers
cat ./database_scripts/init/*-migrations.sql | docker exec -i containers-db-1 mysql -p123456
```

## 🖥️ Starting Airavata Components

### 6️⃣ Start API Server

1. **Navigate to:** `org.apache.airavata.ide.integration.APIServerStarter`
2. **Right-click** in the editor
3. **Select:** `Run 'APIServerStarter.main()'`

> 💡 **JDK 17+ Note:** Add this JVM argument in your run configuration:
> ```
> --add-opens java.base/java.lang=ALL-UNNAMED
> ```

### 7️⃣ Start Job Execution Engine

1. **Navigate to:** `org.apache.airavata.ide.integration.JobEngineStarter`
2. **Right-click** and select **Run**

**Components Started:**
- 🔄 Helix Controller
- 👥 Helix Participant  
- ⚙️ Pre Workflow Manager
- 📋 Post Workflow Manager

### 8️⃣ Start Job Monitoring

#### **Setup Email Monitor (One-Time Setup)**

1. **Create a Gmail Account**  [https://accounts.google.com/signup](https://accounts.google.com/signup)

2. **Enable 2-Step Verification**  [https://myaccount.google.com/security](https://myaccount.google.com/security)

3. **Go to App Passwords**  [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)  
   *(Make sure you're logged in and have already enabled 2-Step Verification.)*

4. **Generate App Password:**
   - Enter the name **"Airavata"** and click **"Generate"**.

5. **Copy the Generated App Password**  
   - A 16 character password will appear **copy and save it immediately**, as it will not be shown again.

5. **Update Configuration:**
   Edit `src/main/resources/airavata-server.properties`:
   ```properties
   email.based.monitor.address=your-email@gmail.com
   email.based.monitor.password=your-app-password
   ```

6. **Start Monitor:**
   - Navigate to: `org.apache.airavata.ide.integration.JobMonitorStarter`
   - Right-click and **Run**

## 🌐 User Portal Setup (Django)

### 9️⃣ Django Portal Installation

**You can create and launch experiments and manage credentials using this portal.**

```bash
# Navigate outside the Airavata directory
cd ..

# Clone the Django portal repository
git clone https://github.com/apache/airavata-django-portal.git
cd airavata-django-portal

# Create a virtual environment
python3 -m venv venv

# Activate the virtual environment
source venv/bin/activate  # For Windows: venv\Scripts\activate

# Install Python dependencies
pip install -r requirements.txt

### 🔟 Configure Django Portal

```bash
# Create local settings
cp django_airavata/settings_local.py.ide django_airavata/settings_local.py

# Run database migrations
python3 manage.py migrate

# Build JavaScript components
./build_js.sh

# Load default CMS pages
python3 manage.py load_default_gateway

# Start development server
python3 manage.py runserver
```

### 🌍 Access User Portal

- **URL:** [http://localhost:8000/auth/login](http://localhost:8000/auth/login)
- **Username:** `default-admin`
- **Password:** `123456`

## 🛠️ Admin Portal Setup (Optional)

For registering compute resources and storage resources:

### 1️⃣ Starting Super Admin Portal (PGA)

**This portal is required when registering new compute or storage resources into the gateway.**

```bash
cd modules/ide-integration/src/main/containers/pga
docker-compose up -d
```

### 2️⃣ Configure Host Resolution

**Get host machine IP:**

**macOS:**
```bash
docker-compose exec pga getent hosts docker.for.mac.host.internal | awk '{ print $1 }'
```

**Windows:**
```bash
docker-compose exec pga getent hosts host.docker.internal
```

**Update container hosts:**
*Replace <host-machine-ip> with the actual IP*
```bash
docker-compose exec pga /bin/sh -c "echo '<host-machine-ip> airavata.host' >> /etc/hosts"
```

### 3️⃣ Access Admin Portal

- **URL:** [http://airavata.host:8008](http://airavata.host:8008)
- **Username:** `default-admin`
- **Password:** `123456`

## 🛑 Cleanup & Troubleshooting

### Stop All Services

```bash
# In each docker-compose directory, run:
docker-compose down
docker-compose rm -f

# Remove unused containers and networks
docker system prune
```

### 🔐 Certificate Renewal (If Expired)

Only needed when Keycloak certificates expire:

```bash
cd modules/ide-integration/src/main/resources/keystores

# Remove old keystore
rm airavata.jks

# Generate new keystore (airavata.jks)
keytool -genkey -keyalg RSA -alias selfsigned -keystore airavata.jks \
        -storetype pkcs12 -storepass airavata -validity 360 -keysize 2048 \
        -dname "CN=airavata.host,OU=airavata.host,O=airavata.host,L=airavata.host,ST=airavata.host,C=airavata.host"
```

## 📊 Service Status Overview

| Service | Port | Status Check | Purpose |
|---------|------|-------------|---------|
| 🗄️ **MySQL** | 3306 | `docker ps` | Database |
| 🔐 **Keycloak** | 8443 | [airavata.host:8443](http://airavata.host:8443) | Authentication |
| 📨 **Kafka** | 9092 | Internal | Messaging |
| 🐰 **RabbitMQ** | 5672 | Internal | Message Queue |
| 🌐 **Django Portal** | 8000 | [localhost:8000](http://localhost:8000) | User Interface |
| 🛠️ **PGA Admin** | 8008 | [airavata.host:8008](http://airavata.host:8008) | Admin Portal |

## 🆘 Common Issues

**Port Conflicts:**
```bash
# Check what's using a port
lsof -i :8000
netstat -tulpn | grep :8000
```

**Docker Issues:**
```bash
# Reset Docker
docker system prune -a
docker-compose down --volumes
```

**Build Failures:**
```bash
# Clean Maven cache
mvn clean
rm -rf ~/.m2/repository/org/apache/airavata
```

---

<div align="center">
  <strong>🎉 Happy Developing with Apache Airavata!</strong>
  <br>
  <em>Need help? Check our <a href="https://airavata.apache.org/mailing-list.html">mailing lists</a></em>
</div>