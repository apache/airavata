# Authentication and Authorization

Every Airavata API call that reads private data or changes anything carries a bearer token:

```
Authorization: Bearer <token>
```

Three kinds of token are accepted:

| Token | Who it identifies | Meant for |
|---|---|---|
| **CILogon access token** | a real user, signed in through their home institution, ORCID, Google or another identity provider that CILogon federates | all normal use |
| **Service account token** | a non-human account created by an admin, always with `USER` authority | automation: CI pipelines, gateways, scheduled jobs |
| **Root token** | the built-in `root` account, with `SUPER_ADMIN` authority | first-time setup and testing only |

**Authentication** establishes who the caller is, from the token. **Authorization** determines what they may do: their role, what they own, and what has been shared with them.

---

## Authentication

### CILogon

[CILogon](https://www.cilogon.org) is an OpenID Connect provider for research and education. Users sign in with an account they already have, and Airavata receives an access token for them.

CILogon access tokens are opaque strings, not JWTs, so Airavata cannot check them locally. For each request, the server:

1. sends the token to CILogon's **introspection** endpoint (RFC 7662), authenticating with its own OAuth client id and secret. An inactive or unknown token is rejected with `401`.
2. calls CILogon's **userinfo** endpoint with the token to get profile claims (name, email). If this call fails, the request continues without them.
3. takes the username from `username`, `preferred_username` or `sub`. CILogon URIs such as `http://cilogon.org/serverE/users/12345` are shortened to **`cilogon:12345`**. This value is the user id used everywhere in Airavata: ownership, shares, group membership and roles.
4. looks up the user's roles in Airavata's own database. **A token never grants its own privileges.**

If CILogon cannot be reached, the request fails with `502 Unable to validate bearer token` rather than `401`, because the failure is not the caller's fault.

### Service accounts

A service account is an identity for software rather than a person. An admin creates it, and Airavata issues it a token. The token expires, and an admin can renew or regenerate it.

A service account is an ordinary user in every way but three:

- **It is never an admin.** Its token always authenticates with `USER` authority. Rows in `user_roles` are ignored for service accounts, so it can't be promoted. A service account can't create or manage other service accounts either.
- **Only admins create and manage it.** Every `/api/v1/service-accounts` endpoint needs `serviceaccount:read` or `serviceaccount:manage`, which `ADMIN` and `SUPER_ADMIN` hold.
- **Its permissions are its own.** See [Permissions for a service account](#permissions-for-a-service-account).

Apart from that, it does what a user does. It owns the SSH credentials, cluster configs, storages and runs it creates. Other users can share records with it, by user id or through a group, and add it to groups.

Its user id is `sa:<name>`, for example `sa:nightly-ci`. The `sa:` prefix is reserved: the user registration endpoint refuses ids that start with it, and the server rejects a CILogon login that would map to one.

#### Create a service account

```bash
curl -s -X POST localhost:9095/api/v1/service-accounts \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"nightly-ci","displayName":"Nightly CI","description":"Runs regression jobs","validityDays":90}'
```

```json
{
  "serviceAccount": {
    "serviceAccountId": "sa:nightly-ci",
    "name": "nightly-ci",
    "status": "ACTIVE",
    "createdBy": "cilogon:12345",
    "tokenIssuedAt": 1767225600000,
    "tokenExpiresAt": 1774998000000
  },
  "token": "airavata_sa_3q2-7wEXAMPLEEXAMPLEEXAMPLEEXAMPLEEXAMPLE"
}
```

**The token is shown once.** Airavata stores only its SHA-256 digest, so the token can't be read back later. Store it in your CI system's secret store. If you lose it, regenerate it.

`name` must be 2–64 characters: lower-case letters, digits, `.`, `_` or `-`, starting with a letter or digit. `validityDays` is between 1 and 365 and defaults to 90.

Use the token like any other bearer token:

```bash
curl -s localhost:9095/api/v1/ssh-credentials -H "Authorization: Bearer $SA_TOKEN"
```

Service account tokens start with `airavata_sa_`. The server checks them against its own database and never sends them to CILogon. The `airavata_sa_` prefix also helps secret scanners recognise a leaked token.

#### Permissions for a service account

By default a service account can do only what any user can do with their own records and what is shared with them. To let it do more, grant it [permissions](#permissions). Each account has its own set, so one can read every process while another can only use its own.

```bash
curl -X POST https://airavata.example.org/api/v1/service-accounts \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name": "monitor", "permissions": ["process:list", "cluster:discover"]}'
```

Change the set later with `PUT /api/v1/service-accounts/{id}`. A `permissions` list **replaces** the whole set, an empty list removes every grant, and leaving it out changes nothing. The change applies on the account's next request. `GET /api/v1/service-accounts/{id}` and `GET /api/v1/users/me` (called with the account's token) both list what it holds.

Three rules keep this from becoming a way round the limits above:

- **A service account holds only what was granted to it.** It gets nothing from a role. Granting a permission to `USER` reaches people, not service accounts.
- **Some permissions can never be granted to one:** `admin:manage`, `user:update_any` and `serviceaccount:manage`. A token holding them could appoint administrators, rewrite another user's profile, or mint service accounts, which would widen its own access. The API refuses them with `400`, and a row inserted into the database by hand is ignored. `user:create` and `user:set_status` can be granted, for an onboarding integration, but they don't reach administrators: changing an administrator's status still needs `admin:manage`.
- **You can only grant what you hold yourself.** An admin missing a permission can't hand it out. The API answers `403`.

#### Manage the token

Each service account has one token at a time.

| Action | Endpoint | Effect |
|---|---|---|
| **Renew** | `POST /api/v1/service-accounts/{id}/token/renew` | Moves the expiry to `validityDays` from now (default 90). The token stays the same, so clients keep working. Also works on an expired token. |
| **Regenerate** | `POST /api/v1/service-accounts/{id}/token/regenerate` | Issues a new token and returns it once. The old token stops working immediately. Use this when a token may have leaked. |
| **Disable** | `PUT /api/v1/service-accounts/{id}` with `{"status":"INACTIVE"}` | The token stops working but is kept. Set `"status":"ACTIVE"` to turn it back on. |
| **Delete** | `DELETE /api/v1/service-accounts/{id}` | Removes the account, its token, its group memberships and every share granted to it. Returns `409` if the account still owns records; disable it instead. |

The renew and regenerate request bodies are optional (`{"validityDays": 30}`).

An expired, regenerated-away or disabled token gets `401` with `WWW-Authenticate: Bearer error="invalid_token"`, the same as any invalid token.

`GET /api/v1/service-accounts` lists all service accounts and `GET /api/v1/service-accounts/{id}` reads one. Neither returns a token. Use `tokenExpiresAt` (milliseconds since the epoch) to find tokens that need renewing.

[airavata-mcp](mcp.md) signs in through CILogon only, so service account tokens can't be used with it.

### Root account

When the root account is enabled, the server prints a token at startup:

```
========================================
ROOT ACCOUNT TOKEN (Super Admin):
5b0c9a8e-2f4d-4e61-9b7a-1c3d5e7f9a0b
========================================
```

This token authenticates as the user `root` with `SUPER_ADMIN` authority. The server creates the `root` user record at startup if it is missing, so `root` can own resources straight away. The token is a new random value on every restart unless you set it with `AIRAVATA_ROOT_ACCOUNT_TOKEN`.

The root account lets a fresh deployment be used before an identity provider is set up. Use it to install and try Airavata, and to set up the first administrator.

> **Disable the root account in production.** It is a single shared secret with full administrative power. It is not tied to a person, carries no second factor and leaves no record of who used it. Once CILogon is configured and at least one real user has `SUPER_ADMIN`, set `AIRAVATA_ROOT_ACCOUNT_ENABLED=false`.

The [MCP server](mcp.md) refuses the root token, because it checks every token's identity with the issuer.

---

## Configuring CILogon at deployment time

### 1. Register an OAuth client with CILogon

Register a client at <https://cilogon.org/oauth2/register>:

- **Client type:** confidential (with a secret). The server needs the secret to call the introspection endpoint.
- **Scopes:** `openid`, `email`, `profile`, `org.cilogon.userinfo` and `offline_access`. The dashboard asks for `offline_access` so it gets a refresh token and can renew a user's access token without another sign-in.
- **Callback URLs:** the redirect URIs of the applications that will sign users in: the dashboard's CILogon callback, `https://<dashboard host>/dashboard/login/callback/` (and `http://localhost:9095/dashboard/login/callback/` for a local server), and `http://localhost:8082/callback` if you will use the [web-api-client](#programmatic-access-web-api-client).

CILogon approves new clients manually. Once your client is approved, you will have a **client id** (`cilogon:/client_id/…`) and a **client secret**.

The server can only introspect tokens that its client is allowed to see. The simplest arrangement is for users' tokens to be issued by the same client registration. If you use a separate public client, for example the one registered for `airavata-mcp`, confirm with CILogon that the server's client can introspect its tokens.

### 2. Set the server's environment

| Variable | Default | Meaning |
|---|---|---|
| `CILOGON_CLIENT_ID` | *(empty)* | The client id from step 1. Setting it turns on CILogon authentication. |
| `CILOGON_CLIENT_SECRET` | *(empty)* | The client secret. Keep it in a secret store, not in a committed file. |
| `CILOGON_ISSUER` | `https://cilogon.org` | Where the dashboard's sign-in finds CILogon's authorization and token endpoints, through OIDC discovery. Change this only for a non-production CILogon instance (e.g. `test.cilogon.org`). |
| `CILOGON_INTROSPECTION_URI` | `https://cilogon.org/oauth2/introspect` | Same. |
| `CILOGON_USERINFO_URI` | `https://cilogon.org/oauth2/userinfo` | Same. |
| `AIRAVATA_ROOT_ACCOUNT_ENABLED` | `true` | Set to `false` in production. |
| `AIRAVATA_ROOT_ACCOUNT_TOKEN` | *(random per start)* | Fixes the root token to a known value, for repeatable test environments. |

```bash
export CILOGON_CLIENT_ID='cilogon:/client_id/0123456789abcdef'
export CILOGON_CLIENT_SECRET='<from your secret store>'
export AIRAVATA_ROOT_ACCOUNT_ENABLED=false
bin/airavata-server
```

The server refuses to start if both the root account and CILogon are off, because nobody would be able to authenticate:

```
no authentication configured: enable the root account or set CILOGON_CLIENT_ID
```

Tokens travel in every request, so put TLS in front of the server in any deployment that is reachable over a network. See [INSTALL.md](https://github.com/apache/airavata/blob/master/INSTALL.md#production-notes).

### 3. New users start suspended

Signing in with CILogon creates the user's **user record** on their first request, with their name and email taken from CILogon. A new record is **suspended**: until an administrator activates it, the server refuses the account everything except `GET /api/v1/users/me`, and the dashboard tells the user their account is waiting for approval. A record that already exists is never changed by signing in. New users get the `USER` role.

### 4. Activate users

Administrators activate, suspend and deactivate users on the dashboard's **Users** page. It opens on the suspended accounts, so new users waiting for approval are the first thing an administrator sees. The change applies from the user's next request. Only `ACTIVE` accounts can use Airavata.

| Route | Who | Purpose |
|---|---|---|
| `GET /api/v1/users/me` | Any signed-in caller, active or not | `{userId, status, authorities, permissions, admin, user}`: who the server thinks the caller is |
| `GET /api/v1/users` | `ADMIN`, `SUPER_ADMIN` | Every user, with email, sign-in method, roles and status |
| `POST /api/v1/users/{userId}/status` | `ADMIN`, `SUPER_ADMIN` | `{"status": "ACTIVE" \| "SUSPENDED" \| "INACTIVE"}` |

An administrator can't change their own status. Only a `SUPER_ADMIN` can change another administrator's. Service accounts have their own lifecycle under `/api/v1/service-accounts`, and the root account has no status.

A `SUPER_ADMIN` can also create a record ahead of a user's first sign-in with `POST /api/v1/users`. A record created that way is active straight away.

### 5. Bootstrap the first administrator

To give a new deployment its first real administrator:

1. Have the administrator sign in once, through the dashboard or any CILogon token, so their record exists.
2. Sign in to the dashboard with the root token and activate them on the **Users** page.
3. Grant the role. No API endpoint changes roles, so do this in the database:

   ```sql
   INSERT INTO user_roles (user_id, role) VALUES ('cilogon:12345', 'SUPER_ADMIN');
   ```

   Their user id is shown on the Users page and on their own waiting-for-approval screen.

4. Restart with `AIRAVATA_ROOT_ACCOUNT_ENABLED=false`. From then on, the new administrator activates other users.

Without the root account, do steps 2 and 3 in the database:

```sql
UPDATE users SET status = 'ACTIVE' WHERE user_id = 'cilogon:12345';
INSERT INTO user_roles (user_id, role) VALUES ('cilogon:12345', 'SUPER_ADMIN');
```

---

## Getting a CILogon token and using it

How you sign in depends on how you use Airavata:

| You are… | Use |
|---|---|
| working interactively in a browser | the **dashboard**, which has CILogon sign-in built in |
| calling the API from scripts, `curl` or your own code | the **web-api-client**, to sign in through CILogon and get a token |
| working through an AI assistant | **airavata-mcp**, which signs in and refreshes tokens itself |

### Dashboard

The dashboard signs users in with CILogon directly. Open it and choose **Sign in with CILogon**, pick your identity provider, and you are signed in. You don't need to handle a token. During initial setup, before CILogon is configured, you can sign in with the root token instead. The token form stays available under **Sign in with a token instead**.

Sign-in is the authorization code flow with PKCE. The browser goes to CILogon and returns to `/dashboard/login/callback/` with a code. The Airavata server redeems the code using its client secret, which never reaches the browser, and returns the access and refresh tokens. The dashboard keeps both in the browser's local storage and renews the access token shortly before it expires, so a session lasts as long as CILogon honours the refresh token. Signing out forgets both.

The server's sign-in routes take no bearer token:

| Route | Purpose |
|---|---|
| `GET /api/v1/auth/cilogon` | `{enabled, authorizationEndpoint, clientId, scope}`. `enabled` is `false` unless both `CILOGON_CLIENT_ID` and `CILOGON_CLIENT_SECRET` are set. |
| `POST /api/v1/auth/cilogon/token` | `{code, codeVerifier, redirectUri}` → `{accessToken, refreshToken, expiresIn}`. `redirectUri` must be a `/dashboard/login/callback/` URL. |
| `POST /api/v1/auth/cilogon/refresh` | `{refreshToken}` → new tokens |

A user's first sign-in creates their account, suspended. The dashboard shows a new user that they are waiting for approval until an administrator activates them (see [Activate users](#4-activate-users)).

### Programmatic access: web-api-client

To call the API programmatically, get a CILogon token through the **web-api-client** in [`dev-tools/web-api-client`](https://github.com/apache/airavata/blob/master/dev-tools/web-api-client/README.md). It is a small web UI that runs the CILogon sign-in flow (authorization code with PKCE) and holds the resulting access token.

1. Add `http://localhost:8082/callback` as a redirect URI on your CILogon client.
2. Configure the client in `dev-tools/web-api-client/.env.local`. This file is never committed.

   ```
   SESSION_SECRET=<openssl rand -base64 32>
   CILOGON_CLIENT_ID=<CILogon client id>
   CILOGON_CLIENT_SECRET=<CILogon client secret>
   ```

3. Run it and sign in:

   ```bash
   cd dev-tools/web-api-client
   npm install
   npm run dev        # http://localhost:8082
   ```

Once you're signed in, the web-api-client's console sends requests to any Airavata endpoint with your token attached as `Authorization: Bearer <token>`. It sends them from its own server, so cross-origin requests are not a problem. Use the same access token in your scripts:

```bash
TOKEN='<CILogon access token>'
curl -s localhost:9095/api/v1/ssh-credentials -H "Authorization: Bearer $TOKEN"
```

Access tokens are short-lived. When requests start returning `401` with `WWW-Authenticate: Bearer error="invalid_token"`, sign out of the web-api-client and sign in again to get a new token. The console also warns you once the token has expired.

For WebSocket routes, such as the [interactive auth](interactive-auth.md) subscription, send the same header on the upgrade request. A browser offers the token as a `base64url.bearer.<token>` subprotocol instead.

### airavata-mcp

The MCP client handles sign-in for you and keeps the token refreshed:

```bash
export AIRAVATA_MCP_OAUTH_CLIENT_ID=<the CILogon client id registered for airavata-mcp>
airavata-mcp login            # opens a browser
airavata-mcp login --device   # prints a code to enter on another device (for SSH sessions and HPC login nodes)
```

Tokens are stored in the OS keychain. `airavata-mcp logout` removes them. See [mcp.md](mcp.md#install-and-sign-in).

### Find your user id

Your Airavata user id is the normalised CILogon subject. Ask CILogon for it:

```bash
curl -s https://cilogon.org/oauth2/userinfo -H "Authorization: Bearer $TOKEN" | jq -r '.sub'
# http://cilogon.org/serverE/users/12345   →   your user id is cilogon:12345
```

---

## Authorization

What a caller may do comes from three sources.

### Roles

Roles are stored in Airavata's `user_roles` table and looked up on every request. They are never read from the token.

| Role | Adds |
|---|---|
| `USER` | The default for anyone with no role row, and the only role a service account ever has. Self-service: register SSH credentials, cluster configs, storages, data products, templates, deployments and runs; manage and share what you own. |
| `ADMIN` | Platform-wide listings (every process, config, storage and user), and activating and suspending users other than administrators. |
| `SUPER_ADMIN` | Everything `ADMIN` can do, plus changing administrators' status and creating user records ahead of a first sign-in. The root account has this role. |

### Permissions

Actions that need privilege are gated by a **permission** such as `audit:read`, not by a role name. Each role is granted a set of permissions in Airavata's `role_permissions` table, and a caller holds the union of what their roles grant. Which roles may do something is therefore data, not code. `GET /api/v1/users/me` lists the caller's permissions.

| Permission | Allows | Default |
|---|---|---|
| `audit:read` | Reading the [audit log](audit.md) | `ADMIN` |
| `user:list` | Listing every user | `ADMIN` |
| `user:read_any` | Reading another user's record | `ADMIN` |
| `user:set_status` | Activating, suspending and deactivating users | `ADMIN` |
| `user:create` | Creating a user record ahead of the user's first sign-in | `SUPER_ADMIN` only |
| `user:update_any` | Editing another user's profile | `SUPER_ADMIN` only |
| `admin:manage` | Appointing and removing administrators, and changing an administrator's status | `SUPER_ADMIN` only |
| `group:list` | Listing every group | `ADMIN` |
| `group:read_any` | Reading a group the caller is not in | `ADMIN` |
| `group:manage_any` | Renaming, deleting and managing the members of any group | `ADMIN` |
| `serviceaccount:read` | Listing and reading service accounts | `ADMIN` |
| `serviceaccount:manage` | Creating, editing and deleting service accounts, and re-issuing their tokens | `ADMIN` |
| `cluster:manage` | Creating, editing and deleting clusters and their partitions | `ADMIN` |
| `cluster:discover` | Discovering a cluster's partitions over SSH | `ADMIN` |
| `clusterconfig:list` | Listing every cluster config | `ADMIN` |
| `template:manage` | Creating, editing and deleting application templates | `ADMIN` |
| `deployment:manage` | Creating, editing and deleting batch deployments | `ADMIN` |
| `storage:list` | Listing every data storage | `ADMIN` |
| `dataproduct:list` | Listing every data product | `ADMIN` |
| `virtualdirectory:list` | Listing every virtual data directory | `ADMIN` |
| `resource:manage_any` | Acting as the owner of any cluster config, data storage, data product or virtual data directory. SSH credentials are not included: they stay visible to their owner only | `ADMIN` |
| `process:list` | Listing every process | `ADMIN` |
| `process:update` | Editing any process directly | `ADMIN` |
| `process:delete` | Deleting any process | `ADMIN` |
| `process:manage_any` | Reading, launching and editing the tasks, statuses and workflows of any process | `ADMIN` |

A name ending in `_any` is an override that reaches records the caller does not own. A caller can always act on their own records, and on records shared with them, without any permission. Granting only one of these permissions grants exactly that: a role holding `process:update` can edit a process, but still needs access to the cluster config the process names.

Where the API reference says an endpoint is "admin only", read it as "needs the permission listed here, which `ADMIN` holds by default".

`SUPER_ADMIN` holds every permission by definition and is never looked up in the table, so no change to `role_permissions` can lock the last administrator out. To let another role read the audit log, for example:

```sql
INSERT INTO role_permissions (role, permission) VALUES ('USER', 'audit:read');
```

Grants are cached for a minute, so a change made directly in the database reaches every request within that time.

### Ownership

Anything a user creates belongs to them. The owner is always taken from the token, never from the request body, and cannot be transferred. Owners can read, change, delete and share their records. Admins have the same rights, with one exception: **SSH credentials are visible to their owner only**, admins included, because a credential is the secret itself.

### Sharing

Owners grant other users or groups `READ` or `WRITE` on their records, such as cluster configs, data storages, data products and virtual data directories. See [sharing.md](sharing.md).

### Status codes

| Status | Meaning |
|---|---|
| `401 Unauthorized` | No token, or a token that is invalid, expired, or an old root token from before a restart. A service account token also gets `401` once it has been regenerated, or when its account is disabled. |
| `403 Forbidden` | Authenticated, but not allowed: missing role, not the owner, or no share. Also returned for everything but `GET /api/v1/users/me` while the caller's account is suspended or inactive. |
| `404 Not Found` | The record doesn't exist. Also returned when a caller who isn't a CILogon user, and has no user record, tries to create something they would own. |
| `502 Bad Gateway` | CILogon could not be reached to validate the token. |

---

## Production checklist

- [ ] CILogon client registered and approved, with `CILOGON_CLIENT_ID` and `CILOGON_CLIENT_SECRET` supplied from a secret store
- [ ] At least one real user registered with `SUPER_ADMIN`
- [ ] **`AIRAVATA_ROOT_ACCOUNT_ENABLED=false`**, with any `AIRAVATA_ROOT_ACCOUNT_TOKEN` removed from the environment
- [ ] TLS terminated in front of the server
- [ ] CORS narrowed to the portal origins that need it
- [ ] Service account tokens kept in a secret store, with `validityDays` no longer than your rotation policy allows
