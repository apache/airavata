#!/bin/sh
# Run Airavata from the built fat JAR (no Maven at runtime).
# Use this to run the same artifact as in the tarball; requires DB, Keycloak, Temporal.
#
# Usage: ./scripts/jar.sh [command] [args...]
#   Default: serve
#   Examples: ./scripts/jar.sh serve
#             ./scripts/jar.sh serve -d
#             ./scripts/jar.sh init
#             ./scripts/jar.sh --help
#
# JAR is looked up in: modules/distribution/target/
# Config (conf/) is taken from modules/distribution/src/main/resources.
set -e

SCRIPT_DIR="$(dirname "$0")"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CONF_DIR="$ROOT/modules/distribution/src/main/resources/conf"

if [ -f "$ROOT/modules/distribution/target/airavata-0.21-SNAPSHOT.jar" ]; then
  JAR="$ROOT/modules/distribution/target/airavata-0.21-SNAPSHOT.jar"
else
  echo "JAR not found. Build first: mvn package -pl modules/distribution -DskipTests"
  exit 1
fi

if [ ! -d "$CONF_DIR" ]; then
  echo "Config not found at $CONF_DIR. Check modules/distribution/src/main/resources/conf."
  exit 1
fi

export AIRAVATA_HOME="$ROOT/modules/distribution/src/main/resources"
CMD="${*:-serve}"
exec java -Dairavata.home="$AIRAVATA_HOME" -Dairavata.config.dir="$CONF_DIR" -jar "$JAR" $CMD
