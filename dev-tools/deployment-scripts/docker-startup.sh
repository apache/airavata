#!/bin/bash

set -e

echo "🚀 Starting Apache Airavata Monolithic Server..."
echo "📋 All services included: API Server, Agent Service, Research Service, File Server"
echo "📁 Properties file location: /opt/airavata/vault/airavata-server.properties"
echo "📊 All logs will be captured and visible via 'docker logs'"

# Wait for dependencies if environment variables are set
if [ ! -z "${DB_HOST}" ]; then
    echo "⏳ Waiting for database at ${DB_HOST}:${DB_PORT:-13306}..."
    while ! nc -z ${DB_HOST} ${DB_PORT:-13306}; do
        sleep 2
    done
    echo "✅ Database is ready"
fi

if [ ! -z "${RABBITMQ_HOST}" ]; then
    echo "⏳ Waiting for RabbitMQ at ${RABBITMQ_HOST}:${RABBITMQ_PORT:-5672}..."
    while ! nc -z ${RABBITMQ_HOST} ${RABBITMQ_PORT:-5672}; do
        sleep 2
    done
    echo "✅ RabbitMQ is ready"
fi

if [ ! -z "${ZOOKEEPER_HOST}" ]; then
    echo "⏳ Waiting for ZooKeeper at ${ZOOKEEPER_HOST}:${ZOOKEEPER_PORT:-2181}..."
    while ! nc -z ${ZOOKEEPER_HOST} ${ZOOKEEPER_PORT:-2181}; do
        sleep 2
    done
    echo "✅ ZooKeeper is ready"
fi

# Function to log with timestamp
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# Function to start a service and capture its logs
start_service() {
    local service_name=$1
    local service_script=$2
    local log_file=$3
    
    log "🔄 Starting $service_name..."
    
    # Start the service in background and capture its output
    {
        $service_script -d start 2>&1
        echo "$(date '+%Y-%m-%d %H:%M:%S') - $service_name started successfully"
    } | sed "s/^/[$service_name] /" &
    
    # Wait a moment for the service to start
    sleep 3
    
    # Start monitoring the log file in background
    if [ -f "$log_file" ]; then
        tail -f "$log_file" | sed "s/^/[$service_name] /" &
    fi
}

# ================================
# Start the API Server Components
# ================================
log "🔧 Starting the API Services..."

cd ${AIRAVATA_HOME}

# Start all API services
start_service "Orchestrator" "./bin/orchestrator.sh" "${AIRAVATA_HOME}/logs/orchestrator.log"
start_service "Controller" "./bin/controller.sh" "${AIRAVATA_HOME}/logs/controller.log"
start_service "Participant" "./bin/participant.sh" "${AIRAVATA_HOME}/logs/participant.log"
start_service "Email Monitor" "./bin/email-monitor.sh" "${AIRAVATA_HOME}/logs/email-monitor.log"
start_service "Realtime Monitor" "./bin/realtime-monitor.sh" "${AIRAVATA_HOME}/logs/realtime-monitor.log"
start_service "Pre-WM" "./bin/pre-wm.sh" "${AIRAVATA_HOME}/logs/pre-wm.log"
start_service "Post-WM" "./bin/post-wm.sh" "${AIRAVATA_HOME}/logs/post-wm.log"

# ================================
# Start the Agent Service
# ================================
log "🤖 Starting the Agent Service..."
cd ${AIRAVATA_AGENT_HOME}
start_service "Agent Service" "./bin/agent-service.sh" "${AIRAVATA_AGENT_HOME}/logs/agent-service.log"

# ================================
# Start the Research Service
# ================================
log "🔬 Starting the Research Service..."
cd ${AIRAVATA_RESEARCH_HOME}
start_service "Research Service" "./bin/research-service.sh" "${AIRAVATA_RESEARCH_HOME}/logs/research-service.log"

# ================================
# Start the File Service
# ================================
log "📁 Starting the File Service..."
cd ${AIRAVATA_FILE_HOME}
start_service "File Service" "./bin/file-service.sh" "${AIRAVATA_FILE_HOME}/logs/file-service.log"

# ================================
# Monitor all logs and keep container running
# ================================
log "🎉 All Airavata services started successfully!"
log "📊 Starting comprehensive log monitoring..."

# Wait a moment for all services to initialize
sleep 10

# Monitor all log files and keep the container running
# This ensures all logs are captured by Docker
cd ${AIRAVATA_HOME}

# Create a comprehensive log monitoring setup
{
    # Monitor all service logs with prefixes
    tail -f ${AIRAVATA_HOME}/apache-airavata-api-server/logs/orchestrator.log | sed 's/^/[API-Orchestrator] /' &
    tail -f ${AIRAVATA_HOME}/apache-airavata-api-server/logs/controller.log | sed 's/^/[API-Controller] /' &
    tail -f ${AIRAVATA_HOME}/apache-airavata-api-server/logs/participant.log | sed 's/^/[API-Participant] /' &
    tail -f ${AIRAVATA_HOME}/apache-airavata-api-server/logs/email-monitor.log | sed 's/^/[API-EmailMonitor] /' &
    tail -f ${AIRAVATA_HOME}/apache-airavata-api-server/logs/realtime-monitor.log | sed 's/^/[API-RealtimeMonitor] /' &
    tail -f ${AIRAVATA_HOME}/apache-airavata-api-server/logs/pre-wm.log | sed 's/^/[API-PreWM] /' &
    tail -f ${AIRAVATA_HOME}/apache-airavata-api-server/logs/post-wm.log | sed 's/^/[API-PostWM] /' &
    tail -f ${AIRAVATA_AGENT_HOME}/apache-airavata-agent-service/logs/agent-service.log | sed 's/^/[Agent] /' &
    tail -f ${AIRAVATA_RESEARCH_HOME}/apache-airavata-research-service/logs/research-service.log | sed 's/^/[Research] /' &
    tail -f ${AIRAVATA_FILE_HOME}/apache-airavata-file-service/logs/file-service.log | sed 's/^/[File] /' &
    
    # Keep the main process running
    wait
} | while read line; do
    echo "$line"
done 
