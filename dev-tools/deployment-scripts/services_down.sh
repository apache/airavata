#!/bin/bash

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# ================================
# Shutdown the Unified Server
# ================================
log "Stopping the Airavata Unified Server..."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/unified-server.sh -d stop
log "Unified Server stopped."

# ================================
# Shutdown the Agent Service
# ================================
log "Stopping the Agent Service..."
./apache-airavata-agent-service-0.21-SNAPSHOT/bin/agent-service.sh -d stop
log "Agent Service stopped."

# ================================
# Shutdown the Research Service
# ================================
log "Stopping the Research Service..."
./apache-airavata-research-service-0.21-SNAPSHOT/bin/research-service.sh -d stop
log "Research Service stopped."

# ================================
# Shutdown the File Service
# ================================
log "Stopping the File Service..."
./apache-airavata-file-server-0.21-SNAPSHOT/bin/file-service.sh -d stop
log "File Service stopped."

# ================================
# Shutdown the REST proxy
# ================================
log "Stopping the REST proxy..."
./apache-airavata-restproxy-0.21-SNAPSHOT/bin/restproxy.sh -d stop
log "REST proxy stopped."