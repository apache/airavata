#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_DIR="$(dirname "$SCRIPT_DIR")"
CONF_DIR="$BASE_DIR/conf"
LIB_DIR="$BASE_DIR/lib"
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx2g}"
exec java $JAVA_OPTS -jar "$LIB_DIR"/airavata-server-*.jar --spring.config.location="$CONF_DIR/" "$@"
