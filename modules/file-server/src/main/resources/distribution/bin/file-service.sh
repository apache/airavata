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

. `dirname $0`/setenv.sh
SERVICE_NAME="file-service"
MAIN_CLASS="org.apache.airavata.file.server.FileServerApplication"

# Capture user's working dir before changing directory
CWD="$PWD"
cd ${AIRAVATA_HOME}/bin
LOGO_FILE="${AIRAVATA_HOME}/logo.txt"

JAVA_OPTS="-Dspring.config.location=${AIRAVATA_HOME}/conf/ -Dairavata.home=${AIRAVATA_HOME} -Dlog4j.configurationFile=file:${AIRAVATA_HOME}/conf/log4j2.xml"
AIRAVATA_COMMAND=""
EXTRA_ARGS=""
SERVERS=""
IS_SUBSET=false
SUBSET=""
DEFAULT_LOG_FILE="${AIRAVATA_HOME}/logs/${SERVICE_NAME}.log"
LOG_FILE=$DEFAULT_LOG_FILE
DAEMON_MODE=false
PID_PATH_NAME="${AIRAVATA_HOME}/bin/pid-${SERVICE_NAME}"

# Function to start the service
start_service() {
  if [ "$DAEMON_MODE" = true ]; then
    echo "Starting $SERVICE_NAME in daemon mode..."
    if [ ! -f $PID_PATH_NAME ]; then
      nohup java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" ${MAIN_CLASS} ${AIRAVATA_COMMAND} $* >$LOG_FILE 2>&1 &
      echo $! >$PID_PATH_NAME
      echo "$SERVICE_NAME started ..."
    else
      echo "$SERVICE_NAME is already running ..."
    fi
  else
    echo "Starting $SERVICE_NAME in interactive mode..."
    java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" ${MAIN_CLASS} ${AIRAVATA_COMMAND} $*
  fi
}

# Function to stop the service
stop_service() {
  if [ -f $PID_PATH_NAME ]; then
    PID=$(cat $PID_PATH_NAME)
    echo "$SERVICE_NAME stopping ..."
    kill $PID
    RETRY=0
    while kill -0 $PID 2>/dev/null; do
      echo "Waiting for the process $PID to be stopped"
      RETRY=$(expr ${RETRY} + 1)
      if [ "${RETRY}" -gt "20" ]; then
        echo "Forcefully killing the process as it is not responding ..."
        kill -9 $PID
      fi
      sleep 1
    done
    echo "$SERVICE_NAME stopped ..."
    rm $PID_PATH_NAME
  else
    echo "$SERVICE_NAME is not running ..."
  fi
}

# Function to restart the service
restart_service() {
  if [ -f $PID_PATH_NAME ]; then
    PID=$(cat $PID_PATH_NAME)
    echo "$SERVICE_NAME stopping ..."
    kill $PID
    RETRY=0
    while kill -0 $PID 2>/dev/null; do
      echo "Waiting for the process $PID to be stopped"
      RETRY=$(expr ${RETRY} + 1)
      if [ "${RETRY}" -gt "20" ]; then
        echo "Forcefully killing the process as it is not responding ..."
        kill -9 $PID
      fi
      sleep 1
    done
    echo "$SERVICE_NAME stopped ..."
    rm $PID_PATH_NAME
  fi
  start_service $*
}

# Function to show help
show_help() {
  echo "Usage: file-service.sh [options] [command]"
  echo ""
  echo "Commands (daemon mode only):"
  echo "  start               Start server in daemon mode"
  echo "  stop                Stop server running in daemon mode"
  echo "  restart             Restart server in daemon mode"
  echo ""
  echo "Options:"
  echo "  -d                  Run in daemon mode"
  echo "  -xdebug             Start File Service under JPDA debugger"
  echo "  -log <LOG_FILE>     Where to redirect stdout/stderr (defaults to $DEFAULT_LOG_FILE)"
  echo "  -h                  Display this help and exit"
  echo ""
  echo "Examples:"
  echo "  file-service.sh                    # Run in interactive mode"
  echo "  file-service.sh -d start           # Start in daemon mode"
  echo "  file-service.sh -d stop            # Stop daemon"
  echo "  file-service.sh -d restart         # Restart daemon"
  echo "  file-service.sh -xdebug            # Run with debugger in interactive mode"
}

# parse command arguments
for var in "$@"; do
  case ${var} in
  -d)
    DAEMON_MODE=true
    shift
    ;;
  -xdebug)
    AIRAVATA_COMMAND="${AIRAVATA_COMMAND}"
    JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=*:8000"
    shift
    ;;
  -log)
    shift
    LOG_FILE="$1"
    shift
    # If relative path, expand to absolute path using the user's $CWD
    if [ -z "$(echo "$LOG_FILE" | egrep "^/")" ]; then
      LOG_FILE="${CWD}/${LOG_FILE}"
    fi
    ;;
  start)
    if [ "$DAEMON_MODE" = true ]; then
      start_service $*
      exit 0
    else
      echo "Error: 'start' command requires daemon mode (-d flag)"
      exit 1
    fi
    ;;
  stop)
    if [ "$DAEMON_MODE" = true ]; then
      stop_service
      exit 0
    else
      echo "Error: 'stop' command requires daemon mode (-d flag)"
      exit 1
    fi
    ;;
  restart)
    if [ "$DAEMON_MODE" = true ]; then
      restart_service $*
      exit 0
    else
      echo "Error: 'restart' command requires daemon mode (-d flag)"
      exit 1
    fi
    ;;
  -h)
    show_help
    exit 0
    ;;
  *)
    EXTRA_ARGS="${EXTRA_ARGS} ${var}"
    shift
    ;;
  esac
done

# If no specific command was given, run in interactive mode
if [ "$DAEMON_MODE" = false ]; then
  java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" ${MAIN_CLASS} ${AIRAVATA_COMMAND} $*
fi
