#!/bin/sh
# One-command cold start: build (if needed) + init --clean + serve.
# Use for first-time setup or when you want a fresh slate.
#
# Usage: ./scripts/quickstart.sh
set -e

SCRIPT_DIR="$(dirname "$0")"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT"

echo "=== Airavata Quick Start ==="
echo ""

# Build if distribution not built
if [ ! -d "$ROOT/modules/distribution/target/classes" ]; then
  echo "Building..."
  mvn clean install -DskipTests -q
  echo ""
fi

# Init (clean) - starts Docker if needed
echo "Initializing (clean)..."
"$SCRIPT_DIR/init.sh" --clean
echo ""

# Serve
echo "Starting Airavata server..."
exec "$SCRIPT_DIR/dev.sh" serve
