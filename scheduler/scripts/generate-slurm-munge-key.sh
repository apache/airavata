#!/bin/bash

# Generate deterministic shared munge key for SLURM clusters (integration tests)
# This ensures all nodes and clusters use the same authentication key across runs

set -euo pipefail

MUNGE_KEY_FILE="tests/docker/slurm/shared-munge.key"
SEED="airavata-munge-test-seed-v1"

echo "Generating deterministic shared munge key for SLURM clusters..."

# Ensure directory exists
mkdir -p "$(dirname "$MUNGE_KEY_FILE")"

# Build a 1024-byte deterministic binary by concatenating 32-byte SHA256 digests
# of SEED-suffixes until we reach 1024 bytes (32 bytes * 32 chunks = 1024 bytes)
hex_accum=""
for i in $(seq 0 63); do
  # Each sha256 is 32 bytes -> 64 hex chars; 64 * 32 = 2048 hex chars (1024 bytes)
  chunk=$(printf "%s" "${SEED}-${i}" | sha256sum | awk '{print $1}')
  hex_accum="${hex_accum}${chunk}"
done

# Remove existing file if it exists and has restricted permissions
if [ -f "$MUNGE_KEY_FILE" ]; then
    chmod 644 "$MUNGE_KEY_FILE" 2>/dev/null || true
    rm -f "$MUNGE_KEY_FILE"
fi

# Truncate to exactly 1024 bytes (2048 hex chars) and write as binary
echo -n "${hex_accum:0:2048}" | xxd -r -p > "$MUNGE_KEY_FILE"

# Set strict permissions; ownership fixed inside containers at runtime
chmod 400 "$MUNGE_KEY_FILE" || true

echo "Deterministic shared munge key written to: $MUNGE_KEY_FILE"
echo "This key will be mounted read-only into all SLURM nodes for authentication"
