#!/bin/sh
# Initialize infra: Keycloak (realm, pga client, default-admin) + Airavata DB migrations.
# Use before starting the server in either dev or JAR mode.
#
# Usage: ./scripts/init.sh [--clean]
#   (no args)  Reuse: ensure db/redis/keycloak up, then Keycloak setup + DB migrations.
#   --clean    Clean-initialize: docker compose down -v, up -d, then setup + DB wipe + migrations.
# Env: CLEAN_INIT=1 is equivalent to --clean.
set -e

SCRIPT_DIR="$(dirname "$0")"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$ROOT/.devcontainer/docker-compose.yml"
cd "$ROOT"

# Parse --clean
CLEAN_INIT=false
for arg in "$@"; do
  case "$arg" in
    --clean) CLEAN_INIT=true ;;
  esac
done
CLEAN_INIT_ENV="${CLEAN_INIT:-}"
[ "$CLEAN_INIT_ENV" = "1" ] || [ "$CLEAN_INIT_ENV" = "true" ] || [ "$CLEAN_INIT_ENV" = "yes" ] && CLEAN_INIT=true

echo "=== Airavata init ==="
echo "Mode: $CLEAN_INIT (reuse / clean-initialize)"

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: Docker is not available."
  exit 1
fi

export COMPOSE_FILE

if [ "$CLEAN_INIT" = "true" ]; then
  echo "Clean-initialize: tearing down containers and volumes..."
  docker compose -f "$COMPOSE_FILE" down -v --remove-orphans 2>/dev/null || true
  echo "Starting core infrastructure (db, redis, keycloak)..."
  docker compose -f "$COMPOSE_FILE" up -d db redis keycloak
fi

echo "Ensuring core services are running..."
if ! docker compose -f "$COMPOSE_FILE" ps keycloak 2>/dev/null | grep -q "Up"; then
  echo "Starting core infrastructure (db, redis, keycloak)..."
  docker compose -f "$COMPOSE_FILE" up -d db redis keycloak
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
echo "Waiting for services to be healthy (max ${MAX_WAIT}s): db redis keycloak"
for svc in db redis keycloak; do
  cid=$(docker compose -f "$COMPOSE_FILE" ps -q "$svc" 2>/dev/null | head -1)
  if [ -z "$cid" ]; then
    echo "ERROR: Service $svc not found (is it running?)."
    exit 1
  fi
  if ! wait_healthy "$cid"; then
    echo "ERROR: Service $svc did not become healthy."
    exit 1
  fi
  echo "  $svc: healthy"
done
echo "All services healthy."

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

echo ""
echo "=== Init complete ==="
echo "  Keycloak: default realm, pga client, default-admin (default-admin / admin123)."
echo "  Database: Flyway migrations applied."
echo "Next: ./scripts/dev.sh serve   or   ./scripts/jar.sh serve"
echo ""
