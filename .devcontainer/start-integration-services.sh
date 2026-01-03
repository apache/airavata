#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yml"
SERVICES=("db" "zookeeper" "kafka" "rabbitmq")

# Detect available compose command
if command -v nerdctl >/dev/null 2>&1 && nerdctl compose version >/dev/null 2>&1; then
    COMPOSE_CMD="nerdctl compose"
    echo "Using nerdctl compose"
elif command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
    echo "Using docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
    echo "Using docker-compose"
else
    echo "Error: No compose command found. Please install nerdctl, docker compose, or docker-compose." >&2
    exit 1
fi

echo "Starting integration services: ${SERVICES[*]}"
echo "Compose file: ${COMPOSE_FILE}"

# Start services
${COMPOSE_CMD} -f "${COMPOSE_FILE}" up -d "${SERVICES[@]}"

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 5

# Verify services are running
echo "Verifying services are running..."
for service in "${SERVICES[@]}"; do
    if ${COMPOSE_CMD} -f "${COMPOSE_FILE}" ps "${service}" | grep -q "Up"; then
        echo "✓ ${service} is running"
    else
        echo "✗ ${service} is not running" >&2
        exit 1
    fi
done

# Additional health checks
echo "Performing health checks..."

# Check MariaDB
if command -v mysql >/dev/null 2>&1; then
    if mysql -h localhost -P 13306 -u root -p123456 -e "SELECT 1" >/dev/null 2>&1; then
        echo "✓ MariaDB is accessible"
    else
        echo "⚠ MariaDB may not be ready yet (this is OK if it's still starting)"
    fi
else
    echo "⚠ mysql client not found, skipping MariaDB health check"
fi

# Check Zookeeper
if command -v nc >/dev/null 2>&1; then
    if nc -z localhost 2181 2>/dev/null; then
        echo "✓ Zookeeper is listening on port 2181"
    else
        echo "⚠ Zookeeper may not be ready yet (this is OK if it's still starting)"
    fi
else
    echo "⚠ nc (netcat) not found, skipping Zookeeper health check"
fi

# Check Kafka
if command -v nc >/dev/null 2>&1; then
    if nc -z localhost 9092 2>/dev/null; then
        echo "✓ Kafka is listening on port 9092"
    else
        echo "⚠ Kafka may not be ready yet (this is OK if it's still starting)"
    fi
else
    echo "⚠ nc (netcat) not found, skipping Kafka health check"
fi

# Check RabbitMQ
if command -v nc >/dev/null 2>&1; then
    if nc -z localhost 5672 2>/dev/null; then
        echo "✓ RabbitMQ is listening on port 5672"
    else
        echo "⚠ RabbitMQ may not be ready yet (this is OK if it's still starting)"
    fi
else
    echo "⚠ nc (netcat) not found, skipping RabbitMQ health check"
fi

echo ""
echo "Integration services started successfully!"
echo "Services are available at:"
echo "  - MariaDB: localhost:13306"
echo "  - Zookeeper: localhost:2181"
echo "  - Kafka: localhost:9092"
echo "  - RabbitMQ: localhost:5672"
echo ""
echo "You can now run tests. Tests will automatically detect and use these services."

