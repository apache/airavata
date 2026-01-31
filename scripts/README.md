# Scripts

The project is started in one of two modes:

| Mode | Script | Use when |
|------|--------|----------|
| **Dev** | [dev.sh](dev.sh) | Developing from code: hot reload, optional debug (e.g. `./scripts/dev.sh serve`, `./scripts/dev.sh --debug serve`). |
| **JAR** | [jar.sh](jar.sh) | Running the built fat JAR (same artifact as tarball; no Maven at runtime). |

Infra (Keycloak + DB) is initialized once with [init.sh](init.sh) before starting in either mode.

| Script | Purpose |
|--------|---------|
| [quickstart.sh](quickstart.sh) | **One-command cold start:** build (if needed) + `init.sh --clean` + `dev.sh serve`. |
| [dev.sh](dev.sh) | **Dev mode:** run from code with hot reload; `serve` uses `spring-boot:run`, other commands use `exec:java`. Optional `--debug` (jdwp *:5005). |
| [jar.sh](jar.sh) | **JAR mode:** run from built fat JAR (looks in `distribution/` then `modules/distribution/target/`). Default command: `serve`. |
| [init.sh](init.sh) | **Infra:** Keycloak setup (realm, pga client, default-admin) + Airavata DB migrations. `--clean` for full reset (down -v, up, init). |

**Examples**

```bash
# One-command cold start (build + init --clean + serve)
./scripts/quickstart.sh

# One-time: init infra (reuse or clean)
./scripts/init.sh
./scripts/init.sh --clean

# Dev: start with hot reload (or --debug for remote debug)
./scripts/dev.sh serve
./scripts/dev.sh serve -d
./scripts/dev.sh --debug serve
./scripts/dev.sh init
./scripts/dev.sh init --clean
./scripts/dev.sh --help

# JAR: start from built JAR
./scripts/jar.sh serve
./scripts/jar.sh serve -d
./scripts/jar.sh init
./scripts/jar.sh --help
```

See the main [README.md](../README.md) for quick start and [docs/README.md](../docs/README.md) for documentation index.
