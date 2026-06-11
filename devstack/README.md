# devstack

Shared dev-substrate kit. One colima VM runs all containers; a single shared Traefik on
`127.0.0.1:80/443` serves `https://*.airavata.host` (mkcert-trusted) across per-project
docker networks. dnsmasq maps the dev TLD to `127.0.0.1`.

---

## One-time prerequisites

### macOS

```bash
# Install deps (idempotent via Homebrew)
brew install colima docker dnsmasq mkcert

# Trust the mkcert CA in system and NSS stores
mkcert -install

# Then run setup (writes dnsmasq drop-in, /etc/resolver, starts the colima VM and Traefik)
./devstack/devstack setup   # run as your normal user — it elevates via sudo internally only for DNS
```

### Ubuntu / Debian Linux

```bash
sudo apt-get update && sudo apt-get install -y dnsmasq mkcert libnss3-tools
# colima + docker for Linux must be installed separately:
#   https://github.com/abiosoft/colima/blob/main/docs/INSTALL.md

mkcert -install

./devstack/devstack setup   # run as your normal user — it elevates via sudo internally only for DNS
```

After `setup` completes, run `tilt up` from the repo root to start the project services.

---

## Daily workflow

```bash
# First terminal — start/resume the shared substrate (idempotent, no sudo)
./devstack/devstack ensure

# Second terminal — start the project
tilt up
```

`ensure` is also the first resource in the `Tiltfile`; `tilt up` runs it automatically.

---

## Status

```bash
./devstack/devstack status
# profile=airavata project=airavata tld=airavata.host
#   INFO colima [profile=airavata] is running
#   traefik: Up 3 hours
```

---

## Restart matrix

| Scenario | Command | Effect |
|----------|---------|--------|
| Reboot (colima auto-starts) | `./devstack/devstack ensure` | Idempotent bring-up |
| colima stopped manually | `colima start -p airavata` then `ensure` | Restores VM + ingress |
| Stop everything | `./devstack/devstack down` | Stops ingress + colima VM; **global** (all projects) |
| Full reset | `./devstack/devstack reset` | Deletes the VM; **global + destructive** — prompts for profile name |

`down` and `reset` are both global — they affect every project sharing the same colima VM.
After `reset`, re-run `./devstack/devstack setup` to recreate everything from scratch.

---

## Traefik provider decision

Phase 1 uses the **docker-socket-proxy** variant (Task 1 empirically verified):

- `tecnativa/docker-socket-proxy:0.3.0` advertises a modern Docker API version to Traefik,
  bypassing the colima socket version mismatch (`client version 1.24 is too old, min 1.44`).
- Labels on project containers are the routing source of truth (no per-service file fragments).
- The file provider is also active (`traefik/dynamic/`) for the TLS store default cert and
  per-project cert entries written by `ingress_register_project`.

---

## Config

Defaults live in `~/.airavata-devstack/config.env`:

```env
DEVSTACK_PROFILE=airavata
DEVSTACK_PROJECT=airavata
DEVSTACK_TLD=airavata.host
```

Override any key before running `setup` or `ensure`; values are persisted on first `setup`.

---

## State directory layout

```
~/.airavata-devstack/
  config.env                       # profile/project/tld
  certs/
    rootCA.pem                     # mkcert CA (copied from CAROOT)
    airavata.host.pem              # wildcard+apex cert
    airavata.host-key.pem
  traefik/
    traefik.yml                    # static config (copied from devstack/traefik/)
    dynamic/
      _tls-default.yml             # default TLS store cert
      airavata.yml                 # per-project cert entry
```

All state is under `$HOME` — colima requires host mounts to be under the home directory
(not `/tmp`). `tilt down -v` removes containers but leaves this state intact; only
`devstack reset` destroys it.

---

## Hostnames

| URL | Service |
|-----|---------|
| `https://api.airavata.host` | Airavata server (gRPC + REST) |
| `https://auth.airavata.host` | Keycloak |
| `https://rabbitmq.airavata.host` | RabbitMQ management UI |
| `https://adminer.airavata.host` | Adminer (`--profile tools`) |
| `https://gateway.airavata.host` | Django portal (portals repo) |
| `db.airavata.host:3306` | MariaDB — raw TCP on host `127.0.0.1:3306` (connect with a DB client, not a browser) |
