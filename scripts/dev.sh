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

# serve → spring-boot:run for hot reload; other commands → exec:java
# AIRAVATA_HOME points to distribution target/classes (config at target/classes/conf)
CLASSES="$ROOT/modules/distribution/target/classes"
[ -d "$CLASSES" ] || CLASSES="$ROOT/target/classes"
export AIRAVATA_HOME="${AIRAVATA_HOME:-$CLASSES}"

case "$ARGS" in
  serve*)
    exec mvn -pl modules/distribution spring-boot:run \
      -Dspring-boot.run.jvmArguments="-Dairavata.home=$AIRAVATA_HOME -Dairavata.config.dir=$AIRAVATA_HOME/conf" \
      -Dspring-boot.run.arguments="$ARGS"
    ;;
  *)
    exec mvn -pl modules/distribution exec:java -Dexec.args="$ARGS"
    ;;
esac
