#!/bin/bash

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
  sleep 1
}

# ================================
# Start the Airavata Server
# ================================
log "Starting the Airavata Server..."
./airavata-server-0.21-SNAPSHOT/bin/airavata.sh start
log "Airavata Server started."
