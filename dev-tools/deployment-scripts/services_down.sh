#!/bin/bash

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# ================================
# Shutdown the API Server
# ================================
log "Stopping the API Services..."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/airavata-server.sh -d stop -f api-orch
log "Orchestrator stopped."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/controller.sh -d stop
log "Controller stopped."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/participant.sh -d stop
log "Participant stopped."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/pre-wm.sh -d stop
log "Pre-Workflow Manager stopped."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/post-wm.sh -d stop
log "Post-Workflow Manager stopped."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/email-monitor.sh -d stop
log "Email Monitor stopped."
./apache-airavata-api-server-0.21-SNAPSHOT/bin/realtime-monitor.sh -d stop
log "Realtime Monitor stopped."

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
