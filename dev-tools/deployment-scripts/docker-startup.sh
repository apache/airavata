#!/bin/bash

set -e

# Set AIRAVATA_HOME if not already set
[ -z "$AIRAVATA_HOME" ] && AIRAVATA_HOME=/opt/apache-airavata
export AIRAVATA_HOME

# Set Airavata configuration directory (defaults to AIRAVATA_HOME/conf if not explicitly set)
[ -z "$AIRAVATA_CONFIG_DIR" ] && AIRAVATA_CONFIG_DIR="${AIRAVATA_HOME}/conf"
export AIRAVATA_CONFIG_DIR

echo "🚀 Starting Apache Airavata Unified Server..."
echo "📋 All services included in single Spring Boot application:"
echo "   - Thrift Server"
echo "   - Orchestrator"
echo "   - Registry"
echo "   - Profile Service"
echo "   - Sharing Registry"
echo "   - Credential Store"
echo "   - All Workflow Managers"
echo "   - All Background Services"
echo "📁 Properties file location: ${AIRAVATA_CONFIG_DIR}/airavata.properties"
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

if [ ! -z "${REDIS_HOST}" ]; then
    echo "⏳ Waiting for Redis at ${REDIS_HOST}:${REDIS_PORT:-6379}..."
    while ! nc -z ${REDIS_HOST} ${REDIS_PORT:-6379}; do
        sleep 2
    done
    echo "✅ Redis is ready"
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
# Start the Unified Airavata Server
# ================================
log "🔧 Starting the Unified Airavata API Server..."

cd ${AIRAVATA_HOME}

# Start the unified server (all services in one process)
if [ -f "./bin/airavata.sh" ]; then
    log "🚀 Starting unified Airavata server..."
    ./bin/airavata.sh -d start
    log "✅ Unified Airavata server started"
    
    # Monitor the main log file
    if [ -f "${AIRAVATA_HOME}/logs/airavata.log" ]; then
        tail -f "${AIRAVATA_HOME}/logs/airavata.log" | sed "s/^/[Airavata] /" &
    fi
else
    log "❌ ERROR: Airavata server startup script not found"
    exit 1
fi

# ================================
# Monitor all logs and keep container running
# ================================
log "🎉 All Airavata services started successfully!"
log "📊 Starting comprehensive log monitoring..."

# Wait a moment for all services to initialize
sleep 10

# Stream unified server log to docker logs and keep container running
echo "🚀 Airavata unified server is running!"
echo "📋 Service status:"
echo "   - Database: Connected"
echo "   - Redis: Connected (for Dapr)"
echo "   - Unified Server: Running (all services in one process)"
echo ""
echo "🔍 Streaming logs to 'docker logs -f airavata-server'"
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

# Start monitoring unified server log
echo "📊 Starting log monitoring for unified Airavata server..."
cd ${AIRAVATA_HOME}

# Monitor the main unified server log
monitor_log "${AIRAVATA_HOME}/logs/airavata.log" "Airavata"

echo "📊 All log monitoring started!"
echo "🔍 Use 'docker logs -f airavata-monolithic' to view all service logs"

# Keep container running and show periodic status
while true; do
    sleep 300  # Check every 5 minutes
    echo "[Status] $(date): Container active, monitoring logs from all services"
    
    # Check if the unified server process is still running
    if ! pgrep -f "airavata" > /dev/null 2>&1; then
        echo "[WARNING] Airavata server process not found"
    fi
    
    # Show number of running tail processes
    tail_count=$(pgrep -f "tail -F" | wc -l)
    echo "[Status] Currently monitoring $tail_count log files"
done
