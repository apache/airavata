#!/bin/bash

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# ================================
# Shutdown the Airavata Server
# ================================
log "Stopping the Airavata Server..."
./airavata-server-0.21-SNAPSHOT/bin/airavata.sh stop
log "Airavata Server stopped."
