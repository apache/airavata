# Airavata Setup Flow and Multi-Host Configuration

This document explains the setup flow and how to configure Airavata components across multiple hosts.

## `airavata_setup.yml` Execution Flow

### Execution Order

```
1. env_setup
   ↓
2. java
   ↓
3. common
   ↓
4. zookeeper
   ↓
5. kafka
   ↓
6. rabbitmq
   ↓
7. database
   ↓
8. letsencrypt
   ↓
9. keycloak
   ↓
10. reverse_proxy
   ↓
11. api-orch (conditional)
   ↓
12. airavata_services
```

### Role Details

#### 1. `env_setup`
**Purpose**: System-level environment setup
- Creates system user/group (`user`, `group` variables)
- Configures firewall (firewalld/ufw)
- Sets up basic system requirements
- **Runs on**: `airavata_servers` (or respective host groups)

#### 2. `java`
**Purpose**: Java installation
- Installs OpenJDK or Oracle JDK
- Sets `JAVA_HOME`
- **Runs on**: Any host that needs Java (Airavata, Keycloak)

#### 3. `common`
**Purpose**: Common development tools
- Installs Maven
- Installs Git
- Checks out Airavata source code
- **Runs on**: `airavata_servers` only (needed for building Airavata)
- **Become user**: `{{ user }}` (non-root)

#### 4. `zookeeper`
**Purpose**: Zookeeper installation
- Downloads and installs Zookeeper
- Configures `zoo.cfg`
- Creates systemd service
- **Runs on**: `[zookeeper]` group (usually same as `airavata_servers`)

#### 5. `kafka`
**Purpose**: Kafka installation
- Downloads and installs Kafka
- Configures `server.properties`
- Creates systemd service
- **Runs on**: `[kafka]` group (usually same as `airavata_servers`)

#### 6. `rabbitmq`
**Purpose**: RabbitMQ installation
- Installs RabbitMQ from distribution packages (Ubuntu/Debian) or RPM repositories (CentOS/Rocky)
- For Ubuntu 24.04: Uses distro packages (erlang + rabbitmq-server)
- Creates vhosts and users
- Configures permissions
- **Runs on**: `[rabbitmq]` group (usually same as `airavata_servers`)

#### 7. `database`
**Purpose**: MariaDB installation and database setup
- Installs MariaDB
- Sets root password
- Creates databases:
  - `experiment_catalog`
  - `app_catalog`
  - `replica_catalog`
  - `workflow_catalog`
  - `sharing_catalog`
  - `credential_store`
  - `profile_service`
  - `research_catalog`
  - `keycloak` (if Keycloak role runs)
- Creates database user (`db_user` with `db_password`)
- **Runs on**: `[database]` group (can be separate host)

#### 8. `letsencrypt`
**Purpose**: SSL certificate generation
- Installs certbot
- Generates Let's Encrypt certificates for:
  - Keycloak vhost (`keycloak_vhost_servername`)
  - API server (`api_server_public_hostname`)
- **Runs on**: Hosts that need SSL certificates

#### 9. `keycloak`
**Purpose**: Keycloak IAM installation
- Downloads Keycloak distribution (24.0.0+ uses Quarkus)
- Installs MariaDB JDBC driver in `providers/` directory (for Keycloak 24+)
- Configures database connection via environment variables (can be remote)
- Sets up SSL certificates
- Creates admin user via environment variables
- Imports realm definition (default realm) if `keycloak_realm_import_enabled` is true
- Creates systemd service
- **Runs on**: `airavata_servers` (by default) or `keycloak_servers` (if separate)
- **Note**: Requires database, Java, and SSL certificates
- **Keycloak 24+**: Uses Quarkus, requires MariaDB driver in providers directory, uses environment variables for configuration

#### 10. `reverse_proxy`
**Purpose**: Apache2 reverse proxy for Keycloak
- Installs Apache2/httpd
- Configures virtual host to proxy to Keycloak
- Enables SSL
- **Runs on**: Same host as Keycloak

#### 11. `api-orch`
**Purpose**: HAProxy for API server SSL termination
- Installs HAProxy
- Configures SSL termination
- Proxies to API server on port 8930
- **Runs on**: `airavata_servers`
- **Condition**: Only runs if `api_server_public_hostname` is defined

#### 12. `airavata_services`
**Purpose**: Build and deploy Airavata services
- Builds Airavata from source (Maven)
- Generates configuration files from templates
- Deploys services to `deployment_dir`
- Starts all services (API Server, Orchestrator, Registry, Agent Service, Research Service, File Server, REST Proxy)
- **Runs on**: `airavata_servers` only
- **Become user**: `{{ user }}` (non-root)


## Multi-Host Configuration

### Supported Separations

You can separate:
- **Keycloak** → Separate host
- **Database** → Separate host
- **Airavata Services** → One host (all services together)

### Example: Three-Host Setup

**Host 1**: Database Server (MariaDB only)  
**Host 2**: Keycloak Server (Keycloak + Apache2)  
**Host 3**: Airavata Server (All Airavata services + Zookeeper + Kafka + RabbitMQ)

## Configuration Steps

