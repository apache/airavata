# Airavata CLI

A comprehensive command-line interface for Apache Airavata that provides access to all major Airavata services through a unified CLI tool.

## Features

- **Complete API Coverage**: Supports all 10 Airavata services with 300+ methods
- **Device Authentication**: OAuth2 device authorization flow for secure authentication
- **Multiple Output Formats**: Table, JSON, and CSV output formats
- **Configuration Management**: Persistent configuration with automatic token refresh
- **Service Multiplexing**: Uses TMultiplexedProtocol to access all services through a single connection

## Installation

### Prerequisites

- Go 1.21 or later
- Apache Thrift compiler (for building from source)

### Build from Source

```bash
# Clone the repository
git clone https://github.com/apache/airavata.git
cd airavata/cli

# Install dependencies
go mod download

# Generate Thrift client code
make generate-thrift

# Build the CLI
make build

# Install to your PATH
make install
```

### Using Make

```bash
# Check if Thrift is installed
make check-thrift

# Generate Thrift client code
make generate-thrift

# Build the binary
make build

# Install to GOPATH/bin
make install

# Run tests
make test

# Clean generated files
make clean

# Format code
make fmt

# Lint code
make lint
```

## Quick Start

### 1. Authentication

First, authenticate with your Airavata server:

```bash
airavata auth login api.scigap.org:9930
```

This will:
1. Discover the Keycloak configuration for the server
2. Start an OAuth2 device authorization flow
3. Display a user code and verification URL
4. Wait for you to complete authentication in your browser
5. Store the authentication tokens for future use

### 2. Basic Usage

```bash
# Check authentication status
airavata auth status

# List available gateways
airavata gateway list

# List projects
airavata project list --gateway <gateway-id>

# Create a new experiment
airavata experiment create --gateway <gateway-id> --project <project-id> --name "My Experiment"

# Launch an experiment
airavata experiment launch <experiment-id>
```

### 3. Output Formats

```bash
# Table output (default)
airavata gateway list

# JSON output
airavata gateway list --output json

# CSV output
airavata gateway list --output csv
```

## Configuration

The CLI stores configuration in `~/.airavata-cli/config.yaml`:

```yaml
server:
  hostname: api.scigap.org
  port: 9930
  tls: true
auth:
  keycloak_url: https://iam.scigap.org
  realm: airavata
  client_id: airavata-cli
  access_token: <token>
  refresh_token: <token>
  expires_at: <timestamp>
  username: <user>
gateway:
  id: default-gateway
```

## Command Reference

### Authentication Commands

```bash
# Authenticate with a server
airavata auth login <hostname:port>

# Logout and clear stored tokens
airavata auth logout

# Show authentication status
airavata auth status

# Manually refresh token
airavata auth refresh
```

### Gateway Commands

```bash
# Create a gateway
airavata gateway create --name <name> --domain <domain>

# Update a gateway
airavata gateway update <id> --name <name>

# Get gateway details
airavata gateway get <id>

# List all gateways
airavata gateway list

# Delete a gateway
airavata gateway delete <id>

# Check if gateway exists
airavata gateway exists <id>
```

### Project Commands

```bash
# Create a project
airavata project create --gateway <id> --name <name> --owner <user>

# Update a project
airavata project update <id> --name <name>

# Get project details
airavata project get <id>

# List projects
airavata project list --gateway <id> [--user <user>]

# Delete a project
airavata project delete <id>
```

### Experiment Commands

```bash
# Create an experiment
airavata experiment create --gateway <id> --project <id> --name <name>

# Update an experiment
airavata experiment update <id>

# Get experiment details
airavata experiment get <id>

# List experiments
airavata experiment list --gateway <id> [--project <id>] [--user <user>]

# Delete an experiment
airavata experiment delete <id>

# Launch an experiment
airavata experiment launch <id>

# Terminate an experiment
airavata experiment terminate <id>

# Clone an experiment
airavata experiment clone <id> --new-name <name>

# Validate an experiment
airavata experiment validate <id>

# Get experiment status
airavata experiment get-status <id>

# Get experiment outputs
airavata experiment get-outputs <id>
```

### Application Commands

#### Application Modules

