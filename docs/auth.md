# Authentication and Authorization

Every Airavata API call that reads private data or changes anything carries a bearer token:

```
Authorization: Bearer <token>
```

Two kinds of token are accepted:

| Token | Who it identifies | Meant for |
|---|---|---|
| **CILogon access token** | a real user, signed in through their home institution, ORCID, Google or another identity provider that CILogon federates | all normal use |
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

### Root account

When the root account is enabled, the server prints a token at startup:

```
========================================
ROOT ACCOUNT TOKEN (Super Admin):
5b0c9a8e-2f4d-4e61-9b7a-1c3d5e7f9a0b
========================================
```

This token authenticates as the user `root` with `SUPER_ADMIN` authority. The server creates the `root` user record at startup if it is missing, so `root` can own resources straight away. The token is a new random value on every restart unless you set it with `AIRAVATA_ROOT_ACCOUNT_TOKEN`.

The root account lets a fresh deployment be used before an identity provider is set up. Use it to install and try Airavata, and to register the first users and administrators.

> **Disable the root account in production.** It is a single shared secret with full administrative power. It is not tied to a person, carries no second factor and leaves no record of who used it. Once CILogon is configured and at least one real user has `SUPER_ADMIN`, set `AIRAVATA_ROOT_ACCOUNT_ENABLED=false`.

The [MCP server](mcp.md) refuses the root token, because it checks every token's identity with the issuer.

---

## Configuring CILogon at deployment time

### 1. Register an OAuth client with CILogon

Register a client at <https://cilogon.org/oauth2/register>:

- **Client type:** confidential (with a secret). The server needs the secret to call the introspection endpoint.
- **Scopes:** `openid`, `email`, `profile`, `org.cilogon.userinfo`. Add `offline_access` if clients should get refresh tokens.
- **Callback URLs:** the redirect URIs of the applications that will sign users in: the dashboard's CILogon callback, and `http://localhost:8082/callback` if you will use the [web-api-client](#programmatic-access-web-api-client).

CILogon approves new clients manually. Once your client is approved, you will have a **client id** (`cilogon:/client_id/…`) and a **client secret**.

The server can only introspect tokens that its client is allowed to see. The simplest arrangement is for users' tokens to be issued by the same client registration. If you use a separate public client, for example the one registered for `airavata-mcp`, confirm with CILogon that the server's client can introspect its tokens.

### 2. Set the server's environment

| Variable | Default | Meaning |
|---|---|---|
| `CILOGON_CLIENT_ID` | *(empty)* | The client id from step 1. Setting it turns on CILogon authentication. |
| `CILOGON_CLIENT_SECRET` | *(empty)* | The client secret. Keep it in a secret store, not in a committed file. |
| `CILOGON_INTROSPECTION_URI` | `https://cilogon.org/oauth2/introspect` | Change this only for a non-production CILogon instance (e.g. `test.cilogon.org`). |
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

### 3. Bootstrap the first administrator

A CILogon user needs a **user record** in Airavata before they can own anything (credentials, cluster configs, storages, runs). Without one they get `404 No user record found for authenticated principal`. Only a `SUPER_ADMIN` can create records. To give a new deployment its first real administrator:

1. Start with the root account **enabled**.
2. Have the administrator sign in to CILogon and find their user id (see [Find your user id](#find-your-user-id)).
3. Register them using the root token:

   ```bash
   curl -s -X POST localhost:9095/api/v1/users \
     -H "Authorization: Bearer $ROOT_TOKEN" -H 'Content-Type: application/json' \
     -d '{"userId":"cilogon:12345","firstName":"Ada","lastName":"Lovelace","email":"ada@example.edu","authMethod":"CILOGON"}'
   ```

4. Grant the role. No API endpoint changes roles, so do this in the database:

   ```sql
   INSERT INTO user_roles (user_id, role) VALUES ('cilogon:12345', 'SUPER_ADMIN');
   ```

5. Restart with `AIRAVATA_ROOT_ACCOUNT_ENABLED=false`. From then on, the new administrator registers other users with their own CILogon token.

---

## Getting a CILogon token and using it

How you sign in depends on how you use Airavata:

| You are… | Use |
|---|---|
| working interactively in a browser | the **dashboard**, which has CILogon sign-in built in |
| calling the API from scripts, `curl` or your own code | the **web-api-client**, to sign in through CILogon and get a token |
| working through an AI assistant | **airavata-mcp**, which signs in and refreshes tokens itself |

### Dashboard

The dashboard signs users in with CILogon directly. Open it and choose CILogon sign-in, pick your identity provider, and you are signed in. You don't need to handle a token. During initial setup, before CILogon is configured, you can sign in with the root token instead.

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
| `USER` | The default for anyone with no role row. Self-service: register SSH credentials, cluster configs, storages, data products, templates, deployments and runs; manage and share what you own. |
| `ADMIN` | Platform-wide listings (every process, config, storage and user) and administration of other users' records. |
| `SUPER_ADMIN` | Everything `ADMIN` can do, plus registering users. The root account has this role. |

### Ownership

Anything a user creates belongs to them. The owner is always taken from the token, never from the request body, and cannot be transferred. Owners can read, change, delete and share their records. Admins have the same rights, with one exception: **SSH credentials are visible to their owner only**, admins included, because a credential is the secret itself.

### Sharing

Owners grant other users or groups `READ` or `WRITE` on their records, such as cluster configs, data storages, data products and virtual data directories. See [sharing.md](sharing.md).

### Status codes

| Status | Meaning |
|---|---|
| `401 Unauthorized` | No token, or a token that is invalid, expired, or an old root token from before a restart. |
| `403 Forbidden` | Authenticated, but not allowed: missing role, not the owner, or no share. |
| `404 Not Found` | The record doesn't exist. Also returned when the caller has no user record and tries to create something they would own. |
| `502 Bad Gateway` | CILogon could not be reached to validate the token. |

---

## Production checklist

- [ ] CILogon client registered and approved, with `CILOGON_CLIENT_ID` and `CILOGON_CLIENT_SECRET` supplied from a secret store
- [ ] At least one real user registered with `SUPER_ADMIN`
- [ ] **`AIRAVATA_ROOT_ACCOUNT_ENABLED=false`**, with any `AIRAVATA_ROOT_ACCOUNT_TOKEN` removed from the environment
- [ ] TLS terminated in front of the server
- [ ] CORS narrowed to the portal origins that need it
