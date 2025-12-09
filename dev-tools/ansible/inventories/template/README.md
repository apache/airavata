# Airavata Deployment Inventory Template

This directory contains a template for creating new Airavata deployment inventories.

## Quick Start for New Environment

### 1. Copy this template

```bash
cp -r inventories/template inventories/my-env
cd inventories/my-env
```

### 2. Rename example files

```bash
mv hosts.example hosts
mv group_vars/all/vars.yml.example group_vars/all/vars.yml
mv group_vars/all/vault.yml.example group_vars/all/vault.yml
mv host_vars/airavata-server/vault.yml.example host_vars/airavata-server/vault.yml
```

### 3. Edit configuration files

Edit all files and replace `CHANGEME` values with your actual values:

- **hosts** - Replace `airavata-server` with your host alias if needed
- **group_vars/all/vars.yml** - Set non-sensitive configuration values
- **group_vars/all/vault.yml** - Set sensitive values (passwords, URLs, etc.)
- **host_vars/airavata-server/vault.yml** - Set server IP addresses and SSH credentials

### 4. Encrypt sensitive files

Encrypt the vault files to protect sensitive information:

```bash
cd ../my-env

# Encrypt group variables (database passwords, API keys, etc.)
ansible-vault encrypt group_vars/all/vault.yml

# Encrypt host variables (server IPs, SSH keys)
ansible-vault encrypt host_vars/airavata-server/vault.yml
```

### 5. Test connection

Verify you can connect to your server:

```bash
ansible-playbook -i inventories/my-env --list-hosts -m ping --ask-vault-pass
```

### 6. Deploy

**For initial setup (full environment from scratch):**
```bash
cd ../..
ansible-playbook -i inventories/my-env airavata_setup.yml --ask-vault-pass
```

**For service updates (infrastructure already exists):**
```bash
cd ../..
ansible-playbook -i inventories/my-env airavata_update.yml --ask-vault-pass
```

## File Structure

```
my-env/
├── hosts                               # Host definitions
├── group_vars/
│   └── all/
│       ├── vars.yml                    # Non-sensitive variables
│       └── vault.yml                   # Encrypted sensitive variables
└── host_vars/
    └── airavata-server/
        └── vault.yml                   # Encrypted server-specific variables
```

## Key Configuration Points

### Server Access (host_vars/airavata-server/vault.yml)
- `ansible_host` - Server IP address or hostname
- `ansible_user` - SSH user for deployment
- `ansible_ssh_private_key_file` - Path to SSH private key

### Database Configuration (group_vars/all/vault.yml)
- All database passwords
- Database URLs and connection strings
- Server IP addresses embedded in URLs

### Service Configuration (group_vars/all/vault.yml)
- IAM/Keycloak credentials
- OAuth client secrets
- RabbitMQ connection strings
- Email monitoring credentials
- Tunnel server tokens
- Keystore passwords

### Non-Sensitive Configuration (group_vars/all/vars.yml)
- Service ports
- Build settings (git repository, branch, version)
- Paths and directories

## Managing Vault Files

**View an encrypted file:**
```bash
ansible-vault view group_vars/all/vault.yml
```

**Edit an encrypted file:**
```bash
ansible-vault edit group_vars/all/vault.yml
```

**Change vault password:**
```bash
ansible-vault rekey group_vars/all/vault.yml
```

## Troubleshooting

**Issue: Playbook asks for vault password repeatedly**
- Check that all vault files are encrypted
- Verify the inventory directory path is correct

**Issue: Connection refused**
- Verify `ansible_host` in host_vars is correct
- Check SSH key file path and permissions
- Ensure target server is accessible from your machine

**Issue: Services don't start**
- Check logs in `deployment_dir/logs/`
- Verify all required ports are open
- Ensure database connectivity