```bash
# Create an application module
airavata app module create --gateway <id> --name <name> --version <ver>

# Update an application module
airavata app module update <id>

# Get application module details
airavata app module get <id>

# List application modules
airavata app module list --gateway <id>

# Delete an application module
airavata app module delete <id>
```

#### Application Deployments

```bash
# Create an application deployment
airavata app deployment create --gateway <id> --module <id> --compute <id>

# Update an application deployment
airavata app deployment update <id>

# Get application deployment details
airavata app deployment get <id>

# List application deployments
airavata app deployment list --gateway <id> [--module <id>]

# Delete an application deployment
airavata app deployment delete <id>
```

#### Application Interfaces

```bash
# Create an application interface
airavata app interface create --gateway <id> --name <name>

# Update an application interface
airavata app interface update <id>

# Get application interface details
airavata app interface get <id>

# List application interfaces
airavata app interface list --gateway <id>

# Delete an application interface
airavata app interface delete <id>

# Clone an application interface
airavata app interface clone <id> --new-name <name>
```

### Compute Resource Commands

```bash
# Create a compute resource
airavata compute create --name <name> --host <host>

# Update a compute resource
airavata compute update <id>

# Get compute resource details
airavata compute get <id>

# List compute resources
airavata compute list

# Delete a compute resource
airavata compute delete <id>

# Add job submission interface
airavata compute add-job-submission <id> --type <ssh|local|cloud|unicore>

# Add data movement interface
airavata compute add-data-movement <id> --type <scp|gridftp|unicore|local>

# Add batch queue
airavata compute add-batch-queue <id> --queue-name <name>

# Delete batch queue
airavata compute delete-batch-queue <id> --queue-name <name>
```

### Storage Resource Commands

```bash
# Create a storage resource
airavata storage create --name <name> --host <host>

# Update a storage resource
airavata storage update <id>

# Get storage resource details
airavata storage get <id>

# List storage resources
airavata storage list

# Delete a storage resource
airavata storage delete <id>
```

### Credential Commands

```bash
# Add SSH credential
airavata credential add-ssh --gateway <id> --token <id> --private-key <file>

# Add password credential
airavata credential add-password --gateway <id> --token <id> --username <user> --password <pwd>

# Add certificate credential
airavata credential add-cert --gateway <id> --token <id>

# Get SSH credential
airavata credential get-ssh <token> --gateway <id>

# Get password credential
airavata credential get-password <token> --gateway <id>

# Get certificate credential
airavata credential get-cert <token> --gateway <id>

# List credentials
airavata credential list --gateway <id> --type <ssh|password|cert>

# Delete SSH credential
airavata credential delete-ssh <token> --gateway <id>

# Delete password credential
airavata credential delete-password <token> --gateway <id>
```

### Resource Profile Commands

#### Gateway Resource Profiles

```bash
# Create gateway resource profile
airavata resource-profile gateway create <gateway-id>

# Update gateway resource profile
airavata resource-profile gateway update <gateway-id>

# Get gateway resource profile
airavata resource-profile gateway get <gateway-id>

# Delete gateway resource profile
airavata resource-profile gateway delete <gateway-id>

# Add compute preference
airavata resource-profile gateway add-compute-preference <gateway-id> --compute <id>

# Add storage preference
airavata resource-profile gateway add-storage-preference <gateway-id> --storage <id>
```

#### User Resource Profiles

```bash
# Create user resource profile
airavata resource-profile user create --user <id> --gateway <id>

# Update user resource profile
airavata resource-profile user update --user <id> --gateway <id>

# Get user resource profile
airavata resource-profile user get --user <id> --gateway <id>

# Delete user resource profile
airavata resource-profile user delete --user <id> --gateway <id>

# Add compute preference
airavata resource-profile user add-compute-preference --user <id> --gateway <id> --compute <id>
```

#### Group Resource Profiles

```bash
# Create group resource profile
airavata resource-profile group create --name <name>

# Update group resource profile
airavata resource-profile group update <id>

# Get group resource profile
airavata resource-profile group get <id>

# Delete group resource profile
airavata resource-profile group delete <id>
```

### Workflow Commands

