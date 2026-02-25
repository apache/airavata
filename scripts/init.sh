#!/bin/sh
# Initialize infra: Keycloak (realm, pga client, default-admin) + Airavata DB migrations.
# Use before starting the server in either dev or JAR mode.
#
# Usage: ./scripts/init.sh [--clean] [--run]
#   (no args)  Reuse: ensure services up, Keycloak setup, DB migrations.
#   --clean    Full reset (down -v, up, setup, DB wipe).
#   --run      After init, start server (dev.sh serve). Use with --clean for cold start.
# Env: CLEAN_INIT=1 = --clean
set -e

SCRIPT_DIR="$(dirname "$0")"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$ROOT/.devcontainer/compose.yml"
cd "$ROOT"

# Parse args (CLEAN_INIT=1 in env = --clean)
_e="${CLEAN_INIT:-}"
CLEAN_INIT=false
RUN_AFTER=false
[ "$_e" = "1" ] || [ "$_e" = "true" ] || [ "$_e" = "yes" ] && CLEAN_INIT=true
for arg in "$@"; do
  case "$arg" in
    --clean) CLEAN_INIT=true ;;
    --run) RUN_AFTER=true ;;
  esac
done

if [ "$CLEAN_INIT" = "true" ]; then
  echo "=== Airavata init (clean) ==="
else
  echo "=== Airavata init (reuse) ==="
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: Docker is not available."
  exit 1
fi

export COMPOSE_FILE

if [ "$CLEAN_INIT" = "true" ]; then
  echo "Clean-initialize: tearing down containers and volumes..."
  docker compose -f "$COMPOSE_FILE" down -v --remove-orphans 2>/dev/null || true
  echo "Starting core infrastructure (db, keycloak, temporal)..."
  docker compose -f "$COMPOSE_FILE" up -d db keycloak temporal
fi

echo "Ensuring core services are running..."
if ! docker compose -f "$COMPOSE_FILE" ps keycloak 2>/dev/null | grep -q "Up"; then
  echo "Starting core infrastructure (db, keycloak, temporal)..."
  docker compose -f "$COMPOSE_FILE" up -d db keycloak temporal
fi

# Wait for services to be healthy (inlined)
MAX_WAIT="${WAIT_FOR_SERVICES_MAX:-120}"
wait_healthy() {
  cid="$1"
  i=0
  while [ "$i" -lt "$MAX_WAIT" ]; do
    st=$(docker inspect -f '{{.State.Health.Status}}' "$cid" 2>/dev/null || echo "unknown")
    case "$st" in
      healthy) return 0 ;;
      unhealthy) echo "  Container is unhealthy"; return 1 ;;
    esac
    i=$((i + 2))
    sleep 2
  done
  echo "  Container did not become healthy in ${MAX_WAIT}s"; return 1
}
echo "Waiting for services to be healthy (max ${MAX_WAIT}s): db keycloak temporal"
for svc in db keycloak temporal; do
  cid=$(docker compose -f "$COMPOSE_FILE" ps -q "$svc" 2>/dev/null | head -1)
  if [ -z "$cid" ]; then
    echo "ERROR: Service $svc not found (is it running?)."
    exit 1
  fi
  has_healthcheck=$(docker inspect -f '{{if .State.Health}}yes{{else}}no{{end}}' "$cid" 2>/dev/null || echo "no")
  if [ "$has_healthcheck" = "yes" ]; then
    if ! wait_healthy "$cid"; then
      echo "ERROR: Service $svc did not become healthy."
      exit 1
    fi
  else
    running=$(docker inspect -f '{{.State.Running}}' "$cid" 2>/dev/null || echo "false")
    if [ "$running" != "true" ]; then
      echo "ERROR: Service $svc is not running."
      exit 1
    fi
  fi
  echo "  $svc: ready"
done
echo "All services ready."

echo "Running Keycloak setup (realm, pga client, default-admin)..."
if ! docker compose -f "$COMPOSE_FILE" run --rm keycloak-setup; then
  echo "ERROR: Keycloak setup failed."
  exit 1
fi

echo ""
echo "Keycloak setup complete (default realm, pga client, default-admin)."
echo "Running Airavata database initialization..."
if [ "$CLEAN_INIT" = "true" ]; then
  ./scripts/dev.sh init --clean
else
  ./scripts/dev.sh init
fi

# Generate TLS keystore if missing (non-fatal; only needed when TLS is enabled)
KEYSTORE_DIR="$ROOT/modules/distribution/src/main/resources/conf/keystores"
if [ ! -f "$KEYSTORE_DIR/airavata.p12" ]; then
  if command -v openssl >/dev/null 2>&1 && command -v keytool >/dev/null 2>&1; then
    echo "Generating airavata.p12 keystore..."
    bash "$KEYSTORE_DIR/generate_keystore.sh" || echo "  (keystore generation failed — non-fatal, TLS is disabled by default)"
  else
    echo "  Skipping keystore generation (openssl or keytool not found)"
  fi
else
  echo "  airavata.p12 keystore already exists"
fi

echo ""
echo "=== Init complete ==="
echo "  Keycloak: default-admin / admin123"
echo "  Database: Flyway migrations applied."
echo "  Temporal: localhost:7233, UI http://localhost:8233"
if [ "$RUN_AFTER" = "true" ]; then
  exec "$SCRIPT_DIR/dev.sh" serve
else
  echo "Next: ./scripts/dev.sh serve"
fi
