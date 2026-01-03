#!/usr/bin/env bash

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

# This script starts the unified Airavata Spring Boot application.
# It supports two modes: 'thrift' (default) and 'rest'.

# Resolve symlinks to get the real script location
PRG="$0"
while [ -L "$PRG" ]; do
  PRG=$(readlink "$PRG")
done
PRGDIR=$(dirname "$PRG")

# Set AIRAVATA_HOME if not already set
[ -z "$AIRAVATA_HOME" ] && AIRAVATA_HOME=$(cd "$PRGDIR/.." && pwd)

# Default mode is thrift
MODE="thrift"

# Parse command line arguments
DAEMON_MODE=false
ACTION=""
EXTRA_ARGS=""
while (($# > 0)); do
  case "$1" in
    -mode)
      shift
      MODE="$1"
      if [[ "$MODE" != "thrift" && "$MODE" != "rest" ]]; then
        echo "Error: Mode must be 'thrift' or 'rest'"
        exit 1
      fi
      ;;
    -d)
      DAEMON_MODE=true
      ;;
    start|stop|restart)
      ACTION="$1"
      ;;
    -h|--help)
      echo "Usage: $0 [-mode thrift|rest] [-d] [start|stop|restart] [additional-args...]"
      echo ""
      echo "Options:"
      echo "  -mode thrift|rest    Run mode: 'thrift' (default) or 'rest'"
      echo "  -d                   Run in daemon mode"
      echo "  start                Start server in daemon mode"
      echo "  stop                 Stop server running in daemon mode"
      echo "  restart              Restart server in daemon mode"
      echo "  -h, --help           Show this help message"
      echo ""
      echo "Modes:"
      echo "  thrift: Starts Thrift API, Agent Service, Research Service, File Server, and background services"
      echo "  rest:   Starts REST Proxy instead of Thrift API, along with all other services"
      exit 0
      ;;
    *)
      EXTRA_ARGS+=" $1"
      ;;
  esac
  shift
done

# Validate daemon mode usage
if [[ "$DAEMON_MODE" == true && -z "$ACTION" ]]; then
  echo "Error: Daemon mode (-d) requires one of: start, stop, restart"
  exit 1
fi

# Set mode-specific properties
if [[ "$MODE" == "thrift" ]]; then
  MODE_ARGS="-Dservices.thrift.enabled=true -Dservices.rest.enabled=false"
else
  MODE_ARGS="-Dservices.thrift.enabled=false -Dservices.rest.enabled=true"
fi

# Main class
MAIN_CLASS="org.apache.airavata.AiravataServer"

# Java options
JAVA_OPTS="-Dairavata.config.dir=${AIRAVATA_HOME}/conf -Dairavata.home=${AIRAVATA_HOME} -Dlog4j.configurationFile=file:${AIRAVATA_HOME}/conf/log4j2.xml ${MODE_ARGS}"

# JAR file - find the distribution JAR
JAR_FILE=$(find "${AIRAVATA_HOME}/lib" -name "distribution-*.jar" | head -1)
if [[ -z "$JAR_FILE" ]]; then
  echo "Error: Could not find distribution JAR file in ${AIRAVATA_HOME}/lib"
  exit 1
fi

PID_PATH_NAME="${AIRAVATA_HOME}/bin/pid-airavata"
DEFAULT_LOG_FILE="${AIRAVATA_HOME}/logs/airavata.log"
LOG_FILE="$DEFAULT_LOG_FILE"

# Helper function to stop daemon process
stop_daemon() {
  if [[ -f "$PID_PATH_NAME" ]]; then
    local PID=$(cat "$PID_PATH_NAME")
    echo "Airavata Server stopping..."
    pkill -P "$PID" 2>/dev/null
    kill "$PID" 2>/dev/null

    local retry=0
    while kill -0 "$PID" 2>/dev/null && ((retry++ < 20)); do
      echo "[PID: $PID] Waiting for process to stop..."
      sleep 1
    done

    if kill -0 "$PID" 2>/dev/null; then
      echo "[PID: $PID] Forcefully killing non-responsive process..."
      pkill -9 -P "$PID" 2>/dev/null
      kill -9 "$PID" 2>/dev/null
    fi

    echo "Airavata Server is now stopped."
    rm "$PID_PATH_NAME"
    return 0
  else
    echo "Airavata Server is not running."
    return 1
  fi
}

# Helper function to start daemon process
start_daemon() {
  echo "Starting Airavata Server in $MODE mode..."
  if [[ ! -f "$PID_PATH_NAME" ]]; then
    nohup java $JAVA_OPTS -jar "$JAR_FILE" $EXTRA_ARGS >"$LOG_FILE" 2>&1 &
    echo $! >"$PID_PATH_NAME"
    echo "Airavata Server now running in $MODE mode: PID $(cat "$PID_PATH_NAME")"
    echo "Log file: $LOG_FILE"
  else
    echo "Airavata Server already running: PID $(cat "$PID_PATH_NAME")"
  fi
}

# Main execution
case "$ACTION" in
  start)
    if [[ "$DAEMON_MODE" == true ]]; then
      start_daemon
    else
      echo "Error: 'start' command requires daemon mode (-d)"
      exit 1
    fi
    ;;
  stop)
    if [[ "$DAEMON_MODE" == true ]]; then
      stop_daemon
    else
      echo "Error: 'stop' command requires daemon mode (-d)"
      exit 1
    fi
    ;;
  restart)
    if [[ "$DAEMON_MODE" == true ]]; then
      stop_daemon
      sleep 2
      start_daemon
    else
      echo "Error: 'restart' command requires daemon mode (-d)"
      exit 1
    fi
    ;;
  "")
    if [[ "$DAEMON_MODE" == false ]]; then
      # Foreground mode - start the application
      echo "Starting Airavata Server in $MODE mode (foreground)..."
      java $JAVA_OPTS -jar "$JAR_FILE" $EXTRA_ARGS
    else
      echo "Error: Action required in daemon mode"
      exit 1
    fi
    ;;
esac