```bash
# Create a workflow
airavata workflow create --name <name> --definition <file>

# Update a workflow
airavata workflow update <id> --definition <file>

# Get workflow details
airavata workflow get <id>

# List workflows
airavata workflow list

# Delete a workflow
airavata workflow delete <id>

# Check if workflow exists
airavata workflow exists --name <name>
```

### Sharing Registry Commands

#### Domain Commands

```bash
# Create a domain
airavata sharing domain create --name <name> --description <desc>

# Update a domain
airavata sharing domain update <id>

# Get domain details
airavata sharing domain get <id>

# List domains
airavata sharing domain list

# Delete a domain
airavata sharing domain delete <id>
```

#### User Commands

```bash
# Create a user
airavata sharing user create --domain <id> --user-id <id> --username <name>

# Update a user
airavata sharing user update --domain <id> --user-id <id>

# Get user details
airavata sharing user get --domain <id> --user-id <id>

# List users
airavata sharing user list --domain <id>

# Delete a user
airavata sharing user delete --domain <id> --user-id <id>
```

#### Group Commands

```bash
# Create a group
airavata sharing group create --domain <id> --name <name>

# Update a group
airavata sharing group update --domain <id> --group-id <id>

# Get group details
airavata sharing group get --domain <id> --group-id <id>

# List groups
airavata sharing group list --domain <id>

# Delete a group
airavata sharing group delete --domain <id> --group-id <id>

# Add users to group
airavata sharing group add-users --domain <id> --group-id <id> --users <id1,id2>

# Remove users from group
airavata sharing group remove-users --domain <id> --group-id <id> --users <id1,id2>
```

#### Entity Commands

```bash
# Create an entity
airavata sharing entity create --domain <id> --entity-id <id> --type <type>

# Share entity with users
airavata sharing entity share --domain <id> --entity-id <id> --users <ids> --permission <id>

# Revoke entity sharing
airavata sharing entity revoke --domain <id> --entity-id <id> --users <ids> --permission <id>
```

#### Permission Commands

```bash
# Create a permission
airavata sharing permission create --domain <id> --name <name>
```

### Orchestrator Commands

```bash
# Launch an experiment
airavata orchestrator launch-experiment <experiment-id> --gateway <id>

# Launch a process
airavata orchestrator launch-process <process-id> --gateway <id> --token <cred-token>

# Validate an experiment
airavata orchestrator validate-experiment <experiment-id>

# Validate a process
airavata orchestrator validate-process <experiment-id>

# Terminate an experiment
airavata orchestrator terminate-experiment <experiment-id> --gateway <id>
```

### User Profile Commands

```bash
# Initialize user profile from IAM
airavata user-profile init

# Update user profile
airavata user-profile update --first-name <name> --last-name <name>

# Get user profile
airavata user-profile get <user-id> --gateway <id>

# List user profiles
airavata user-profile list --gateway <id> [--offset 0] [--limit 50]

# Delete user profile
airavata user-profile delete <user-id> --gateway <id>

# Check if user profile exists
airavata user-profile exists <user-id> --gateway <id>
```

### Tenant Profile Commands

```bash
# Add a gateway
airavata tenant add-gateway --name <name> --domain <domain>

# Update a gateway
airavata tenant update-gateway <id>

# Get gateway details
airavata tenant get-gateway <id>

# List all gateways
airavata tenant list-gateways

# Delete a gateway
airavata tenant delete-gateway <id>

# Check if gateway exists
airavata tenant gateway-exists <id>
```

### IAM Admin Commands

```bash
# Set up a gateway
airavata iam-admin setup-gateway --name <name> --domain <domain>

# Register a new user
airavata iam-admin register-user --username <user> --email <email> --first-name <fn> --last-name <ln> --password <pwd>

# Get user details
airavata iam-admin get-user <username>

# List users
airavata iam-admin list-users [--offset 0] [--limit 50] [--search <query>]

# Enable a user
airavata iam-admin enable-user <username>

# Disable a user
airavata iam-admin disable-user <username>

# Delete a user
airavata iam-admin delete-user <username>

# Reset user password
airavata iam-admin reset-password <username> --new-password <pwd>

# Add role to user
airavata iam-admin add-role <username> --role <role-name>

# Remove role from user
airavata iam-admin remove-role <username> --role <role-name>

# List users with role
airavata iam-admin list-users-with-role <role-name>

# Check if username is available
airavata iam-admin username-available <username>

# Check if user exists
airavata iam-admin user-exists <username>
```

