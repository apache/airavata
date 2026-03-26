# Airavata Deployment - Developer Quick Guide

## Prerequisites

- Ansible installed (or use `venv` in `deployment/ansible/`)
- SSH access to target server with sudo privileges
- Vault password (if using encrypted vault files)

## Setting Up a New Environment

### Step 1: Copy Template

```bash
cd deployment/ansible
cp -r inventories/template inventories/my-env
cd inventories/my-env
```

### Step 2: Edit Configuration Files

**Edit `hosts`**
- Replace `CHANGEME` with actual hostnames/IPs for each group (database, apiserver, keycloak, temporal, portal)

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

### 1. Deploy (Recommended)

```bash
cd deployment/ansible

# Activate virtual environment
source ENV/bin/activate

# Deploy everything
ansible-playbook -i inventories/my-env deploy.yml --ask-vault-pass
```

**What this does:**
- Creates `airavata` user/group
- Installs Java 25
- Sets up MariaDB (airavata + keycloak databases)
- Installs Temporal dev server
- Installs Keycloak 26.5 (IAM)
- Builds and deploys the unified Airavata API server (+ agent binary)
- Deploys the Next.js portal
- Configures HAProxy for SSL termination
- Starts all services

### 2. Update Existing Deployment

```bash
cd deployment/ansible
source ENV/bin/activate

# Redeploy API server only (stops, builds, deploys, starts)
ansible-playbook -i inventories/my-env deploy.yml --tags apiserver --ask-vault-pass
```

## Vault File Management

### Encrypt Vault Files

```bash
cd deployment/ansible

# Encrypt vault files (will prompt for password)
ansible-vault encrypt inventories/my-env/group_vars/all/vault.yml
ansible-vault encrypt inventories/my-env/host_vars/airavata-server/vault.yml
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

Services are managed via systemd on the target servers:

```bash
# On the target server (via SSH or ansible ad-hoc)
systemctl start apiserver
systemctl stop apiserver
systemctl restart apiserver
systemctl status apiserver

# Keycloak / Temporal / Portal similarly:
systemctl status keycloak temporal portal
```

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
| `db_password` | `vault.yml` | Database password |
| `iam_server_url` | `vault.yml` | Keycloak URL |
| `keycloak_master_account_password` | `vault.yml` | Keycloak admin password |
| `keycloak_db_password` | `vault.yml` | Keycloak database password |
| `keycloak_pga_client_secret` | `vault.yml` | PGA (Gateway) OAuth client secret |
| `keycloak_pga_redirect_uris` | `vault.yml` | PGA redirect URIs (list) |
| `keycloak_jupyterlab_client_secret` | `vault.yml` | JupyterLab OAuth client secret |
| `keycloak_cilogon_client_id` | `vault.yml` | CILogon identity provider client ID |
| `keycloak_cilogon_client_secret` | `vault.yml` | CILogon identity provider secret |
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

# 3. Deploy
ansible-playbook -i inventories/my-env deploy.yml --ask-vault-pass
```

### Update Code and Redeploy

```bash
# Redeploy API server (stops, builds, deploys, starts)
ansible-playbook -i inventories/my-env deploy.yml --tags apiserver --ask-vault-pass

# Redeploy portal only
ansible-playbook -i inventories/my-env deploy.yml --tags portal --ask-vault-pass
```

### Change Configuration

```bash
# 1. Edit encrypted vault
ansible-vault edit inventories/my-env/group_vars/all/vault.yml

# 2. Redeploy (will regenerate configs and restart)
ansible-playbook -i inventories/my-env deploy.yml --tags apiserver --ask-vault-pass
```

## Verification

### Check Services Are Running

```bash
# Check ports (unified HTTP + gRPC)
ansible airavata-server -i inventories/my-env -m shell \
  -a "ss -tuln | grep -E '8090|9090'"

# Check process
ansible airavata-server -i inventories/my-env -m shell \
  -a "ps aux | grep java | grep airavata | wc -l"
```

### Check Service Logs

```bash
# SSH to server
ssh <ansible_user>@<server_ip>

# View logs
tail -f /opt/apache-airavata/logs/airavata.log
```

### Check Infrastructure Services

```bash
ansible airavata-server -i inventories/my-env -m shell \
  -a "systemctl status mariadb keycloak"
```

## Troubleshooting

### Services Won't Start

1. **Check ports are free:**
   ```bash
   systemctl stop apiserver
   ```

2. **Check database connection:**
   - Verify JDBC URL in vault.yml points to correct database
   - Test: `mysql -h <db_host> -u airavata -p`

3. **Check Temporal:**
   - Verify Temporal is running on port 7233
   - Check `spring.temporal.connection.target` in configuration

4. **Check logs:**
   ```bash
   ssh <server>
   tail -f /opt/apache-airavata/logs/airavata.log
   ```

## Quick Reference

| Task | Command |
|------|---------|
| Deploy all | `ansible-playbook -i inventories/my-env deploy.yml --ask-vault-pass` |
| Deploy API server | `ansible-playbook -i inventories/my-env deploy.yml --tags apiserver --ask-vault-pass` |
| Deploy portal | `ansible-playbook -i inventories/my-env deploy.yml --tags portal --ask-vault-pass` |
| Deploy database | `ansible-playbook -i inventories/my-env deploy.yml --tags database --ask-vault-pass` |
| Deploy keycloak | `ansible-playbook -i inventories/my-env deploy.yml --tags keycloak --ask-vault-pass` |
| Deploy temporal | `ansible-playbook -i inventories/my-env deploy.yml --tags temporal --ask-vault-pass` |
| Encrypt vault | `ansible-vault encrypt inventories/my-env/group_vars/all/vault.yml` |
| Edit vault | `ansible-vault edit inventories/my-env/group_vars/all/vault.yml` |
| Decrypt vault | `ansible-vault decrypt inventories/my-env/group_vars/all/vault.yml` |

## Additional Resources

- **`SETUP_FLOW.md`** - Detailed role descriptions and multi-host configuration
- **`roles/README.md`** - Individual role documentation
- **`inventories/template/`** - Template inventory with `CHANGEME` placeholders
