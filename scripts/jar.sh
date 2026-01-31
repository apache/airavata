#!/bin/sh
# Run Airavata from the built fat JAR (no Maven at runtime).
# Use this to run the same artifact as in the tarball; requires DB, Keycloak, Redis.
#
# Usage: ./scripts/jar.sh [command] [args...]
#   Default: serve
#   Examples: ./scripts/jar.sh serve
#             ./scripts/jar.sh serve -d
#             ./scripts/jar.sh init
#             ./scripts/jar.sh --help
#
# JAR is looked up in: distribution/ then modules/distribution/target/
# Config (conf/) is taken from modules/distribution/target/classes.
set -e

SCRIPT_DIR="$(dirname "$0")"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CLASSES="$ROOT/modules/distribution/target/classes"

# Prefer JAR in distribution/ (same place as tarball), then target/
if [ -f "$ROOT/distribution/airavata-0.21-SNAPSHOT.jar" ]; then
  JAR="$ROOT/distribution/airavata-0.21-SNAPSHOT.jar"
elif [ -f "$ROOT/modules/distribution/target/airavata-0.21-SNAPSHOT.jar" ]; then
  JAR="$ROOT/modules/distribution/target/airavata-0.21-SNAPSHOT.jar"
else
  echo "JAR not found. Build first: mvn package -pl modules/distribution -DskipTests"
  exit 1
fi

if [ ! -d "$CLASSES/conf" ]; then
  echo "Config not found at $CLASSES/conf. Build first: mvn package -pl modules/distribution -DskipTests"
  exit 1
fi

export AIRAVATA_HOME="$CLASSES"
CMD="${*:-serve}"
exec java -Dairavata.home="$CLASSES" -Dairavata.config.dir="$CLASSES/conf" -jar "$JAR" $CMD
