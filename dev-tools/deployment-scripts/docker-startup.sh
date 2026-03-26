#!/bin/bash

set -e

# Set Airavata configuration directory
export AIRAVATA_CONFIG_DIR=/opt/airavata/vault

echo "🚀 Starting Apache Airavata Unified Server..."
echo "📋 Single JVM server with all Thrift services multiplexed on port 8930"
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
# Start the Unified Server
# ================================
log "🔧 Starting Airavata Unified Server..."

cd ${AIRAVATA_HOME}

# Start unified server - single JVM with all services multiplexed on port 8930
start_service "Airavata Unified" "./bin/unified-server.sh" "${AIRAVATA_HOME}/logs/unified-server.log"

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
# Monitor logs and keep container running
# ================================
log "🎉 Airavata Unified Server started successfully!"
log "📊 Starting log monitoring..."

# Wait a moment for server to initialize
sleep 5

# Stream server logs to docker logs and keep container running
echo "🚀 Airavata Unified Server is running on port 8930!"
echo "📋 Server status:"
echo "   - Single JVM with all Thrift services multiplexed"
echo "   - Services: Airavata, RegistryService, SharingRegistry, CredentialStore"
echo "   - Additional: UserProfile, TenantProfile, IamAdminServices, GroupManager"
echo ""
echo "🔍 Streaming logs to 'docker logs -f airavata-monolithic'"
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

# Wait a moment for log file to be created
echo "📊 Waiting for unified server log file to be created..."
sleep 5

# Start monitoring unified server logs
echo "📊 Starting log monitoring for unified server..."
cd ${AIRAVATA_HOME}

# Monitor unified server logs
monitor_log "${AIRAVATA_HOME}/logs/unified-server.log" "Unified-Server"

echo "📊 Log monitoring started!"
echo "🔍 Use 'docker logs -f airavata-monolithic' to view server logs"

# Keep container running and show periodic status
while true; do
    sleep 300  # Check every 5 minutes
    echo "[Status] $(date): Container active, monitoring unified server logs"

    # Check if unified server process is still running
    if ! pgrep -f "AiravataUnifiedServer" > /dev/null; then
        echo "[WARNING] Unified server process not found"
    fi

    # Show number of running tail processes
    tail_count=$(pgrep -f "tail -F" | wc -l)
    echo "[Status] Currently monitoring $tail_count log files"
done
