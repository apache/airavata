# Airavata Deployment - Developer Quick Guide

## Prerequisites

- Ansible installed (or use `venv` in `dev-tools/ansible/`)
- SSH access to target server with sudo privileges
- Vault password (if using encrypted vault files)

## Setting Up a New Environment

### Step 1: Copy Template

```bash
cd dev-tools/ansible
cp -r inventories/template inventories/my-env
cd inventories/my-env
```

### Step 2: Rename Example Files

```bash
mv hosts.example hosts
mv group_vars/all/vars.yml.example group_vars/all/vars.yml
mv group_vars/all/vault.yml.example group_vars/all/vault.yml
mv host_vars/airavata-server/vault.yml.example host_vars/airavata-server/vault.yml
```

### Step 3: Edit Configuration Files

**Edit `hosts`**
- Replace `airavata-server` with your host alias if needed

**Edit `group_vars/all/vars.yml` (Non-sensitive):**
- Set deployment user, ports, paths
- Set git branch, version
- Replace any `CHANGEME` values

**Edit `group_vars/all/vault.yml` (Sensitive):**
- Replace all `CHANGEME_*` values:
  - Database passwords (`CHANGEME_DB_PASSWORD`)
  - Database host (`CHANGEME_DB_HOST` in JDBC URLs)
  - IAM credentials (`CHANGEME_IAM_PASSWORD`)
  - Keycloak admin password (`CHANGEME_KEYCLOAK_ADMIN_PASSWORD`)
  - Keycloak database password (`CHANGEME_KEYCLOAK_DB_PASSWORD`)
  - Keycloak client secrets:
    - `CHANGEME_PGA_CLIENT_SECRET` - PGA (Gateway) client secret
    - `CHANGEME_JUPYTERLAB_CLIENT_SECRET` - JupyterLab client secret
    - `CHANGEME_CILOGON_CLIENT_SECRET` - CILogon identity provider secret
  - Keycloak client IDs:
    - `CHANGEME_CILOGON_CLIENT_ID` - CILogon client ID
  - Keycloak redirect URIs (update `CHANGEME_GATEWAY_HOST` and `CHANGEME_JUPYTERLAB_HOST`)
  - OAuth secrets (`CHANGEME_OAUTH_SECRET`)
  - Keystore passwords (`CHANGEME_KEYSTORE_PASSWORD`)
  - Email passwords (`CHANGEME_EMAIL_PASSWORD`)
  - Tunnel tokens (`CHANGEME_TUNNEL_TOKEN`)

**Edit `host_vars/airavata-server/vault.yml` (Sensitive):**
- Set `ansible_host`: Server IP address
- Set `ansible_user`: SSH user for deployment

### Step 4: Encrypt Vault Files

```bash
# Encrypt group variables (database passwords, API keys, etc.)
ansible-vault encrypt group_vars/all/vault.yml

# Encrypt host variables (server IPs, SSH credentials)
ansible-vault encrypt host_vars/airavata-server/vault.yml
```

**Note:** You'll be prompted to set a vault password. Remember this password - you'll need it for all playbook runs.

### Step 5: Test Connection

```bash
cd ../..
ansible -i inventories/my-env airavata_servers -m ping --ask-vault-pass
```

## Quick Start

### 1. Initial Setup (First Time)

```bash
cd dev-tools/ansible

# Activate virtual environment
source venv/bin/activate

# Run full setup
ansible-playbook -i inventories/my-env airavata_setup.yml --ask-vault-pass
```

**What this does:**
- Creates `airavata` user/group
- Installs Java, Maven, Git
- Sets up Zookeeper, Kafka, RabbitMQ, MariaDB
- Installs Keycloak (IAM)
- Builds and deploys all Airavata services
- Starts all services

### 2. Update Existing Deployment

```bash
cd dev-tools/ansible
source venv/bin/activate

# Update services (stops, builds, deploys, starts)
ansible-playbook -i inventories/my-env airavata_update.yml --ask-vault-pass
```

## Vault File Management

### Encrypt Vault Files

