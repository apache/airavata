# Airavata Setup Flow and Multi-Host Configuration

This document explains the setup flow and how to configure Airavata components across multiple hosts.

## `deploy.yml` Execution Flow (Recommended)

The consolidated `deploy.yml` playbook deploys all components using the four essential roles:

```
1. base (on all hosts)
   ↓
2. database (on [database] hosts)
   ↓
3. keycloak (on [keycloak] hosts)
   ↓
4. apiserver (on [apiserver] hosts)
```

### Role Details

#### 1. `base`
**Purpose**: System-level environment setup
- Creates system user/group (`user`, `group` variables)
- Installs Java 25 JDK
- Configures firewall (firewalld/ufw, OS-specific)
- Installs Let's Encrypt certbot for SSL certificates
- Configures automatic security updates
- **Runs on**: All target hosts

#### 2. `database`
**Purpose**: MariaDB installation and database setup
- Installs MariaDB 11.7 (OS-specific packages)
- Security hardening (mysql_secure_installation equivalent)
- Creates unified `airavata` database
- Creates `keycloak` database
- Creates database user (`db_user` with `db_password`)
- Configures firewall for port 3306
- **Runs on**: `[database]` group

#### 3. `keycloak`
**Purpose**: Keycloak 26.5 (Quarkus-based) IAM server
- Installs Apache/httpd reverse proxy
- Generates SSL certificates via Let's Encrypt
- Downloads and installs Keycloak 26.5
- Configures database connection (MariaDB, can be remote)
- Sets admin credentials via environment variables
- Imports realm definition if enabled
- Creates systemd service
- **Runs on**: `[keycloak]` group
- **Requires**: database role, Java, SSL certificates

#### 4. `apiserver`
**Purpose**: Unified Airavata API server deployment
- Checks out Airavata source from Git
- Builds with Maven (`mvn clean install -DskipTests`)
- Sets up keystore file
- Deploys distribution to target directory
- Configures HAProxy for SSL termination (port 443 → 8090)
- Configures firewall (port 443 HTTPS, 8090 HTTP, 9090 gRPC)
- Creates systemd service
- **Runs on**: `[apiserver]` group

## Legacy `airavata_setup.yml` Flow

The legacy setup playbook runs additional roles that may not be needed for new deployments:

```
1. env_setup → 2. java → 3. common → 4. database → 5. letsencrypt
   → 6. keycloak → 7. reverse_proxy → 8. api-orch → 9. airavata_services
```

**Note:** The legacy setup references Zookeeper, Kafka, and RabbitMQ roles that are **no longer required**. The current architecture uses:
- **Temporal** for workflow orchestration (provided externally or via Docker)
- **In-process event delivery** for status changes (no messaging broker needed)

## Multi-Host Configuration

### Supported Separations

You can separate:
- **Database** → Separate host (MariaDB)
- **Keycloak** → Separate host (IAM)
- **Temporal** → Separate host (workflow engine, typically Docker)
- **Airavata API Server** → One host (all services together in single JVM)

### Example: Three-Host Setup

**Host 1**: Database Server (MariaDB only)
**Host 2**: Keycloak Server (Keycloak + Apache2 reverse proxy)
**Host 3**: Airavata Server (unified API server)

Temporal typically runs as a Docker container on any of the hosts or as a managed service.

## Configuration Steps

### Step 1: Update Inventory (`inventories/my-env/hosts`)

```ini
# Database server (separate host)
[database]
db-server ansible_host=<DB_IP> ansible_user=exouser

# Keycloak server (separate host)
[keycloak]
keycloak-server ansible_host=<KEYCLOAK_IP> ansible_user=exouser

# Airavata API server
[apiserver]
airavata-server ansible_host=<AIRAVATA_IP> ansible_user=exouser
```

### Step 2: Database Server Configuration

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

Create `inventories/my-env/group_vars/keycloak/vars.yml`:

```yaml
---
# Keycloak host-specific variables
keycloak_vhost_servername: "auth.airavata.apache.org"
letsencrypt_email: "admin@airavata.org"

# Keycloak database connection (points to database server)
keycloak_db_host: "<DB_IP>"
keycloak_db_port: "3306"
keycloak_db_schema_name: "keycloak"
```

Create `inventories/my-env/group_vars/keycloak/vault.yml`:

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

Update `inventories/my-env/group_vars/all/vars.yml`:

```yaml
---
airavata_version: "0.21-SNAPSHOT"
git_branch: "master"
deploy_user: "exouser"
deployment_dir: "/home/{{ deploy_user }}/airavata-deployment"
```

Update `inventories/my-env/group_vars/all/vault.yml`:

