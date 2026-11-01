# airavata-mcp

`airavata-mcp` is a [Model Context Protocol](https://modelcontextprotocol.io) server that lets an AI assistant
(Claude, VS Code Copilot, Cursor, ...) use Airavata on your behalf: find clusters and applications, turn an
sbatch script into an Airavata application, plan and launch runs, and explain failures. It is a thin client of
the REST API in [api.md](api.md): it never touches the database, and it can do only what your own account can.

It speaks MCP 2026-07-28 and stays compatible with 2025-11-25 clients. This is Part A of
`airavata-mcp-server-plan.md`: everything here works against today's `airavata-server`.

## Install and sign in

```bash
go build -o bin/airavata-mcp ./cmd/airavata-mcp
export AIRAVATA_MCP_API_URL=https://airavata.example.edu
export AIRAVATA_MCP_OAUTH_CLIENT_ID=<the public CILogon client id registered for airavata-mcp>
bin/airavata-mcp login            # browser sign-in (PKCE + loopback redirect)
bin/airavata-mcp login --device   # on an SSH session or HPC login node
```

The access and refresh tokens are stored in the OS keychain and refreshed automatically. `airavata-mcp logout`
removes them. For CI you can set `AIRAVATA_MCP_TOKEN` instead; it is never a flag, so it never shows up in a
process listing, and it cannot be refreshed. Airavata's bootstrap root token is refused: every token's identity
is checked with the issuer's userinfo endpoint.

## Connect a client

Claude Code:

```bash
claude mcp add airavata -e AIRAVATA_MCP_API_URL=https://airavata.example.edu -- /path/to/airavata-mcp --allow-write
```

Any client that launches stdio servers takes the same command and environment variables.

## Configuration

Every flag has an environment variable; a flag wins over its variable.

| Flag | Environment variable | Default | |
|---|---|---|---|
| `--api-url` | `AIRAVATA_MCP_API_URL` | `http://localhost:9095` | `airavata-server` base URL |
| (none) | `AIRAVATA_MCP_TOKEN` | | Overrides the keychain token |
| `--oauth-issuer` | `AIRAVATA_MCP_OAUTH_ISSUER` | `https://cilogon.org` | OpenID Connect issuer used by `login` |
| `--oauth-client-id` | `AIRAVATA_MCP_OAUTH_CLIENT_ID` | | Public client id used by `login` |
| `--allow-write` / `--read-only` | `AIRAVATA_MCP_ALLOW_WRITE` | read-only | Write tools are not even listed without it |
| `--toolsets` | `AIRAVATA_MCP_TOOLSETS` | `compute,applications,data,processes` | `sharing` is opt-in |
| `--lockdown` | `AIRAVATA_MCP_LOCKDOWN` | off | Hide descriptions written by anyone outside you and your groups |
| `--admin` | `AIRAVATA_MCP_ADMIN` | off | stdio only; adds the `admin` toolset. airavata-server still requires an ADMIN account |
| `--policy` | `AIRAVATA_MCP_POLICY` | `<user config dir>/airavata-mcp/policy.yaml` | See below |
| `--transport` | `AIRAVATA_MCP_TRANSPORT` | `stdio` | `http` serves stateless Streamable HTTP on a loopback address, for testing |
| `--http-addr` | `AIRAVATA_MCP_HTTP_ADDR` | `127.0.0.1:8095` | Must be a loopback address |

### Policy file

The policy file can only add restrictions. Entries are exact tool names. Unknown keys are rejected.

```yaml
deny:            # never listed, even with --allow-write
  - share_resource
confirm:         # also confirm these read-only tools
  - list_data_products
limits:          # checked before a launch confirmation is shown
  max_walltime_minutes: 2880
  max_core_hours_per_launch: 5000
  max_gpu_hours_per_launch: 64
```

Core-hours and GPU-hours are what a run requests (walltime × cores or GPUs), not what an allocation is charged.

## Confirmations

Every tool that changes anything asks you to confirm in your client before it runs, through MCP elicitation.
The form says exactly what will happen; for a launch you can lower or raise the walltime there, and the new
value is checked again. If your client does not support elicitation, write tools refuse rather than run
unconfirmed. Plans (`plan_run`, `draft_application`, `convert_sbatch_script`) return single-use handles that
expire after 24 hours and are bound to your account, so a confirmed launch can never run twice.

Declining a form and dismissing it are reported differently, as the MCP spec distinguishes them. A decline
tells the assistant not to work around your decision. A dismissal (`cancel`, also what some clients send when
they cannot show the form, such as Claude Desktop in some modes) does nothing and leaves the handle usable, so
you can be asked again.

Every write is logged to stderr as one JSON line (principal, tool, a hash of the arguments, the confirmation
result and the ids created). Arguments themselves are never logged.

## Tools

| Toolset | Tools |
|---|---|
| compute | `list_clusters`, `get_cluster`, `list_cluster_configs`, `list_ssh_credentials`, `list_ssh_auth_prompts`, `recommend_partition`, `register_cluster_config` (write) |
| applications | `list_applications`, `get_application`, `convert_sbatch_script`, `draft_application` |
| data | `list_data_storages`, `list_data_products`, `register_dataset` (write) |
| processes | `plan_run`, `launch_process` (write), `get_process_status`, `diagnose_process` |
| sharing | `list_shares`, `share_resource` (write), `revoke_share` (write) |
| admin | `register_cluster`, `update_partition`, `register_application`, `update_deployment` (all writes) |

Resources: `airavata://clusters/{clusterId}`, `airavata://applications/{templateId}`,
`airavata://processes/{processId}` (subscribe for updates), `airavata://data-products/{dataProductId}`,
`airavata://docs/api`, `airavata://docs/sharing`. Prompts: `onboard`, `bring_my_slurm_script`,
`run_experiment`, `why_did_my_job_fail`.

## Interactive SSH logins

If a cluster login uses a fully interactive credential, runs wait until you answer its prompts (password, OTP,
Duo). Answer them in a terminal, never in the conversation:

```bash
airavata-mcp ssh-auth
```

It shows each pending login and reads secret answers without echo.

## Not available yet

These need changes to `airavata-server` (Part B of the plan): registering applications as a non-admin, listing
your earlier runs, cancelling a run, reading job logs, choosing a partition per run, keeping sbatch options the
API has no field for, workflows (predefined recipes such as sweeps and chains, which job arrays and dependency
chains will map to, and Nextflow pipelines), searching data by metadata, and browsing a storage.
