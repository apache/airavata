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
      echo "Usage: $0 [-mode thrift|rest] [-d] [start|stop|restart]"
      echo ""
      echo "Options:"
      echo "  -mode thrift|rest    Run mode: 'thrift' (default) or 'rest'"
      echo "  -d                   Run in daemon mode"
      echo "  start                Start all services"
      echo "  stop                 Stop all services"
      echo "  restart              Restart all services"
      echo "  -h, --help           Show this help message"
      echo ""
      echo "Modes:"
      echo "  thrift: Starts API Server with Thrift API (default)"
      echo "  rest:   Starts REST Proxy and API Server (without Thrift API)"
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      echo "Use -h for help"
      exit 1
      ;;
  esac
  shift
done

# Validate daemon mode usage
if [[ "$DAEMON_MODE" == true && -z "$ACTION" ]]; then
  echo "Error: Daemon mode (-d) requires one of: start, stop, restart"
  exit 1
fi

# Logging function
log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# Service directories
API_SERVER_DIR="${AIRAVATA_HOME}/services/api-server"
AGENT_SERVICE_DIR="${AIRAVATA_HOME}/services/agent-service"
FILE_SERVER_DIR="${AIRAVATA_HOME}/services/file-server"
RESEARCH_SERVICE_DIR="${AIRAVATA_HOME}/services/research-service"
restapi_DIR="${AIRAVATA_HOME}/services/restapi"

# Function to start a service
start_service() {
  local service_name=$1
  local service_dir=$2
  local service_script=$3
  
  if [[ ! -f "$service_dir/$service_script" ]]; then
    log "Warning: $service_name script not found at $service_dir/$service_script"
    return 1
  fi
  
  log "Starting $service_name..."
  cd "$service_dir"
  if [[ "$DAEMON_MODE" == true ]]; then
    ./$service_script -d start
  else
    ./$service_script &
  fi
  sleep 2
  log "$service_name started."
}

# Function to stop a service
stop_service() {
  local service_name=$1
  local service_dir=$2
  local service_script=$3
  
  if [[ ! -f "$service_dir/$service_script" ]]; then
    log "Warning: $service_name script not found at $service_dir/$service_script"
    return 1
  fi
  
  log "Stopping $service_name..."
  cd "$service_dir"
  ./$service_script -d stop
  log "$service_name stopped."
}

# Function to start all services
start_all() {
  log "Starting Airavata in $MODE mode..."
  
  # Always start core services
  start_service "Agent Service" "$AGENT_SERVICE_DIR" "bin/agent-service.sh"
  start_service "File Server" "$FILE_SERVER_DIR" "bin/file-service.sh"
  start_service "Research Service" "$RESEARCH_SERVICE_DIR" "bin/research-service.sh"
  
  # Mode-specific services
  if [[ "$MODE" == "thrift" ]]; then
    log "Starting API Server with Thrift API (includes all background services)..."
    cd "$API_SERVER_DIR"
    if [[ "$DAEMON_MODE" == true ]]; then
      ./bin/airavata.sh -d start api-orch
    else
      ./bin/airavata.sh api-orch &
    fi
    log "API Server with Thrift API started."
  else
    # REST mode: Start REST proxy and API server without Thrift
    log "Starting REST Proxy..."
    start_service "REST Proxy" "$restapi_DIR" "bin/restapi.sh"
    
    log "Starting API Server (without Thrift API, includes all background services)..."
    cd "$API_SERVER_DIR"
    # Disable Thrift by setting the property
    if [[ "$DAEMON_MODE" == true ]]; then
      ./bin/airavata.sh -d start api-orch --services.thrift.enabled=false
    else
      ./bin/airavata.sh api-orch --services.thrift.enabled=false &
    fi
    log "API Server (without Thrift) started."
  fi
  
  log "All services started successfully!"
}

# Function to stop all services
stop_all() {
  log "Stopping all Airavata services..."
  
  # Stop mode-specific services first
  if [[ "$MODE" == "rest" ]]; then
    stop_service "REST Proxy" "$restapi_DIR" "bin/restapi.sh"
  fi
  
  # Stop API server (this will stop all background services internally)
  log "Stopping API Server (includes all background services)..."
  cd "$API_SERVER_DIR"
  ./bin/airavata.sh -d stop
  
  # Stop core services
  stop_service "Research Service" "$RESEARCH_SERVICE_DIR" "bin/research-service.sh"
  stop_service "File Server" "$FILE_SERVER_DIR" "bin/file-service.sh"
  stop_service "Agent Service" "$AGENT_SERVICE_DIR" "bin/agent-service.sh"
  
  log "All services stopped."
}

# Main execution
case "$ACTION" in
  start)
    start_all
    ;;
  stop)
    stop_all
    ;;
  restart)
    stop_all
    sleep 3
    start_all
    ;;
  "")
    if [[ "$DAEMON_MODE" == false ]]; then
      # Foreground mode - start all services
      start_all
      # Wait for all background processes
      wait
    else
      echo "Error: Action required in daemon mode"
      exit 1
    fi
    ;;
esac