```yaml
---
# Unified database URL
datasource_url: "jdbc:mariadb://<DB_IP>:3306/airavata"
db_password: "CHANGEME_DB_PASSWORD"

# IAM credentials (Keycloak) - points to Keycloak server
iam_server_url: "https://auth.airavata.apache.org"
iam_admin_username: "admin"
iam_admin_password: "CHANGEME_IAM_PASSWORD"

# Temporal connection
temporal_target: "<TEMPORAL_HOST>:7233"
```

### Step 5: Running Setup

**Option A: Deploy everything at once**

```bash
ansible-playbook -i inventories/my-env deploy.yml --ask-vault-pass
```

**Option B: Deploy components separately**

```bash
# 1. Deploy database first
ansible-playbook -i inventories/my-env deploy.yml \
  --limit database \
  --tags database \
  --ask-vault-pass

# 2. Deploy Keycloak
ansible-playbook -i inventories/my-env deploy.yml \
  --limit keycloak \
  --tags keycloak \
  --ask-vault-pass

# 3. Deploy Airavata API server
ansible-playbook -i inventories/my-env deploy.yml \
  --limit apiserver \
  --tags apiserver \
  --ask-vault-pass
```

## Network Requirements

### Database Server
- **Port 3306**: Must be accessible from Keycloak and Airavata servers
- **Firewall**: Allow MySQL connections from Keycloak and Airavata IPs

### Keycloak Server
- **Port 443 (HTTPS)**: Must be accessible from internet
- **Port 80 (HTTP)**: For Let's Encrypt certificate verification
- **Outbound**: Must connect to database server (port 3306)
- **DNS**: `keycloak_vhost_servername` must resolve to Keycloak server IP

### Airavata Server
- **Port 8090**: HTTP API server (or via HAProxy on 443)
- **Port 9090**: gRPC server (agent streams)
- **Port 7233**: Temporal connection (outbound to Temporal server)
- **Outbound**: Must connect to:
  - Database server (port 3306)
  - Keycloak server (HTTPS)
  - Temporal server (port 7233)

## Running Setup

### Full Setup (All on Same Host)
```bash
ansible-playbook -i inventories/my-env deploy.yml --ask-vault-pass
```

### Updating Existing Deployment
```bash
ansible-playbook -i inventories/my-env airavata_update.yml --ask-vault-pass
```

### Multi-Host Setup (Separate Hosts)
```bash
# Deploy in order: Database → Keycloak → API Server
ansible-playbook -i inventories/my-env deploy.yml \
  --limit database --tags database --ask-vault-pass

ansible-playbook -i inventories/my-env deploy.yml \
  --limit keycloak --tags keycloak --ask-vault-pass

ansible-playbook -i inventories/my-env deploy.yml \
  --limit apiserver --tags apiserver --ask-vault-pass
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
   - Verify MariaDB driver exists in Keycloak providers directory
   - Check environment variables: `systemctl show keycloak | grep KC_DB`

3. **Airavata server fails to start**
   - Check database connection in configuration
   - Check service logs: `tail -f /opt/apache-airavata/logs/airavata.log`
   - Verify `iam_server_url` points to Keycloak server
   - Verify Temporal is accessible on port 7233
   - Verify Java 25: `java --version`
   - Use `start_services.yml` / `stop_services.yml` to manage

4. **SSL certificate generation fails**
   - Verify DNS points to server: `dig <hostname>`
   - Check port 80/443 are open: `ss -tuln | grep -E '80|443'`
   - Verify email is valid in `letsencrypt_email`

5. **Network connectivity issues**
   - Test database connection: `mysql -h <DB_IP> -u root -p`
   - Test Keycloak connection: `curl https://<keycloak_host>/health`
   - Test Temporal connection: `temporal operator cluster health --address <temporal_host>:7233`
   - Check firewall rules: `firewall-cmd --list-all` or `ufw status`

## Next Steps After Setup

1. **Verify services are running**:
   ```bash
   # Database server
   ansible database -i inventories/my-env -m shell \
     -a "systemctl status mariadb"

   # Keycloak server
   ansible keycloak -i inventories/my-env -m shell \
     -a "systemctl status keycloak"

   # Airavata server
   ansible apiserver -i inventories/my-env -m shell \
     -a "systemctl status apiserver"
   ```

2. **Check Airavata API**:
   ```bash
   curl http://<apiserver_host>:8090/api/v1/
   ```

3. **Update services**:
   ```bash
   ansible-playbook -i inventories/my-env airavata_update.yml \
     --limit apiserver \
     --ask-vault-pass
   ```

4. **Start/Stop server**:
   ```bash
   ansible-playbook -i inventories/my-env start_services.yml --ask-vault-pass
   ansible-playbook -i inventories/my-env stop_services.yml --ask-vault-pass
   ```
