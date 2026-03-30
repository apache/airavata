#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=== Airavata Setup ==="

# 1. Start infrastructure
echo "Starting infrastructure services..."
cd "$ROOT_DIR"
docker compose up -d
echo "Waiting for services to be ready..."
sleep 10

# Verify DB is up
until docker exec airavata-db mariadb -h127.0.0.1 -uairavata -p123456 -e "SELECT 1" > /dev/null 2>&1; do
    echo "  Waiting for MariaDB..."
    sleep 2
done
echo "  MariaDB: ready"

# Verify RabbitMQ is up
until docker exec airavata-rabbitmq rabbitmqctl status > /dev/null 2>&1; do
    echo "  Waiting for RabbitMQ..."
    sleep 2
done
echo "  RabbitMQ: ready"

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
