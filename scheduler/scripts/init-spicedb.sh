#!/bin/bash

# Initialize SpiceDB with schema
# This script should be run after SpiceDB is started

set -e

SPICEDB_HOST="localhost:50052"
SCHEMA_FILE="./db/spicedb_schema.zed"
PRESHARED_KEY="somerandomkeyhere"

echo "Waiting for SpiceDB to be ready..."
until grpcurl -plaintext -H "authorization: Bearer $PRESHARED_KEY" $SPICEDB_HOST list; do
  echo "SpiceDB is not ready yet, waiting..."
  sleep 2
done

echo "SpiceDB is ready. Loading schema..."

# Load the schema
grpcurl -plaintext -H "authorization: Bearer $PRESHARED_KEY" \
  -d @ <(echo '{"schema": "'"$(cat $SCHEMA_FILE | sed 's/"/\\"/g' | tr '\n' ' ')"'"}') \
  $SPICEDB_HOST authzed.api.v1.SchemaService/WriteSchema

echo "Schema loaded successfully!"