```bash
cd dev-tools/ansible

# Encrypt vault files (will prompt for password)
ansible-vault encrypt inventories/my-env/group_vars/all/vault.yml
ansible-vault encrypt inventories/my-env/host_vars/dev-server/vault.yml
```

**Important:** Set a strong password and store it securely. You'll need it every time you run playbooks.

### Decrypt Vault Files

```bash
ansible-vault decrypt inventories/my-env/group_vars/all/vault.yml
```

**Note:** Decrypting removes encryption. Re-encrypt after editing.

### Edit Encrypted Vault (Recommended)

```bash
# Opens editor, decrypts temporarily, re-encrypts on save
ansible-vault edit inventories/my-env/group_vars/all/vault.yml
```

**Best Practice:** Use `ansible-vault edit` instead of decrypt/edit/encrypt to avoid leaving unencrypted files.

### View Encrypted Vault (Without Decrypting)

```bash
ansible-vault view inventories/my-env/group_vars/all/vault.yml
```

### Change Vault Password

```bash
ansible-vault rekey inventories/my-env/group_vars/all/vault.yml
# Enter old password, then new password
```

## Service Management

### Stop All Services

```bash
cd dev-tools/ansible
ansible-playbook -i inventories/my-env stop_services.yml --ask-vault-pass
```

**What this does:**
- Stops all Airavata services gracefully
- Checks for processes holding ports
- Kills processes if needed (SIGTERM, then SIGKILL)
- Verifies all ports are free

### Start All Services

```bash
cd dev-tools/ansible
ansible-playbook -i inventories/my-env start_services.yml --ask-vault-pass
```

**What this does:**
- Starts all Airavata services
- Waits for services to be ready
- Verifies ports are listening

## Configuration Files

### Key Files to Edit

1. **`inventories/my-env/group_vars/all/vars.yml`** (Non-sensitive)
   - Deployment user, ports, paths
   - Git branch, version

2. **`inventories/my-env/group_vars/all/vault.yml`** (Sensitive - encrypted)
   - Database passwords, URLs
   - IAM/Keycloak credentials (admin password, database password)
   - Keycloak realm client configuration (PGA, JupyterLab, CILogon secrets and redirect URIs)
   - OAuth secrets
   - Keystore passwords

3. **`inventories/my-env/host_vars/airavata-server/vault.yml`** (Sensitive - encrypted)
   - Server connection details
   - `ansible_host`, `ansible_user`

### Important Variables

| Variable | File | Description |
|---------|------|-------------|
| `deploy_user` | `vars.yml` | User for deployment (`airavata`) |
| `user`, `group` | `vars.yml` | System user/group (`airavata`) |
| `db_password` | `vault.yml` | Database password for all services |
| `iam_server_url` | `vault.yml` | Keycloak URL |
| `keycloak_master_account_password` | `vault.yml` | Keycloak admin password |
| `keycloak_db_password` | `vault.yml` | Keycloak database password |
| `keycloak_pga_client_secret` | `vault.yml` | PGA (Gateway) OAuth client secret |
| `keycloak_pga_redirect_uris` | `vault.yml` | PGA redirect URIs (list) |
| `keycloak_jupyterlab_client_secret` | `vault.yml` | JupyterLab OAuth client secret |
| `keycloak_cilogon_client_id` | `vault.yml` | CILogon identity provider client ID |
| `keycloak_cilogon_client_secret` | `vault.yml` | CILogon identity provider secret |
| `rabbitmq_broker_url` | `vault.yml` | RabbitMQ connection string |
| `*_jdbc_url` | `vault.yml` | Database URLs (use `localhost` or DB server IP) |

### Keystore Management

**Automatic Generation (Default):**
The keystore file (`airavata.sym.p12`) is automatically generated from Let's Encrypt certificates during deployment. No manual action needed.

**Manual Keystore (Optional):**
If you need to use a custom keystore file:
1. Place it in `inventories/my-env/files/airavata.sym.p12`
2. Optionally encrypt it: `ansible-vault encrypt inventories/my-env/files/airavata.sym.p12`
3. The playbook will use your file instead of auto-generating

