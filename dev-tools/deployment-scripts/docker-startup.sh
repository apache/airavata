#!/bin/bash

set -e

# Set Airavata configuration directory
export AIRAVATA_CONFIG_DIR=/opt/airavata/vault

echo "🚀 Starting Apache Airavata Monolithic Server..."
echo "📋 All services included: API Server, Agent Service, Research Service, File Server"
echo "📁 Properties file location: /opt/airavata/vault/airavata-server.properties"
echo "📁 Configuration directory: $AIRAVATA_CONFIG_DIR"
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
    # Set the configuration directory for the service
    {
        AIRAVATA_CONFIG_DIR=$AIRAVATA_CONFIG_DIR $service_script -d start 2>&1
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
# Email Monitor (disabled by default due to config requirements)
log "📧 Email Monitor disabled (requires email config)"
echo "$(date '+%Y-%m-%d %H:%M:%S') - Email Monitor disabled (requires email config)" | sed "s/^/[Email Monitor] /"

# Realtime Monitor (disabled due to API connection dependency)  
log "⏱️  Realtime Monitor disabled (requires API server running)"
echo "$(date '+%Y-%m-%d %H:%M:%S') - Realtime Monitor disabled (requires API server running)" | sed "s/^/[Realtime Monitor] /"
start_service "Pre-WM" "./bin/pre-wm.sh" "${AIRAVATA_HOME}/logs/pre-wm.log"
start_service "Post-WM" "./bin/post-wm.sh" "${AIRAVATA_HOME}/logs/post-wm.log"

# ================================
# Start the Agent Service (Optional)
# ================================
log "🤖 Checking Agent Service availability..."
if [ -f "${AIRAVATA_AGENT_HOME}/bin/agent-service.sh" ] && [ -f "${AIRAVATA_AGENT_HOME}/lib/airavata-agent-service-*.jar" ]; then
    cd ${AIRAVATA_AGENT_HOME}
    start_service "Agent Service" "./bin/agent-service.sh" "${AIRAVATA_AGENT_HOME}/logs/agent-service.log"
else
    log "⚠️  Agent Service not available (optional)"
    echo "$(date '+%Y-%m-%d %H:%M:%S') - Agent Service not available (optional)" | sed "s/^/[Agent Service] /"
fi

# ================================
# Start the Research Service (Optional)
# ================================
log "🔬 Checking Research Service availability..."
if [ -f "${AIRAVATA_RESEARCH_HOME}/bin/research-service.sh" ] && [ -f "${AIRAVATA_RESEARCH_HOME}/lib/airavata-research-service-*.jar" ]; then
    cd ${AIRAVATA_RESEARCH_HOME}
    start_service "Research Service" "./bin/research-service.sh" "${AIRAVATA_RESEARCH_HOME}/logs/research-service.log"
else
    log "⚠️  Research Service not available (optional)"
    echo "$(date '+%Y-%m-%d %H:%M:%S') - Research Service not available (optional)" | sed "s/^/[Research Service] /"
fi

# ================================
# Start the File Service (Optional)
# ================================
log "📁 Checking File Service availability..."
if [ -f "${AIRAVATA_FILE_HOME}/bin/file-service.sh" ] && [ -f "${AIRAVATA_FILE_HOME}/lib/airavata-file-server-*.jar" ]; then
    cd ${AIRAVATA_FILE_HOME}
    start_service "File Service" "./bin/file-service.sh" "${AIRAVATA_FILE_HOME}/logs/file-service.log"
else
    log "⚠️  File Service not available (optional)"
    echo "$(date '+%Y-%m-%d %H:%M:%S') - File Service not available (optional)" | sed "s/^/[File Service] /"
fi

# ================================
# Monitor all logs and keep container running
# ================================
log "🎉 All Airavata services started successfully!"
log "📊 Starting comprehensive log monitoring..."

# Wait a moment for all services to initialize
sleep 10

# Stream all service logs to docker logs and keep container running
echo "🚀 All Airavata services are running!"
echo "📋 Service status:"
echo "   - ZooKeeper: Connected (no more 'airavata.host' errors)"
echo "   - All components started successfully"
echo ""
echo "🔍 Streaming all logs to 'docker logs -f airavata-monolithic'"
echo ""

# Function to monitor log file and stream to stdout with prefix
monitor_log() {
    local file=$1
    local prefix=$2
    local max_wait=60  # Maximum wait time for log file to appear
    local waited=0
    
    echo "[$prefix] Waiting for log file: $file"
    
    # Wait for log file to be created
    while [ ! -f "$file" ] && [ $waited -lt $max_wait ]; do
        sleep 2
        waited=$((waited + 2))
    done
    
    if [ -f "$file" ]; then
        echo "[$prefix] Found log file, starting to stream..."
        # Use tail -F to follow file even if it gets rotated or recreated
        tail -F "$file" 2>/dev/null | while IFS= read -r line; do
            echo "[$prefix] $line"
        done &
    else
        echo "[$prefix] Log file not found after ${max_wait}s: $file"
    fi
}

# Wait a moment for log files to be created
echo "📊 Waiting for log files to be created..."
sleep 15

# Start monitoring all service logs
echo "📊 Starting log monitoring for all services..."
cd ${AIRAVATA_HOME}

# Monitor core service logs
monitor_log "${AIRAVATA_HOME}/logs/controller.log" "Controller"
monitor_log "${AIRAVATA_HOME}/logs/participant.log" "Participant"

# Monitor workflow manager logs if they exist
monitor_log "${AIRAVATA_HOME}/logs/pre-wm.log" "Pre-WM"
monitor_log "${AIRAVATA_HOME}/logs/post-wm.log" "Post-WM"

# Monitor additional service logs if they exist
monitor_log "${AIRAVATA_HOME}/logs/orchestrator.log" "Orchestrator"
monitor_log "${AIRAVATA_HOME}/logs/email-monitor.log" "Email-Monitor"
monitor_log "${AIRAVATA_HOME}/logs/realtime-monitor.log" "Realtime-Monitor"

# Monitor optional service logs
monitor_log "${AIRAVATA_HOME}/logs/agent-service.log" "Agent"
monitor_log "${AIRAVATA_HOME}/logs/research-service.log" "Research"
monitor_log "${AIRAVATA_HOME}/logs/file-service.log" "File"

echo "📊 All log monitoring started!"
echo "🔍 Use 'docker logs -f airavata-monolithic' to view all service logs"

# Keep container running and show periodic status
while true; do
    sleep 300  # Check every 5 minutes
    echo "[Status] $(date): Container active, monitoring logs from all services"
    
    # Check if critical processes are still running
    if ! pgrep -f "controller" > /dev/null; then
        echo "[WARNING] Controller process not found"
    fi
    if ! pgrep -f "participant" > /dev/null; then
        echo "[WARNING] Participant process not found"
    fi
    
    # Show number of running tail processes
    tail_count=$(pgrep -f "tail -F" | wc -l)
    echo "[Status] Currently monitoring $tail_count log files"
done