### Group Manager Commands

```bash
# Create a group
airavata group-manager create --name <name> --description <desc>

# Update a group
airavata group-manager update <group-id> --name <name>

# Get group details
airavata group-manager get <group-id>

# List groups
airavata group-manager list

# Delete a group
airavata group-manager delete <group-id> --owner <owner-id>

# Add users to group
airavata group-manager add-users <group-id> --users <id1,id2,...>

# Remove users from group
airavata group-manager remove-users <group-id> --users <id1,id2,...>

# Transfer group ownership
airavata group-manager transfer-ownership <group-id> --new-owner <owner-id>

# Add admins to group
airavata group-manager add-admins <group-id> --admins <id1,id2,...>

# Remove admins from group
airavata group-manager remove-admins <group-id> --admins <id1,id2,...>

# List groups for user
airavata group-manager list-user-groups <username>
```

## Global Options

```bash
# Output format (table, json, csv)
--output, -o string

# Suppress output except errors
--quiet, -q

# Verbose output
--verbose, -v

# Show help
--help, -h

# Show version
--version
```

## Examples

### Complete Workflow Example

```bash
# 1. Authenticate
airavata auth login api.scigap.org:9930

# 2. List available gateways
airavata gateway list

# 3. Create a project
airavata project create --gateway <gateway-id> --name "My Research Project" --owner <username>

# 4. List compute resources
airavata compute list

# 5. Create an experiment
airavata experiment create --gateway <gateway-id> --project <project-id> --name "My Experiment"

# 6. Launch the experiment
airavata experiment launch <experiment-id>

# 7. Check experiment status
airavata experiment get-status <experiment-id>

# 8. Get experiment outputs
airavata experiment get-outputs <experiment-id>
```

### Batch Operations

```bash
# List all experiments in JSON format
airavata experiment list --gateway <gateway-id> --output json

# Export project list to CSV
airavata project list --gateway <gateway-id> --output csv > projects.csv

# Get detailed experiment information
airavata experiment get <experiment-id> --output json | jq '.'
```

## Development

### Project Structure

```
cli/
├── cmd/airavata/          # Main CLI entry point
├── pkg/
│   ├── auth/              # Authentication (OAuth2 device flow)
│   ├── client/             # Thrift client management
│   ├── config/             # Configuration management
│   ├── output/             # Output formatting (table/JSON/CSV)
│   └── commands/           # CLI command implementations
├── gen-go/                 # Generated Thrift client code
├── Makefile               # Build automation
└── README.md              # This file
```

### Adding New Commands

1. Create a new command file in `pkg/commands/`
2. Implement the command structure using Cobra
3. Add the command to the root command in `pkg/commands/root.go`
4. Implement the actual Thrift client calls
5. Add tests for the new command

### Regenerating Thrift Client

```bash
# Generate Go client from Thrift definitions
make generate-thrift
```

This will:
1. Use the Apache Thrift compiler
2. Generate Go client code from `airavata_service.thrift`
3. Place generated code in `gen-go/` directory

### Testing

```bash
# Run all tests
make test

# Run specific package tests
go test ./pkg/auth/...

# Run with coverage
go test -cover ./...
```

## Troubleshooting

### Authentication Issues

```bash
# Check authentication status
airavata auth status

# Refresh token if expired
airavata auth refresh

# Re-authenticate if needed
airavata auth logout
airavata auth login <hostname:port>
```

### Connection Issues

- Ensure the Airavata server is running and accessible
- Check that the hostname:port format is correct
- Verify network connectivity to the server
- Check if TLS is required (most production servers use TLS)

### Output Format Issues

- Use `--output json` for machine-readable output
- Use `--output table` for human-readable output
- Use `--output csv` for spreadsheet-compatible output

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run `make fmt` and `make lint`
6. Submit a pull request

## License

Licensed under the Apache License, Version 2.0. See the LICENSE file for details.

## Support

- Documentation: [Airavata Documentation](https://airavata.apache.org/)
- Issues: [GitHub Issues](https://github.com/apache/airavata/issues)
- Mailing List: [Airavata Mailing Lists](https://airavata.apache.org/mailing-lists.html)
