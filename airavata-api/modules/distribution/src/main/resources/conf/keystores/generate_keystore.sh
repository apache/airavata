#!/bin/bash
# Generate airavata.p12 keystore for TLS and credential encryption.
# Idempotent: skips if airavata.p12 already exists. Use --force to regenerate.
# Does NOT require sudo — all files are local to this directory.
#
# Atomic: builds into a temp file and renames on success, so a failure
# never leaves a partial airavata.p12. Temp files are cleaned up via trap.
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

FORCE=false
[ "$1" = "--force" ] && FORCE=true

if [ -f airavata.p12 ] && [ "$FORCE" != "true" ]; then
  echo "airavata.p12 already exists (use --force to regenerate)"
  exit 0
fi

# Require openssl and keytool
for cmd in openssl keytool; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "ERROR: $cmd is required but not found on PATH"
    exit 1
  fi
done

# Clean up temp files on any exit (success or failure)
cleanup() { rm -f aes.p12 airavata.p12.tmp; }
trap cleanup EXIT

# Generate self-signed cert+key if they don't exist
if [ ! -f server.crt ] || [ ! -f server.key ]; then
  echo "Generating self-signed certificate (server.crt, server.key)..."
  openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout server.key -out server.crt \
    -subj "/CN=localhost/OU=localhost/O=localhost/L=localhost/ST=localhost/C=US" \
    -addext "subjectAltName=DNS:localhost"
fi

# Generate AES-256 symmetric key for credential encryption (temporary)
keytool -genseckey -alias airavata -keyalg AES -keysize 256 \
  -keystore aes.p12 -storepass airavata -storetype PKCS12 2>/dev/null

# Build keystore from self-signed cert into temp file
openssl pkcs12 -export -name tls -out airavata.p12.tmp \
  -passout pass:airavata -in server.crt -inkey server.key

# Merge AES key into the temp keystore
keytool -importkeystore -srckeystore aes.p12 -destkeystore airavata.p12.tmp \
  -srcstorepass airavata -deststorepass airavata -noprompt 2>/dev/null

# Atomic rename: only overwrites airavata.p12 if ALL steps succeeded
rm -f airavata.p12
mv airavata.p12.tmp airavata.p12

# Disable trap cleanup for the final file (we just placed it)
trap - EXIT
rm -f aes.p12

echo "Generated airavata.p12 (TLS + AES key, password=airavata)"
