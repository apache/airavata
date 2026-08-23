# Installing the Airavata Go API

Build, configuration and first-run instructions for the Go implementation of the
Airavata API (`cmd/airavata-server`).

---

## Prerequisites

| Requirement | Version | Check using |
|---|---|---|
| **Go** | 1.24.2+ | `go version` |
| **PostgreSQL** | 14+ | `psql --version` |
| **Docker Engine** | 20.10+ *(optional — only to run PostgreSQL locally)* | `docker -v` |
| **Docker Compose** | 2.0+ *(optional)* | `docker compose version` |

No other tooling is needed. All Go dependencies are declared in `go.mod` and fetched
by the build.

---

## Quick start

From a clean checkout, this brings up a database and a running API:

```bash
# 1. Start PostgreSQL (listens on host port 15432)
docker compose -f dev-tools/compose/compose.yml up -d postgres

# 2. Build
go build -o bin/airavata-server ./cmd/airavata-server

# 3. Run
./bin/airavata-server
```

The defaults in `internal/config/config.go` already match the compose file, so no
configuration is required for local development.

On startup the server creates the schema, then prints the root account token:

```
========================================
ROOT ACCOUNT TOKEN (Super Admin):
7f3c1e2a-...-9b4d
========================================
```

Copy that token — it is how you make the first authenticated call. It is regenerated
on every restart unless you pin it with `AIRAVATA_ROOT_ACCOUNT_TOKEN`.

Confirm the server is up:

```bash
curl -s localhost:9095/health
# {"status":"UP"}
```

---

## Building

```bash
go build -o bin/airavata-server ./cmd/airavata-server   # binary
go test ./...                                           # tests
go vet ./...                                            # static checks
gofmt -l .                                              # formatting (no output = clean)
```

The result is a single static binary with no runtime dependencies beyond the database.

To cross-compile for a Linux deployment host:

```bash
GOOS=linux GOARCH=amd64 go build -o bin/airavata-server-linux ./cmd/airavata-server
```

---

## Configuration

Every setting is read from the environment at startup. All of them have defaults
suitable for local development, so an unconfigured server will start against the
compose database.

### Server

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `9095` | Port the HTTP API listens on. |
| `AIRAVATA_CORS_ALLOWED_ORIGINS` | `*` | Comma-separated allowed origins. The server echoes the caller's origin rather than sending `*`, because every authenticated call carries an `Authorization` header and browsers reject the wildcard for credentialed requests. |

### Database

| Variable | Default | Description |
|---|---|---|
| `AIRAVATA_DB_HOST` | `localhost` | PostgreSQL host. |
| `AIRAVATA_DB_PORT` | `15432` | PostgreSQL port (the compose file maps container `5432` to host `15432`). |
| `AIRAVATA_DB_NAME` | `airavata` | Database name. |
| `AIRAVATA_DB_USER` | `airavata` | Database user. |
| `AIRAVATA_DB_PASSWORD` | `123456` | Database password. **Change this outside development.** |
| `AIRAVATA_DB_SSLMODE` | `disable` | libpq TLS mode: `disable`, `require`, `verify-ca`, `verify-full`. **Set at least `require` for anything but a loopback database.** |
| `AIRAVATA_DB_DSN` | *(built from the above)* | Full PostgreSQL connection URL. Set this to override the six variables above entirely — useful for extra TLS options or a managed database. |
| `AIRAVATA_DB_AUTO_MIGRATE` | `true` | Create/update tables from the entity model at startup. |

The assembled default DSN is:

```
postgres://airavata:123456@localhost:15432/airavata?sslmode=disable
```

Connection pooling follows the settings the Java service used for HikariCP: a maximum
of 20 open connections, 2 kept idle, and a 30-minute connection lifetime
(`internal/db/open.go`).

### Authentication — CILogon

| Variable | Default | Description |
|---|---|---|
| `CILOGON_CLIENT_ID` | *(empty)* | OAuth client id used to authenticate introspection calls. |
| `CILOGON_CLIENT_SECRET` | *(empty)* | OAuth client secret. |
| `CILOGON_INTROSPECTION_URI` | `https://cilogon.org/oauth2/introspect` | RFC 7662 introspection endpoint. |
| `CILOGON_USERINFO_URI` | `https://cilogon.org/oauth2/userinfo` | Profile claims endpoint. |

CILogon access tokens for standard OAuth clients are opaque strings, not JWTs, so they
cannot be verified locally against a key set. Each bearer token is validated by calling
the introspection endpoint; profile claims come from a second call to userinfo, since
introspection alone returns only a small fixed claim set.

Authorities are never read from the token. They are looked up in the `user_roles` table
by username, so a token cannot assert its own privileges.

