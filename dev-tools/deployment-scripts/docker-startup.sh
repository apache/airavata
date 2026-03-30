#!/bin/bash

set -e

# Set Airavata configuration directory
export AIRAVATA_CONFIG_DIR=/opt/airavata/vault

echo "🚀 Starting Apache Airavata Server..."
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

# ================================
# Start the Airavata Server
# ================================
log "Starting Airavata Server..."

cd ${AIRAVATA_HOME}

# Start unified Airavata server (Thrift 8930, REST 18889, gRPC 19900, Monitoring 9097)
java -jar lib/airavata-server-*.jar &
AIRAVATA_PID=$!

# ================================
# Monitor logs and keep container running
# ================================
log "Airavata Server started (PID: $AIRAVATA_PID)"
log "Starting log monitoring..."

# Wait a moment for server to initialize and create log file
sleep 5

echo "Airavata Server is running!"
echo "  Thrift:     port 8930"
echo "  REST:       port 18889"
echo "  gRPC:       port 19900"
echo "  Monitoring: port 9097"
echo ""
echo "Use 'docker logs -f' to view server logs"
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

# Monitor Airavata server logs
monitor_log "${AIRAVATA_HOME}/logs/airavata-server.log" "Airavata-Server"

echo "Log monitoring started!"

# Keep container running and show periodic status
while true; do
    sleep 300  # Check every 5 minutes
    echo "[Status] $(date): Container active, monitoring Airavata server logs"

    # Check if Airavata server process is still running
    if ! kill -0 $AIRAVATA_PID 2>/dev/null; then
        echo "[WARNING] Airavata server process not found"
    fi

    # Show number of running tail processes
    tail_count=$(pgrep -f "tail -F" | wc -l)
    echo "[Status] Currently monitoring $tail_count log files"
done
