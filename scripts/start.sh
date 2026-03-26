#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$ROOT_DIR"

# Ensure infra is running
if ! docker compose ps --status running 2>/dev/null | grep -q airavata-db; then
    echo "Infrastructure not running. Run ./scripts/setup.sh first."
    exit 1
fi

# Build classpath
CP=$(cat airavata-api/target/cp.txt):airavata-api/target/airavata-api-0.21-SNAPSHOT.jar

echo "Starting Airavata Server..."
java -cp "$CP" org.apache.airavata.api.server.AiravataServer "$@"
