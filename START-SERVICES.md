# Starting Airavata Services

## Prerequisites

1. **Docker Socket Setup** (for tests):
   ```bash
   sudo ln -sf ~/.lima/default/sock/docker.sock /var/run/docker.sock
   ```

2. **Start DevContainer Services**:
   ```bash
   cd /Users/pjayawardana3/Projects/airavata
   docker compose -f .devcontainer/docker-compose.yml up -d
   ```

   This starts:
   - MariaDB database (port 13306)
   - RabbitMQ (ports 5672, 15672)
   - ZooKeeper (port 2181)
   - Kafka (port 9092)
   - Keycloak (port 18080)
   - SSH server (port 22222)

3. **Verify Services**:
   ```bash
   docker compose -f .devcontainer/docker-compose.yml ps
   ```

## Configuration

The main `airavata.properties` file now includes:
- `services.airavata.enabled=true` - Enables all persistence units

## Running Tests

```bash
export DOCKER_HOST=unix://$HOME/.lima/default/sock/docker.sock
mm activate airavata
mvn test -pl airavata-api
```

## Starting Airavata Service

Once dependencies are running, the Airavata service can be started via:
- Unified distribution: `bin/airavata-server.sh start`
- Individual service: `bin/airavata.sh api-orch`

The service will:
- Load persistence units (when services.airavata.enabled=true)
- Connect to MariaDB databases
- Start all configured services
