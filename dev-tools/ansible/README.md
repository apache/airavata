# Apache Airavata Ansible Deployment

Ansible playbooks for deploying and managing Apache Airavata and its dependencies.

## Quick Start

### Prerequisites

- Python 3.8+
- SSH access to target servers with sudo privileges
- DNS configured (for Let's Encrypt SSL certificates)

### Installation

```bash
cd dev-tools/ansible

# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

## Main Playbooks

| Playbook | Purpose |
|----------|---------|
| `airavata_setup.yml` | Complete initial setup from scratch |
| `airavata_update.yml` | Update existing deployment (rebuilds and redeploys) |
| `start_services.yml` | Start all Airavata services |
| `stop_services.yml` | Stop all Airavata services |

## Common Operations

### Updating Existing Deployments

#### Development Server
```bash
cd dev-tools/ansible
source venv/bin/activate

# Update services (stops, rebuilds, redeploys, starts)
ansible-playbook -i inventories/dev airavata_update.yml --ask-vault-pass

# Or use vault password file
ansible-playbook -i inventories/dev airavata_update.yml --vault-pass-file=./vault-password.txt
```

#### Production Server
```bash
cd dev-tools/ansible
source venv/bin/activate

# Update services (stops, rebuilds, redeploys, starts)
ansible-playbook -i inventories/prod airavata_update.yml --ask-vault-pass

# Or use vault password file
ansible-playbook -i inventories/prod airavata_update.yml --vault-pass-file=./vault-password.txt
```

### Starting/Stopping Services

```bash
# Start all services
ansible-playbook -i inventories/dev start_services.yml --ask-vault-pass

# Stop all services
ansible-playbook -i inventories/dev stop_services.yml --ask-vault-pass
```

### Initial Setup (First Time)

For setting up a new deployment from scratch, see [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md#setting-up-a-new-environment).

## Setup Flow Overview

The `airavata_setup.yml` playbook executes roles in the following order:

```
1. env_setup            → System user, firewall, basic requirements
2. java                 → Java installation
3. common               → Maven, Git, Airavata source checkout
4. zookeeper            → Zookeeper installation
5. kafka                → Kafka installation
6. rabbitmq             → RabbitMQ installation
7. database             → MariaDB installation and database setup
8. letsencrypt          → SSL certificate generation
9. keycloak             → Keycloak IAM server (24.0.0+ uses Quarkus)
10. reverse_proxy       → Apache2 reverse proxy for Keycloak
11. api-orch            → HAProxy for API server SSL termination
12. airavata_services   → Build and deploy all Airavata services
```

**For detailed information about each role, dependencies, and multi-host configuration, see [SETUP_FLOW.md](SETUP_FLOW.md).**

## Setting Up a New Deployment

If you need to spin up a new deployment (new environment, new server, etc.), follow the [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md#setting-up-a-new-environment).

### Quick Summary

1. **Copy template inventory:**
   ```bash
   cp -r inventories/template inventories/my-env
   ```
   
2. **Edit configuration files:**
   - `inventories/my-env/hosts` - Server IPs and host groups
   - `inventories/my-env/group_vars/all/vars.yml` - Non-sensitive variables (ports, paths, versions)
   - `inventories/my-env/group_vars/all/vault.yml` - **Sensitive variables** (passwords, API keys, database URLs)
   - `inventories/my-env/host_vars/<hostname>/vault.yml` - Host-specific sensitive variables (SSH credentials)


3. **Key properties to change in `vault.yml`:**
   - All `CHANGEME_*` values (database passwords, IAM passwords, OAuth secrets, etc.)
   - Database hostnames/IPs in JDBC URLs
   - Keycloak server URL (`iam_server_url`)
   - Keycloak admin password (`keycloak_master_account_password`)
   - Keycloak database password (`keycloak_db_password`)
   - Keycloak client secrets (`keycloak_pga_client_secret`, `keycloak_jupyterlab_client_secret`, `keycloak_cilogon_client_secret`)
   - Keycloak redirect URIs (update hostnames for your environment)
   - Email credentials
   - Keystore passwords


4. **Encrypt vault files:**
   ```bash
   ansible-vault encrypt inventories/my-env/group_vars/all/vault.yml
   ansible-vault encrypt inventories/my-env/host_vars/<hostname>/vault.yml
   ```
   
5. **Run setup:**
   ```bash
   ansible-playbook -i inventories/my-env airavata_setup.yml --ask-vault-pass
   ```

## Supported Operating Systems

- **Ubuntu**: 20.04, 22.04, 24.04
- **CentOS**: 7
- **Rocky Linux**: 8

## Documentation

- **[DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md)** - Complete guide for developers:
  - Setting up new environments
  - Vault file management (encrypt, decrypt, edit)
  - Service management (start, stop, update)
  - Configuration file locations
  - Troubleshooting common issues


- **[SETUP_FLOW.md](SETUP_FLOW.md)** - Detailed technical documentation:
  - Role execution order and dependencies
  - Role-by-role breakdown
  - Multi-host deployment configuration
  - Network requirements
  - Variable reference

## Key Roles

- **env_setup** - System user, firewall, basic requirements
- **java** - Java installation (OpenJDK)
- **common** - Maven, Git, Airavata source checkout
- **zookeeper** - Zookeeper installation
- **kafka** - Kafka installation
- **rabbitmq** - RabbitMQ installation (uses distro packages)
- **database** - MariaDB installation and database setup
- **letsencrypt** - SSL certificate generation (Let's Encrypt)
- **keycloak** - Keycloak IAM server (24.0.0+ uses Quarkus with MariaDB driver)
- **reverse_proxy** - Apache2 reverse proxy for Keycloak
- **api-orch** - HAProxy for API server SSL termination
- **airavata_services** - Build and deploy all Airavata services

## Troubleshooting

See [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md#troubleshooting) for common issues and solutions.

## Legacy Playbooks

The following playbooks are still available but deprecated in favor of `airavata_setup.yml`:
- `database.yml` - Database setup only
- `airavata.yml` - Airavata services only
- `keycloak.yml` - Keycloak setup only
- `site.yml` - Master playbook (includes all above)
