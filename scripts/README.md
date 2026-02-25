# Scripts

From project root:

| Script | Purpose |
|--------|---------|
| `./scripts/build.sh` | Build: `mvn clean install`. Use `--skip-tests` for compile-only. |
| `./scripts/run.sh` | **One command:** build (if needed) + init --clean + serve |
| `./scripts/init.sh` | Init infra (Keycloak + DB). `--clean` = reset. `--run` = then serve |
| `./scripts/dev.sh` | Dev mode: `serve`, `init`, etc. `--debug` for jdwp |
| `./scripts/jar.sh` | Run from built JAR |
| `./scripts/setup-echo-experiment.sh` | End-to-end smoke test (18 steps: credentials, resources, app, experiment, launch, poll) |

**Cold start:** `./scripts/init.sh --clean --run` tears down containers/volumes, brings up infra, runs Keycloak setup and DB migrations, then serves. Teardown only: `docker compose -f .devcontainer/compose.yml down -v`.