### Authentication — root account

| Variable | Default | Description |
|---|---|---|
| `AIRAVATA_ROOT_ACCOUNT_ENABLED` | `true` | Enables the bootstrap Super Admin token. |
| `AIRAVATA_ROOT_ACCOUNT_TOKEN` | *(random UUID per start)* | Pin the token to a fixed value instead of regenerating it. |

The root account exists so a fresh deployment has a way in before an identity provider
is configured. It is intended for initial setup and testing.

> **Startup guard:** if the root account is disabled and `CILOGON_CLIENT_ID` is empty,
> there is no way to authenticate at all and every guarded endpoint would be
> permanently unreachable. The server refuses to start rather than failing later:
> `no authentication configured: enable the root account or set CILOGON_CLIENT_ID`.

---

## Database setup

### With Docker

```bash
docker compose -f dev-tools/compose/compose.yml up -d postgres
```

This starts PostgreSQL 17 on host port `15432` with the database, user and password
matching the defaults above. Adminer is also available (`up -d adminer`,
`http://localhost:18080`) for browsing the schema.

### Without Docker

Create the database and user, then point the server at it:

```sql
CREATE USER airavata WITH PASSWORD 'a-strong-password';
CREATE DATABASE airavata OWNER airavata ENCODING 'UTF8';
```

The server needs to create tables in the `public` schema of that database, which the
database owner already may:

```bash
export AIRAVATA_DB_HOST=db.internal
export AIRAVATA_DB_PORT=5432
export AIRAVATA_DB_PASSWORD='a-strong-password'
export AIRAVATA_DB_SSLMODE=require
```

### Schema management

With `AIRAVATA_DB_AUTO_MIGRATE=true` (the default) the server creates and updates
tables from the entity model on startup. Like Hibernate's `ddl-auto=update`, it only
adds tables, columns and indexes — it never drops or narrows anything, so it cannot
remove a column that is no longer mapped. This is a development convenience; the
schema is defined by the entities listed in `internal/db/migrate.go`.

For production, set `AIRAVATA_DB_AUTO_MIGRATE=false` and apply schema changes as
versioned migrations instead:

```bash
airavata-server migrate status   # what has and has not been applied
airavata-server migrate up       # apply every pending migration, in order
```

Both read the same `AIRAVATA_DB_DSN` (and related `AIRAVATA_DB_*`) variables as the
server, connect, and exit — they never start the HTTP server. Applied versions are
tracked in a `schema_migrations` table the migrator creates on first use.

Migrations live as plain `.sql` files in `internal/db/migrations/`, named
`NNNN_description.sql`. `0001_baseline.sql` is the full schema recorded from a real
`AutoMigrate` run against PostgreSQL, so a fresh production database ends up identical
to a fresh development one — a claim `internal/db/postgres_test.go` checks against a
live server rather than asserting. Add a new file with the next number for any later
schema change — there are no down migrations: like `ddl-auto`, this framework never
rewrites or drops history, so a mistake is corrected with a further migration rather
than a rollback script.

Each migration runs inside a transaction, and PostgreSQL rolls back DDL, so a file that
fails partway through leaves the schema untouched and can simply be fixed and re-run.

To exercise the two schema paths against a real server:

```bash
docker compose -f dev-tools/compose/compose.yml up -d postgres
AIRAVATA_TEST_POSTGRES_DSN='postgres://airavata:123456@localhost:15432/airavata?sslmode=disable' \
  go test ./internal/db/
```

Those tests skip without that variable, so `go test ./...` stays green on a machine
with no database.

---

## First run

The root token authenticates as the principal `root` with the `SUPER_ADMIN` authority.
That is enough to administer clusters, SSH keys, templates and deployments right away:

```bash
TOKEN='<the token printed at startup>'

# A cluster is reached through an SSH endpoint, so create the host first.
ENDPOINT_ID=$(curl -s -X POST localhost:9095/api/v1/ssh-endpoints \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"expanse-login","hostName":"login.expanse.sdsc.edu"}' | jq -r '.sshEndpointId')

curl -s -X POST localhost:9095/api/v1/clusters \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"clusterName":"expanse","sshEndpointId":"'"$ENDPOINT_ID"'","slurmHome":"/usr/bin"}'
```

### Two things that need a manual step

**1. Owning resources requires a matching `users` row.**

Endpoints that create *owned* resources — SSH endpoint credentials, processes —
resolve the caller to a user record and refuse if none exists:

```
404  No user record found for authenticated principal: root
```

The root token bypasses introspection, so no record is created for it. Insert one if
you intend to own resources as `root`:

