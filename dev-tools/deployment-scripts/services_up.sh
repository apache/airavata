#!/bin/bash

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
  sleep 1
}

# ================================
# Start the API Server
# ================================
log "Starting the API Services..."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/orchestrator.sh -d start api-orch
log "Orchestrator started."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/controller.sh -d start
log "Controller started."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/participant.sh -d start
log "Participant started."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/email-monitor.sh -d start
log "Email Monitor started."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/realtime-monitor.sh -d start
log "Realtime Monitor started."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/pre-wm.sh -d start
log "Pre-Workflow Manager started."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/post-wm.sh -d start
log "Post-Workflow Manager started."

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