### Step 1: Update Inventory (`inventories/my-env/hosts`)

```ini
# Database server (separate host)
[database]
db-server ansible_host=<DB_IP> ansible_user=exouser

# Keycloak server (separate host)
[keycloak_servers]
keycloak-server ansible_host=<KEYCLOAK_IP> ansible_user=exouser

# Airavata services server
[airavata_servers]
airavata-server ansible_host=<AIRAVATA_IP> ansible_user=exouser

# Infrastructure services (on Airavata server)
[zookeeper]
airavata-server

[kafka]
airavata-server

[rabbitmq]
airavata-server
```

### Step 2: Database Server Configuration

Create `inventories/my-env/group_vars/database/vars.yml`:

```yaml
---
# Database server doesn't need much, but you can set:
# (Most variables are in group_vars/all/vault.yml)
```

Create `inventories/my-env/group_vars/database/vault.yml`:

```yaml
---
# MariaDB root password
mysql_root_password: "CHANGEME_MYSQL_ROOT_PASSWORD"

# Database user for Airavata services
db_user: "airavata"
db_password: "CHANGEME_DB_PASSWORD"
```

### Step 3: Keycloak Server Configuration

Create `inventories/my-env/group_vars/keycloak_servers/vars.yml`:

```yaml
---
# Keycloak host-specific variables
keycloak_vhost_servername: "auth.airavata.apache.org"  # DNS name for Keycloak
letsencrypt_email: "admin@airavata.org"

# Keycloak database connection (points to database server)
keycloak_db_host: "<DB_IP>"  # Database server IP
keycloak_db_port: "3306"
keycloak_db_schema_name: "keycloak"
```

Create `inventories/my-env/group_vars/keycloak_servers/vault.yml`:

```yaml
---
# Keycloak admin credentials
keycloak_master_account_username: "admin"
keycloak_master_account_password: "CHANGEME_KEYCLOAK_ADMIN_PASSWORD"

# Keycloak database credentials
keycloak_db_username: "keycloak"
keycloak_db_password: "CHANGEME_KEYCLOAK_DB_PASSWORD"
```

### Step 4: Airavata Server Configuration

Update `inventories/my-env/group_vars/all/vars.yml` (if needed):

```yaml
---
# Airavata version and build settings
airavata_version: "0.21-SNAPSHOT"
git_branch: "master"
deploy_user: "exouser"
deployment_dir: "/home/{{ deploy_user }}/airavata-deployment"
```

Update `inventories/my-env/group_vars/all/vault.yml`:

```yaml
---
# Database URLs
registry_jdbc_url: "jdbc:mariadb://<DB_IP>:3306/experiment_catalog"
appcatalog_jdbc_url: "jdbc:mariadb://<DB_IP>:3306/app_catalog"
replicacatalog_jdbc_url: "jdbc:mariadb://<DB_IP>:3306/replica_catalog"
workflowcatalog_jdbc_url: "jdbc:mariadb://<DB_IP>:3306/workflow_catalog"
sharingcatalog_jdbc_url: "jdbc:mariadb://<DB_IP>:3306/sharing_catalog"
profile_service_jdbc_url: "jdbc:mariadb://<DB_IP>:3306/profile_service"
credential_store_jdbc_url: "jdbc:mariadb://<DB_IP>:3306/credential_store"

# Agent and Research service datasources
agent_service_datasource_url: "jdbc:mariadb://<DB_IP>:3306/app_catalog"
research_service_datasource_url: "jdbc:mariadb://<DB_IP>:3306/research_catalog"

# IAM credentials (Keycloak) - points to Keycloak server
iam_server_url: "https://auth.airavata.apache.org"  # Keycloak hostname
iam_admin_username: "admin"
iam_admin_password: "CHANGEME_IAM_PASSWORD"

# Database passwords (for connecting to database server)
registry_jdbc_password: "CHANGEME_DB_PASSWORD"
appcatalog_jdbc_password: "CHANGEME_DB_PASSWORD"
# ... (all use same db_password)
agent_service_datasource_password: "CHANGEME_DB_PASSWORD"
research_service_datasource_password: "CHANGEME_DB_PASSWORD"
```

### Step 5: Running Setup

**Option A: Run setup separately for each host**

```bash
# 1. Setup database server first
ansible-playbook -i inventories/my-env airavata_setup.yml \
  --limit database \
  --tags "env_setup,database" \
  --ask-vault-pass

# 2. Setup Keycloak server
ansible-playbook -i inventories/my-env airavata_setup.yml \
  --limit keycloak_servers \
  --tags "env_setup,java,letsencrypt,keycloak,reverse_proxy" \
  --ask-vault-pass

# 3. Setup Airavata server (skip database and keycloak)
ansible-playbook -i inventories/my-env airavata_setup.yml \
  --limit airavata_servers \
  --skip-tags "database,keycloak,iam" \
  --ask-vault-pass
```

**Option B: Use `--limit` to target specific hosts**

The playbook will automatically skip roles that don't apply to the target host based on inventory groups.

## Network Requirements

### Database Server
- **Port 3306**: Must be accessible from:
  - Keycloak server
  - Airavata server
