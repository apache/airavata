#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=== Airavata Setup ==="

# 1. Start infrastructure
echo "Starting infrastructure services..."
cd "$ROOT_DIR"
docker compose up -d
echo "Waiting for services to be healthy..."

# Wait for all compose healthchecks to pass
for svc in db rabbitmq zookeeper kafka keycloak; do
    printf "  %s: " "$svc"
    until [ "$(docker inspect --format='{{.State.Health.Status}}' "airavata-$svc" 2>/dev/null)" = "healthy" ]; do
        printf "."
        sleep 3
    done
    echo " ready"
done
echo "All infrastructure healthy."

# 2. Generate keystores if missing
if [ ! -f "$ROOT_DIR/keystores/airavata.p12" ]; then
    echo "Generating keystores..."
    cd "$ROOT_DIR/keystores"
    keytool -genseckey -alias airavata -keyalg AES -keysize 256 \
        -keystore aes.p12 -storepass airavata -storetype PKCS12 2>/dev/null
    openssl pkcs12 -export -name tls -out airavata.p12 -passout pass:airavata \
        -in server.crt -inkey server.key 2>/dev/null
    keytool -importkeystore -srckeystore aes.p12 -destkeystore airavata.p12 \
        -srcstorepass airavata -deststorepass airavata -noprompt 2>/dev/null
    rm -f aes.p12
    echo "  Keystores generated"
fi

# 3. Build
echo "Building Airavata..."
cd "$ROOT_DIR"
mvn package -DskipTests -T4 -q 2>&1 | tail -1
echo "  Build complete"

echo ""
echo "=== Setup Complete ==="
echo ""
echo "To start Airavata:"
echo "  cd $ROOT_DIR"
echo "  ./scripts/start.sh"
