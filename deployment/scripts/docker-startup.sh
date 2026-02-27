#!/bin/bash

set -e

# Set AIRAVATA_HOME if not already set
[ -z "$AIRAVATA_HOME" ] && AIRAVATA_HOME=/opt/apache-airavata
export AIRAVATA_HOME

# Set Airavata configuration directory (defaults to AIRAVATA_HOME/conf if not explicitly set)
[ -z "$AIRAVATA_CONFIG_DIR" ] && AIRAVATA_CONFIG_DIR="${AIRAVATA_HOME}/conf"
export AIRAVATA_CONFIG_DIR

echo "Starting Apache Airavata Unified Server..."
echo "All services included in single Spring Boot application:"
echo "   - REST API"
echo "   - Orchestrator"
echo "   - Registry"
echo "   - Profile Service"
echo "   - Sharing Registry"
echo "   - Credential Store"
echo "   - All Workflow Managers"
echo "   - All Background Services"
echo "Properties file location: ${AIRAVATA_CONFIG_DIR}/application.properties"
echo "Configuration directory: $AIRAVATA_CONFIG_DIR"

# Wait for dependencies if environment variables are set
if [ -n "${DB_HOST}" ]; then
    echo "Waiting for database at ${DB_HOST}:${DB_PORT:-13306}..."
    while ! nc -z "${DB_HOST}" "${DB_PORT:-13306}"; do
        sleep 2
    done
    echo "Database is ready"
fi

if [ -n "${REDIS_HOST}" ]; then
    echo "Waiting for Redis at ${REDIS_HOST}:${REDIS_PORT:-6379}..."
    while ! nc -z "${REDIS_HOST}" "${REDIS_PORT:-6379}"; do
        sleep 2
    done
    echo "Redis is ready"
fi

# Launch the unified server as PID 1 (receives SIGTERM for graceful shutdown)
if [ -f "${AIRAVATA_HOME}/bin/airavata.sh" ]; then
    echo "Launching Airavata server..."
    exec "${AIRAVATA_HOME}/bin/airavata.sh" serve
else
    echo "ERROR: Airavata server startup script not found at ${AIRAVATA_HOME}/bin/airavata.sh"
    exit 1
fi
