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

JAR="airavata-server/target/airavata-server-0.21-SNAPSHOT.jar"
if [ ! -f "$JAR" ]; then
    echo "Server JAR not found. Run ./scripts/setup.sh first."
    exit 1
fi

echo "Starting Airavata Server..."
java -jar "$JAR" "$@"
