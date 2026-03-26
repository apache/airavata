#!/bin/bash

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
  sleep 1
}

# ================================
# Start the Airavata Server
# ================================
log "Starting the Airavata Server..."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/airavata-server.sh -d start
log "Airavata Server started."

# ================================
# Start the Agent Service
# ================================
log "Starting the Agent Service..."
./apache-airavata-agent-service-0.21-SNAPSHOT/bin/agent-service.sh -d start
log "Agent Service started."

# ================================
# Start the Research Service
# ================================
log "Starting the Research Service..."
./apache-airavata-research-service-0.21-SNAPSHOT/bin/research-service.sh -d start
log "Research Service started."

# ================================
# Start the File Service
# ================================
log "Starting the File Service..."
./apache-airavata-file-server-0.21-SNAPSHOT/bin/file-service.sh -d start
log "File Service started."

# ================================
# Start the REST proxy
# ================================
log "Starting the REST proxy..."
./apache-airavata-restproxy-0.21-SNAPSHOT/bin/restproxy.sh -d start
log "REST proxy started."
