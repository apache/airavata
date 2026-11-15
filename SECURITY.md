# Apache Airavata Security

## Reporting a vulnerability

Please do **not** report security problems in public issues, pull requests or mailing lists. Follow the Apache Software Foundation process: email **security@apache.org**, or see <https://www.apache.org/security/> for details. The Airavata PMC will acknowledge the report and coordinate a fix and disclosure.

Before reporting, read the security model below. It says what Airavata does and does not promise, which helps you tell a vulnerability from expected behavior. If your case falls in a grey area, report it anyway. We welcome reports about anything we say we "strive for" and will treat them as hardening work.

## Security model

This section describes the assumptions Airavata makes and the guarantees it gives. It is an input to your own threat model for a deployment, not a replacement for one.

Airavata is a multi-user service. Users register compute (Slurm clusters) and storage (SCP hosts), describe applications, and run jobs. The server is a Go HTTP API with a PostgreSQL database and a web dashboard. It reaches remote hosts over SSH.

### Deployment assumptions

Airavata assumes the operator provides the following:

- **TLS in front of the server.** The server speaks plain HTTP. Bearer tokens must not cross an untrusted network unencrypted.
- **A trusted database.** PostgreSQL is part of the trust boundary. Anyone with read access to it can read every secret Airavata stores (see [Secrets at rest](#secrets-at-rest)). Use a real password, restrict network access, and set `AIRAVATA_DB_SSLMODE` to at least `require` for a non-loopback database.
- **The root account is disabled in production.** The root token is one shared secret with `SUPER_ADMIN` power. It exists for first-time setup and testing. Set `AIRAVATA_ROOT_ACCOUNT_ENABLED=false` once a real administrator exists.
- **Narrowed CORS.** The default `AIRAVATA_CORS_ALLOWED_ORIGINS=*` is for development. Restrict it to your portal origins.
- **A trusted network path to registered hosts.** Airavata does not currently verify SSH host keys when it connects to cluster or storage hosts. It assumes the network between the server and those hosts is not under an attacker's control. Run the server on a network you trust, or restrict its egress to known hosts.
- **Trusted server hosts.** Anyone with shell access to the machine running the server, or access to its environment (for example `CILOGON_CLIENT_SECRET`), is trusted with everything the server can do.

See [`INSTALL.md`](INSTALL.md#production-notes) and the [production checklist](docs/auth.md#production-checklist).

### Roles and trust levels

| Principal | Trust placed in it |
|---|---|
| **User** (`USER`) | Untrusted with respect to other users. May act only on records they own and on records shared with them. |
| **Service account** (`sa:<name>`) | Same as a user, plus only the permissions an admin explicitly granted. Never an administrator, and some permissions can never be granted to one. |
| **Administrator** (`ADMIN`) | Trusted to see and manage platform-wide records, and to activate and suspend users. **Not** trusted with other users' SSH secrets: admins cannot read them, and the API has no way to return them. |
| **Super administrator** (`SUPER_ADMIN`, root) | Fully trusted. Holds every permission and can manage administrators. |
| **Database operator** | Fully trusted. Outside Airavata's access control entirely. |
| **Remote hosts** (clusters, storage) | Trusted by the user who registered them. A host a user points Airavata at can see the jobs and data sent to it, and can return arbitrary output. |

Roles come from Airavata's own database and are looked up on every request. A token never grants its own privileges. See [`docs/auth.md`](docs/auth.md).

### Authentication

- Users sign in through CILogon (OpenID Connect). Airavata validates each access token by introspection against the issuer, so a revoked or expired token stops working without waiting for a local expiry.
- The dashboard uses the authorization code flow with PKCE. The server redeems the code with its client secret, which never reaches the browser.
- Service account tokens are shown once, stored only as a SHA-256 digest, expire, and can be regenerated or disabled by an admin.
- New users start **suspended** and can do nothing until an administrator activates them.

### Authorization

- **Ownership comes from the token**, never from a request body, and cannot be transferred.
- **Access is the strongest of** ownership, a share naming the caller, and a share naming an active group the caller belongs to. See [`docs/sharing.md`](docs/sharing.md).
- **Admin overrides are explicit permissions** (names ending in `_any`), held through roles and visible in `GET /api/v1/users/me`.
- **Every change to a record is audited** (who, what, which record) in the same transaction as the change. Values are never written to the audit log, so it does not become a second copy of any secret. See [`docs/audit.md`](docs/audit.md).

### Secrets at rest

SSH private keys, passphrases and passwords are stored in the Airavata database. They are **write-only through the API**: no read endpoint, response model, list, share view or audit event contains them. Airavata does not currently encrypt them at the application layer, so protect them with the database and disk controls described under deployment assumptions.

An SSH credential **cannot be shared and cannot be read by anyone but its owner, administrators included.**

### Data product sharing and credential isolation

Airavata lets people use each other's data without ever handing them the credentials needed to reach it. This is a core part of the security model.

**How data is described.** A *data product* records where a file or directory is: a path on a *data storage*. A data storage records a host, a port, a login user and a reference to an *SSH credential*. Chains look like this:

```
SSH credential  ◀──  Data storage  ◀──  Data product  ◀──  Application run input / output
(owner's secret)     (host, login)      (path)              (named by id, never by path)
```

A data product names no credential. The credential is reached only through its storage, and only by the server.

**How sharing works.** The owner of a data product (or storage, virtual data directory, or Slurm cluster config) grants `READ` or `WRITE` to a user or a group. `WRITE` implies `READ`. Neither permission includes deleting the record or reading and changing its share list, which stay with the owner. Grants can be widened, narrowed or revoked, and revocation takes effect immediately.

**How a grantee uses shared data.**

1. The grantee is shown the data product (name, description, path, and their permission). They are never shown the storage's credential, only a public summary.
2. When they download the product, upload to it (with `WRITE`), or map it as an input or output of an application run, Airavata performs the transfer **itself**, authenticating to the storage host with the credential the storage owner registered.
3. The private key, passphrase and password are read inside the server to open that connection. They are not sent to the grantee and are not part of any API response or audit event.
4. The grantee needs no standing on the storage and no access to the host. The share on the product is the only thing checked. Sharing a storage is a separate, broader act that exposes every dataset on that host. Sharing one product exposes exactly that product.

The result is that access is **transparent but not transferable**. A grantee gets the data and can do what the share allows, but cannot reuse the credential elsewhere, cannot connect to the host directly using Airavata's key, and cannot keep access after the share is revoked. To end access, the owner removes the grant, and the user loses the data immediately, with nothing to rotate.

**What the guarantee covers, and what it does not.**

- Covered: a grantee cannot obtain the credential through Airavata's API, audit log or listings. Read models for credentials have no field for secret material.
- Covered: a grantee cannot touch data outside the product they were granted. The remote path always comes from the product's registered `path`, never from an uploaded file's name or a request parameter.
- Not covered: a grantee with `READ` can copy the data itself. Once they have downloaded or staged it, Airavata cannot take it back. Share data only with people you trust to hold it.
- Not covered: the same applies to jobs. A job run by a grantee executes with that grantee's code. If a shared data product is mapped as a job input, the job can read it. Anything the job does with it, such as writing it elsewhere, is outside Airavata's control.
- Not covered: the storage owner is trusted to keep the credential's access on the host narrow. The credential can reach whatever its login user can reach on that host. Register storages with accounts limited to the data you intend to share.

The same principle applies to Slurm cluster configs: sharing one lets others submit jobs under the owner's credential and allocation, without the credential being exposed.

### Behaviors that are intended (not vulnerabilities)

These answer questions that often come up in reports:

- **Administrators can see platform-wide records** such as every user, process, storage, product and cluster config, can act as the owner of those records, and can read the audit log. This is by design. They cannot read SSH credential secrets.
- **Grantees cannot see who else has access.** The share list is owner-only.
- **A grantee with `READ` access to data can copy it.** See above.
- **Users can register arbitrary hosts and run arbitrary commands on them** (for example, an application's run section). Airavata does not sandbox what a user's job does on a cluster they have access to. Cluster-side isolation is the cluster's responsibility.
- **Cluster and storage hosts are reachable from the server.** A user can point a storage or cluster at any host and port the server can reach, which includes internal addresses. If that is a concern for your network, restrict the server's egress.
- **The `root` account is powerful by design** and is an operator tool, not a user account.
- **Version and username information.** The API does not try to hide its version, and user ids (such as `cilogon:12345`) are visible to administrators and to people who share records with each other. Airavata does not treat user id enumeration by signed-in users as a vulnerability.
- **Brute-force protection is the identity provider's and the operator's job.** Airavata does not manage passwords for users. Rate limiting at the proxy is the operator's responsibility.
- **Logs.** Internal errors are returned to clients as a fixed message, with the detail logged server-side. Logs may name users, hosts and paths, so treat them as sensitive and restrict who can read them.

### Reports we want

Report any case where:

- one user can read, change or delete another user's records without ownership, a share, or an admin permission;
- an SSH private key, passphrase, password, or service account token can be obtained through the API, dashboard, logs or audit events by anyone other than the person who supplied it;
- a share continues to grant access after it is revoked, its group membership is inactive, or its record is deleted;
- a grantee can reach data beyond the product or subtree they were granted;
- an unauthenticated caller can reach an authenticated endpoint, or a token confers more than its holder's database roles and grants;
- a service account can gain a permission it should never hold.

If you find a case we do not mention, assume it is welcome.