- **Firewall**: Allow MySQL connections from Keycloak and Airavata IPs

### Keycloak Server
- **Port 443 (HTTPS)**: Must be accessible from internet
- **Port 80 (HTTP)**: For Let's Encrypt verification
- **Outbound**: Must connect to database server (port 3306)
- **DNS**: `keycloak_vhost_servername` must resolve to Keycloak server IP

### Airavata Server
- **Port 8930**: API Server (or via HAProxy on 443)
- **Port 8940**: Orchestrator
- **Port 8962**: Profile Service
- **Port 8970**: Registry
- **Port 8960**: Credential Store
- **Port 7878**: Sharing Registry
- **Port 18880**: Agent Service
- **Port 18899**: Research Service
- **Port 8050**: File Server
- **Port 8082**: REST Proxy
- **Port 2181**: Zookeeper
- **Port 9092**: Kafka
- **Port 5672**: RabbitMQ
- **Outbound**: Must connect to:
  - Database server (port 3306)
  - Keycloak server (HTTPS)

## Running Setup

### Full Setup (All on Same Host)
```bash
ansible-playbook -i inventories/dev airavata_setup.yml --ask-vault-pass
```

### Updating Existing Deployment
```bash
# Development server
ansible-playbook -i inventories/dev airavata_update.yml --ask-vault-pass

# Production server
ansible-playbook -i inventories/prod airavata_update.yml --ask-vault-pass
```

### Multi-Host Setup (Separate Hosts)
```bash
# Setup in order: Database → Keycloak → Airavata
ansible-playbook -i inventories/dev airavata_setup.yml \
  --limit database \
  --tags "env_setup,database" \
  --ask-vault-pass

ansible-playbook -i inventories/dev airavata_setup.yml \
  --limit keycloak_servers \
  --tags "env_setup,java,letsencrypt,keycloak,reverse_proxy" \
  --ask-vault-pass

ansible-playbook -i inventories/dev airavata_setup.yml \
  --limit airavata_servers \
  --skip-tags "database,keycloak,iam" \
  --ask-vault-pass
```

## Troubleshooting

### Common Issues

1. **Database connection fails**
   - Check `mysql_root_password` is set correctly
   - Verify MariaDB is running: `systemctl status mariadb`
   - Check firewall allows connections from Keycloak and Airavata hosts
   - Verify database server IP is correct in JDBC URLs

2. **Keycloak fails to start**
   - Verify database is created: `mysql -u root -p -e "SHOW DATABASES;"`
   - Check Keycloak can connect to database: `telnet <DB_IP> 3306`
   - Check Keycloak logs: `journalctl -u keycloak -f`
   - Verify SSL certificates exist
   - Verify `keycloak_db_host` points to correct database server
   - For Keycloak 24+: Verify MariaDB driver exists: `ls -la /home/airavata/keycloak-24.0.0/providers/mariadb-java-client.jar`
   - Check environment variables: `systemctl show keycloak | grep KC_DB`

3. **Airavata services fail to start**
   - Check database connections in `vault.yml` (should point to database server IP)
   - Verify RabbitMQ vhost exists: `rabbitmqctl list_vhosts`
   - Check service logs in `deployment_dir/*/logs/`
   - Verify `iam_server_url` points to Keycloak server
   - Use `start_services.yml` to start services: `ansible-playbook -i inventories/dev start_services.yml --ask-vault-pass`
   - Use `stop_services.yml` to stop services: `ansible-playbook -i inventories/dev stop_services.yml --ask-vault-pass`

4. **SSL certificate generation fails**
   - Verify DNS points to server: `dig keycloak_vhost_servername`
   - Check port 80/443 are open: `ss -tuln | grep -E '80|443'`
   - Verify email is valid: `letsencrypt_email`

5. **Network connectivity issues**
   - Test database connection: `mysql -h <DB_IP> -u root -p`
   - Test Keycloak connection: `curl https://auth.dev.cybershuttle.org`
   - Check firewall rules: `firewall-cmd --list-all` or `ufw status`

## Next Steps After Setup

1. **Verify services are running**:
   ```bash
   # Database server
   ansible database -i inventories/dev -m shell \
     -a "systemctl status mariadb"
   
   # Keycloak server
   ansible keycloak_servers -i inventories/dev -m shell \
     -a "systemctl status keycloak apache2"
   
   # Airavata server
   ansible airavata_servers -i inventories/dev -m shell \
     -a "systemctl status zookeeper kafka rabbitmq-server"
   ```

2. **Check Airavata services**:
   ```bash
   ansible airavata_servers -i inventories/dev -m shell \
     -a "ps aux | grep java | grep airavata"
   ```

3. **Update services** (use `airavata_update.yml`):
   ```bash
   ansible-playbook -i inventories/dev airavata_update.yml \
     --limit airavata_servers \
     --ask-vault-pass
   ```

4. **Start/Stop services independently**:
   ```bash
   # Start all Airavata services
   ansible-playbook -i inventories/dev start_services.yml --ask-vault-pass
   
   # Stop all Airavata services
   ansible-playbook -i inventories/dev stop_services.yml --ask-vault-pass
   ```