## Common Workflows

### Fresh Deployment

```bash
# 1. Configure inventory files
#    - Edit inventories/my-env/group_vars/all/vars.yml
#    - Edit inventories/my-env/group_vars/all/vault.yml (decrypt first if encrypted)

# 2. Encrypt vault files
ansible-vault encrypt inventories/my-env/group_vars/all/vault.yml
ansible-vault encrypt inventories/my-env/host_vars/airavata-server/vault.yml

# 3. Run setup
ansible-playbook -i inventories/my-env airavata_setup.yml --ask-vault-pass
```

### Update Code and Redeploy

```bash
# 1. Update code (if needed, change git_branch in vars.yml)

# 2. Run update (stops, builds, deploys, starts)
ansible-playbook -i inventories/my-env airavata_update.yml --ask-vault-pass
```

### Change Configuration

```bash
# 1. Edit encrypted vault
ansible-vault edit inventories/my-env/group_vars/all/vault.yml

# 2. Stop services
ansible-playbook -i inventories/my-env stop_services.yml --ask-vault-pass

# 3. Update deployment (will regenerate configs)
ansible-playbook -i inventories/my-env airavata_update.yml --ask-vault-pass --skip-tags build
```

### Restart Services Only

```bash
# Stop
ansible-playbook -i inventories/my-env stop_services.yml --ask-vault-pass

# Start
ansible-playbook -i inventories/my-env start_services.yml --ask-vault-pass
```

## Verification

### Check Services Are Running

```bash
# Check ports
ansible airavata-server -i inventories/my-env -m shell \
  -a "ss -tuln | grep -E '8930|8940|8962|8970|8960|7878|18880|18899|8050|8082'"

# Check processes
ansible airavata-server -i inventories/my-env -m shell \
  -a "ps aux | grep java | grep airavata | wc -l"
```

### Check Service Logs

```bash
# SSH to server
ssh <ansible_user>@<server_ip>

# View logs
tail -f /home/airavata/<airavata-deployment>/apache-airavata-api-server-*/logs/*.log
```

### Check Third-Party Services

```bash
ansible airavata-server -i inventories/my-env -m shell \
  -a "systemctl status zookeeper kafka rabbitmq-server mariadb keycloak"
```

## Troubleshooting

### Services Won't Start

1. **Check ports are free:**
   ```bash
   ansible-playbook -i inventories/my-env stop_services.yml --ask-vault-pass
   ```

2. **Check database connection:**
   - Verify `*_jdbc_url` in vault.yml point to correct database
   - Test: `mysql -h <db_host> -u <user> -p`

3. **Check RabbitMQ:**
   - Verify vhost exists: `rabbitmqctl list_vhosts`
   - Verify user has permissions

4. **Check logs:**
   ```bash
   ssh <server>
   tail -f /home/airavata/<airavata-deployment>/apache-airavata-api-server-*/logs/*.log
   ```

## Quick Reference

| Task | Command |
|------|---------|
| Full setup | `ansible-playbook -i inventories/my-env airavata_setup.yml --ask-vault-pass` |
| Update services | `ansible-playbook -i inventories/my-env airavata_update.yml --ask-vault-pass` |
| Stop services | `ansible-playbook -i inventories/my-env stop_services.yml --ask-vault-pass` |
| Start services | `ansible-playbook -i inventories/my-env start_services.yml --ask-vault-pass` |
| Encrypt vault | `ansible-vault encrypt inventories/my-env/group_vars/all/vault.yml` |
| Edit vault | `ansible-vault edit inventories/my-env/group_vars/all/vault.yml` |
| Decrypt vault | `ansible-vault decrypt inventories/my-env/group_vars/all/vault.yml` |

## Additional Resources

- **`inventories/template/README.md`** - Detailed template setup guide
- **`SETUP_FLOW.md`** - Detailed role descriptions and multi-host configuration
- **`inventories/template/group_vars/all/vault.yml.example`** - Template with all `CHANGEME` placeholders