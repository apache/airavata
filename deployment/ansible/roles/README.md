# Ansible Roles

This directory contains Ansible roles for deploying Airavata components.

## Role Structure

Each role follows standard Ansible structure with tasks organized into logical files:

```
role_name/
  defaults/
    main.yml          # Default variables
  tasks/
    main.yml          # Entry point (includes other task files)
    task1.yml         # Logical grouping of related tasks
    task2.yml         # Another logical grouping
  templates/
    *.j2              # Jinja2 templates
  handlers/
    main.yml          # Handlers (restart services, etc.)
  vars/
    main.yml          # Role-specific variables (if needed)
  files/
    *.conf            # Static configuration files
```

## Essential Roles

### base

Consolidated role providing foundational setup for all components:
- User/group creation
- Java 25 JDK installation
- Firewall configuration (OS-specific)
- Let's Encrypt certbot installation
- Automatic security updates configuration

**Task files:**
- `user.yml`: User and group creation
- `firewall.yml`: Firewall setup
- `java.yml`: Java 25 installation
- `certbot.yml`: Let's Encrypt certbot setup
- `auto-updates.yml`: Security update configuration

### database

MariaDB installation and database setup:
- MariaDB installation (OS-specific packages)
- Database security hardening
- Database and user creation
- Firewall port configuration

**Task files:**
- `install.yml`: Package installation and SELinux setup
- `secure.yml`: Security hardening (mysql_secure_installation equivalent)
- `users.yml`: Database and user creation
- `firewall.yml`: Port access configuration

### apiserver

Airavata API server deployment (includes build and deployment):
- Source code checkout
- Maven build
- Keystore setup
- Distribution deployment
- SSL certificate management
- HAProxy configuration
- Firewall port configuration

**Task files:**
- `build.yml`: Git checkout and Maven build
- `keystore.yml`: Keystore file setup
- `deploy.yml`: Distribution deployment and service setup
- `ssl.yml`: SSL certificates and HAProxy
- `firewall.yml`: Port access configuration

### keycloak

Keycloak 26.5 (Quarkus-based) IAM server deployment:
- Web server (Apache/httpd) setup
- SSL certificate management
- Keycloak installation and configuration
- Systemd service setup

**Task files:**
- `webserver.yml`: Apache/httpd reverse proxy setup
- `ssl.yml`: SSL certificate generation
- `install.yml`: Keycloak download, configuration, and service setup

## Role Usage

Roles are used in the main deployment playbook (`deploy.yml`):

```yaml
- name: API Server
  hosts: apiserver
  roles:
    - base
    - apiserver
```

## OS-Specific Handling

All roles use variable maps in `defaults/main.yml` to handle OS differences:

```yaml
os_packages:
  RedHat:
    java: "java-25-openjdk-devel"
  Debian:
    java: "openjdk-25-jdk"
```

This eliminates scattered conditional logic and makes OS support easier to maintain.
