#!/bin/sh
# One command: build + init --clean + serve
# Usage: ./scripts/run.sh
set -e

SCRIPT_DIR="$(dirname "$0")"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT"

echo "Building..."
mvn clean install -DskipTests -q
exec "$SCRIPT_DIR/init.sh" --clean --run
