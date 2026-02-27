#!/bin/sh
# Dev mode: run Airavata from code with hot reload (and optional debug).
# Use this when developing; requires Maven and built classes.
#
# Usage: ./scripts/dev.sh [options] [command] [args...]
#   command     serve | init | init --clean | --help | ...
#   options     --debug   enable remote debug (jdwp *:5005)
#
# Examples:
#   ./scripts/dev.sh serve              # foreground, hot reload
#   ./scripts/dev.sh serve -d           # background
#   ./scripts/dev.sh --debug serve      # with debug port 5005
#   ./scripts/dev.sh init               # DB migrations only
#   ./scripts/dev.sh init --clean       # DB wipe + migrations
#   ./scripts/dev.sh --help
set -e

SCRIPT_DIR="$(dirname "$0")"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT"

DEBUG=false
ARGS=""
while [ $# -gt 0 ]; do
  case "$1" in
    --debug) DEBUG=true ;;
    *)       ARGS="$ARGS $1" ;;
  esac
  shift
done
ARGS=$(echo "$ARGS" | sed 's/^ *//')

if [ "$DEBUG" = "true" ]; then
  export MAVEN_OPTS="${MAVEN_OPTS:+"$MAVEN_OPTS "}-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
fi

# AIRAVATA_HOME points to distribution src/main/resources (where conf/ lives).
# target/classes does NOT contain conf/ because build-parent redirects resources.
export AIRAVATA_HOME="${AIRAVATA_HOME:-$ROOT/modules/distribution/src/main/resources}"

# serve → spring-boot:run for hot reload; other commands → exec:java
# -nsu skips snapshot repo metadata checks (avoids 30s timeout on apache.snapshots)
case "$ARGS" in
  serve*)
    exec mvn -nsu -pl modules/distribution spring-boot:run \
      -Dspring-boot.run.jvmArguments="-Dairavata.home=$AIRAVATA_HOME -Dairavata.config.dir=$AIRAVATA_HOME/conf" \
      -Dspring-boot.run.arguments="$ARGS"
    ;;
  *)
    exec mvn -nsu -pl modules/distribution exec:java -Dexec.args="$ARGS"
    ;;
esac