```sql
INSERT INTO users (user_id, auth_method, first_name, last_name, status, created_at)
VALUES ('root', 'SYSTEM', 'Root', 'Account', 'ACTIVE', UNIX_TIMESTAMP() * 1000);
```

**2. Granting a role requires a manual insert.**

Authorities are read from `user_roles`, but no endpoint writes to that table — user
registration sets a status and timestamp, and updates deliberately cannot change roles.
Promote a user directly:

```sql
INSERT INTO user_roles (user_id, role) VALUES ('cilogon:12345', 'ADMIN');
```

Valid roles are `SUPER_ADMIN`, `ADMIN` and `USER`. A user with no rows is treated as
`USER`.

CILogon subjects are normalised to `cilogon:<id>` before lookup, so that is the form
the `user_id` column must hold.

### Registering users

With the root token you can register users through the API (Super Admin only):

```bash
curl -s -X POST localhost:9095/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"userId":"cilogon:12345","firstName":"Ada","lastName":"Lovelace","email":"ada@example.edu","authMethod":"CILOGON"}'
```

---

## Verifying the installation

```bash
go test ./...            # 99 tests covering the schema, routing, authorisation and orchestration
curl -s localhost:9095/health
curl -s localhost:9095/api/v1/clusters      # readable without a token
```

Catalogue reads — clusters, partitions, SSH keys, SSH credentials, templates and
deployments — are open to anonymous callers. Beyond those:

- **Writes** require `ADMIN` or `SUPER_ADMIN`, except creating SSH endpoint credentials
  and processes, which any authenticated caller may do for themselves.
- **Owner-scoped reads** (`/ssh-endpoint-credentials/{id}`, `/users/{id}`) require
  authentication and are refused unless the caller owns the record or is an admin.
- **Unfiltered listings** of SSH endpoint credentials, processes and users require
  `ADMIN`, because they expose who holds access to what.

A request with no token to a guarded endpoint returns `401`, while an authenticated
caller lacking the required role returns `403`.

> **Known gap:** reading a process by id (`GET /api/v1/processes/{id}`) or by
> deployment carries no authorisation at all, so any caller can read any process,
> including the batch section nested inside it. This is carried over from the Java service rather than introduced here, but
> it is worth knowing before exposing the API publicly.

---

## Production notes

- **Disable the root account** (`AIRAVATA_ROOT_ACCOUNT_ENABLED=false`) once CILogon is
  configured, and authenticate through the identity provider instead.
- **Set a real database password.** The `123456` default exists only to match the
  development compose file.
- **Narrow CORS** from `*` to the portal origins that actually need it.
- **Turn off auto-migration** (`AIRAVATA_DB_AUTO_MIGRATE=false`) and run
  `airavata-server migrate up` as a reviewed deploy step instead — see "Schema
  management" above.
- **Terminate TLS in front of the server.** It speaks plain HTTP; bearer tokens must
  not cross an untrusted network unencrypted.

---

## Troubleshooting

| Symptom | Cause and fix |
|---|---|
| `connect to database: dial tcp ...: connection refused` | PostgreSQL is not running or the host/port is wrong. Check `docker compose -f dev-tools/compose/compose.yml ps`. |
| `no authentication configured` at startup | Root account disabled with no `CILOGON_CLIENT_ID`. Set one or re-enable the root account. |
| `401` with `WWW-Authenticate: Bearer error="invalid_token"` | The token was rejected by introspection, or it is a stale root token from a previous start. Restart and use the newly printed token, or pin it with `AIRAVATA_ROOT_ACCOUNT_TOKEN`. |
| `502 Unable to validate bearer token` | CILogon is unreachable. This is reported separately from `401` because it is not the caller's fault. |
| `403 Access denied` on a write | The caller authenticated but lacks `ADMIN`/`SUPER_ADMIN`. Grant the role in `user_roles`. |
| `404 No user record found for authenticated principal` | The caller has no `users` row. See "First run" above. |
| `409 Key is in use by a credential` | An SSH key cannot be deleted while a credential references it. Delete the credential first. |
| `409 Template has deployments` | A template cannot be deleted while deployments reference it. Delete the deployments first. |

---

## Layout

```
api/<domain>/          controller.go, service.go, repository.go
api/<domain>/model/    persistent entities
api/<domain>/dto/      request/response payloads and mappers
cmd/airavata-server/   entrypoint
internal/auth/         bearer token introspection and authority guards
internal/config/       environment configuration
internal/db/           connection and schema migration
internal/httpx/        error, JSON and validation plumbing
internal/server/       dependency wiring and routing
```

Domains are `iam`, `credentials`, `compute`, `application`, `data` and `process`.
