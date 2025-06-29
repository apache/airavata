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

. $(dirname $0)/setenv.sh
SERVICE_NAME="airavata-server"
MAIN_CLASS="org.apache.airavata.server.ServerMain"

# Capture user's working dir before changing directory
CWD="$PWD"
cd ${AIRAVATA_HOME}/bin
LOGO_FILE="${AIRAVATA_HOME}/logo.txt"

JAVA_OPTS="-Dairavata.config.dir=${AIRAVATA_HOME}/conf -Dairavata.home=${AIRAVATA_HOME} -Dlog4j.configurationFile=file:${AIRAVATA_HOME}/conf/log4j2.xml"
AIRAVATA_COMMAND=""
EXTRA_ARGS=""
SERVERS=""
IS_DAEMON_MODE=false
LOGO=true
IS_SUBSET=false
SUBSET=""
DEFAULT_LOG_FILE="${AIRAVATA_HOME}/logs/${SERVICE_NAME}.log"
LOG_FILE=$DEFAULT_LOG_FILE
PID_PATH_NAME="${AIRAVATA_HOME}/bin/pid-${SERVICE_NAME}"
FORCE=false
ACTION=""

# parse command arguments
while [ $# -gt 0 ]; do
  case $1 in
  -d)
    IS_DAEMON_MODE=true
    shift
    ;;
  -xdebug)
    AIRAVATA_COMMAND="${AIRAVATA_COMMAND}"
    JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=*:8000"
    shift
    ;;
  -security)
    JAVA_OPTS="${JAVA_OPTS} -Djava.security.manager -Djava.security.policy=${AIRAVATA_HOME}/conf/axis2.policy -Daxis2.home=${AIRAVATA_HOME}"
    shift
    ;;
  -enableLegacyTLS)
    # Enable TLS v1 and v1.1.  disableSystemPropertiesFile is needed
    # because the system properties file takes precedence. See 'man update-crypto-policies' for more info.
    JAVA_OPTS="${JAVA_OPTS} -Djava.security.policy=${AIRAVATA_HOME}/bin/enableLegacyTLS.security -Djava.security.disableSystemPropertiesFile=true"
    shift
    ;;
  -nologo)
    LOGO=false
    shift
    ;;
  -log)
    shift
    if [ $# -eq 0 ]; then
      echo "Error: -log requires a filename"
      exit 1
    fi
    LOG_FILE="$1"
    shift
    # If relative path, expand to absolute path using the user's $CWD
    if [ -z "$(echo "$LOG_FILE" | egrep "^/")" ]; then
      LOG_FILE="${CWD}/${LOG_FILE}"
    fi
    ;;
  -f | --force)
    FORCE=true
    shift
    ;;
  start)
    ACTION="start"
    shift
    ;;
  stop)
    ACTION="stop"
    shift
    ;;
  -h)
    echo "Usage: airavata-server [command-options] {start|stop}"
    echo ""
    echo "Commands:"
    echo "  start               Start Airavata server(s)"
    echo "  stop                Stop Airavata server(s)"
    echo ""
    echo "Server names (for start command):"
    echo "  apiserver           Start apiserver"
    echo "  gfac                Start gfac server"
    echo "  orchestrator        Start orchestrator server"
    echo "  credentialstore     Start credentialstore server"
    echo "  regserver           Start registry server"
    echo "  all                 Start all servers in one JVM"
    echo "  api-orch            Start API and orchestrator servers"
    echo "  execution           Start execution-related servers"
    echo ""
    echo "Command options:"
    echo "  -d                  Start server in daemon mode"
    echo "  -xdebug             Start Airavata Server under JPDA debugger"
    echo "  -nologo             Do not show airavata logo"
    echo "  -security           Enable Java 2 security"
    echo "  -enableLegacyTLS    Enable TLS v1 and v1.1"
    echo "  -f, --force         Force stop all servers (for stop command)"
    echo "  -log <LOG_FILE>     Where to redirect stdout/stderr (defaults to $DEFAULT_LOG_FILE)"
    echo "  --<key>[=<value>]   Server setting(s) to override or introduce (overrides values in airavata-server.properties)"
    echo "  -h                  Display this help and exit"
    echo ""
    echo "Examples:"
    echo "  airavata-server start apiserver"
    echo "  airavata-server -d start api-orch"
    echo "  airavata-server start -d -log custom.log all"
    echo "  airavata-server -d stop"
    echo "  airavata-server stop -f"
    exit 0
    ;;
  *)
    # Handle server names and other arguments based on action
    if [ "$ACTION" = "start" ]; then
      case $1 in
      apiserver | gfac | orchestrator | credentialstore | regserver)
        if [ -z ${SERVERS} ]; then
          SERVERS="${1}"
        else
          SERVERS="${SERVERS},${1}"
        fi
        ;;
      all | api-orch | execution)
        IS_SUBSET=true
        SUBSET="${1}"
        ;;
      *)
        EXTRA_ARGS="${EXTRA_ARGS} $1"
        ;;
      esac
    else
      EXTRA_ARGS="${EXTRA_ARGS} $1"
    fi
    shift
    ;;
  esac
done

# Check if action was specified
if [ -z "$ACTION" ]; then
  echo "Error: No action specified. Use 'start' or 'stop'"
  echo "Use -h for help"
  exit 1
fi

# Validate force flag usage
if [ "$FORCE" = true ] && [ "$ACTION" != "stop" ]; then
  echo "Error: -f/--force flag is only valid with 'stop' command"
  exit 1
fi

# Handle start action
if [ "$ACTION" = "start" ]; then
  # Construct Airavata command arguments in proper order.
  if ${IS_SUBSET}; then
    AIRAVATA_COMMAND="--servers=${SUBSET} ${AIRAVATA_COMMAND} ${EXTRA_ARGS}"
  else
    if [ -z ${SERVERS} ]; then
      echo "You should provide at least one server component to start the airavata server. Please use -h option to get more details."
      exit 1
    else
      AIRAVATA_COMMAND="--servers=${SERVERS} ${AIRAVATA_COMMAND} ${EXTRA_ARGS}"
    fi
  fi

  # Print logo file
  if ${LOGO}; then
    if [ -e ${LOGO_FILE} ]; then
      cat ${LOGO_FILE}
    fi
  fi

  if ${IS_DAEMON_MODE}; then
    echo "Starting ${SERVICE_NAME} in daemon mode..."
    echo "Redirecting output to $LOG_FILE"
    nohup java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" ${MAIN_CLASS} ${AIRAVATA_COMMAND} >$LOG_FILE 2>&1 &
    echo $! >$PID_PATH_NAME
    echo "$SERVICE_NAME started with PID $(cat $PID_PATH_NAME)"
  else
    java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" ${MAIN_CLASS} ${AIRAVATA_COMMAND}
  fi
fi

# Handle stop action
if [ "$ACTION" = "stop" ]; then
  if ${FORCE}; then
    for f in $(find . -name "server_start_*"); do
      # split file name using "_" underscore
      f_split=(${f//_/ })
      echo "Found process file : $f"
      echo -n "    Sending kill signals to process ${f_split[2]}..."
      out=$(kill -9 ${f_split[2]} 2>&1)
      if [ -z "$out" ]; then
        echo "done"
      else
        echo "failed (REASON: $out)"
      fi
      echo -n "    Removing process file..."
      out=$(rm ${f} 2>&1)
      if [ -z "$out" ]; then
        echo "done"
      else
        echo "failed (REASON: $out)"
      fi
    done
  else
    java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" ${MAIN_CLASS} stop ${AIRAVATA_COMMAND} ${EXTRA_ARGS}
  fi
fi
