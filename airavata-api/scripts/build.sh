#!/bin/sh
# Build Airavata: compile and install all modules.
# Use this to verify the project compiles (and optionally run tests).
#
# Usage: ./scripts/build.sh [options]
#   (no args)     mvn clean install (with tests)
#   --skip-tests  mvn clean install -DskipTests
set -e

SCRIPT_DIR="$(dirname "$0")"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT"

SKIP_TESTS=false
for arg in "$@"; do
  case "$arg" in
    --skip-tests) SKIP_TESTS=true ;;
  esac
done

if [ "$SKIP_TESTS" = "true" ]; then
  exec mvn clean install -DskipTests
else
  exec mvn clean install
fi
