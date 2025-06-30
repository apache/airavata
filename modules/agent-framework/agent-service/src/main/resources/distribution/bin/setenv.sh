#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

# Resolve symlinks to get the real script location
PRG="$0"
while [ -L "$PRG" ]; do
  PRG=$(readlink "$PRG")
done
PRGDIR=$(dirname "$PRG")

# Set AIRAVATA_HOME if not already set
[ -z "$AIRAVATA_HOME" ] && AIRAVATA_HOME=$(cd "$PRGDIR/.." && pwd)

# Build CLASSPATH from all JAR files
CLASSPATH=$(printf "%s:" "$AIRAVATA_HOME"/lib/*.jar)
CLASSPATH=${CLASSPATH%:} # Remove trailing colon

export AIRAVATA_HOME CLASSPATH

# Common function to run Airavata services
# Usage: run_service <service_name> <main_class> <java_opts>
run_service() {
  local SERVICE_NAME="$1" MAIN_CLASS="$2" JAVA_OPTS="$3"
  # Export SERVICE_NAME as environment variable for log4j2 configuration
  export SERVICE_NAME
  local CWD="$PWD" PID_PATH_NAME="${AIRAVATA_HOME}/bin/pid-${SERVICE_NAME}"
  local DEFAULT_LOG_FILE="${AIRAVATA_HOME}/logs/${SERVICE_NAME}.log"
  local LOG_FILE="$DEFAULT_LOG_FILE" DAEMON_MODE=false EXTRA_ARGS=""

  # Help text
  local HELP_TEXT="Usage: ${SERVICE_NAME}.sh

command options:
  -d                  Run in daemon mode
  -xdebug             Start ${SERVICE_NAME} under JPDA debugger
  -log <LOG_FILE>     Where to redirect stdout/stderr (defaults to $DEFAULT_LOG_FILE)
  -h                  Display this help and exit

Daemon mode commands (use with -d):
  start               Start server in daemon mode
  stop                Stop server running in daemon mode
  restart             Restart server in daemon mode"

  cd "${AIRAVATA_HOME}/bin"

  # Helper function to stop daemon process
  stop_daemon() {
    if [[ -f "$PID_PATH_NAME" ]]; then
      local PID=$(cat "$PID_PATH_NAME")
      echo "$SERVICE_NAME stopping..."
      kill "$PID"

      local retry=0
      while kill -0 "$PID" 2>/dev/null && ((retry++ < 20)); do
        echo "[PID: $PID] Waiting for process to stop..."
        sleep 1
      done

      if kill -0 "$PID" 2>/dev/null; then
        echo "[PID: $PID] Forcefully killing non-responsive process..."
        kill -9 "$PID"
      fi

      echo "$SERVICE_NAME is now stopped."
      rm "$PID_PATH_NAME"
      return 0
    else
      echo "$SERVICE_NAME is not running."
      return 1
    fi
  }

  # Helper function to start daemon process
  start_daemon() {
    echo "Starting $SERVICE_NAME ..."
    if [[ ! -f "$PID_PATH_NAME" ]]; then
      nohup java $JAVA_OPTS -classpath "$CLASSPATH" "$MAIN_CLASS" "$@" >"$LOG_FILE" 2>&1 &
      echo $! >"$PID_PATH_NAME"
      echo "$SERVICE_NAME now running: PID $(cat "$PID_PATH_NAME")"
    else
      echo "$SERVICE_NAME already running: PID $(cat "$PID_PATH_NAME")"
    fi
  }

  # Parse command arguments
  while (($# > 0)); do
    case "$1" in
    -d) DAEMON_MODE=true ;;
    -xdebug) JAVA_OPTS+=" -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=*:8000" ;;
    -log)
      shift
      LOG_FILE="$1"
      [[ "$LOG_FILE" != /* ]] && LOG_FILE="${CWD}/${LOG_FILE}"
      ;;
    start | stop | restart)
      if [[ "$DAEMON_MODE" == true ]]; then
        case "$1" in
        start) start_daemon "$@" ;;
        stop) stop_daemon ;;
        restart)
          stop_daemon
          start_daemon "$@"
          ;;
        esac
        exit 0
      else
        EXTRA_ARGS+=" $1"
      fi
      ;;
    -h)
      echo "$HELP_TEXT"
      exit 0
      ;;
    *) EXTRA_ARGS+=" $1" ;;
    esac
    shift
  done

  # Validate daemon mode usage
  if [[ "$DAEMON_MODE" == true ]]; then
    echo "Error: Daemon mode (-d) requires one of: start, stop, restart"
    echo "Use -h for help"
    exit 1
  fi

  # Run in foreground mode
  java $JAVA_OPTS -classpath "$CLASSPATH" "$MAIN_CLASS" $EXTRA_ARGS
}
