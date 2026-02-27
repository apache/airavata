#!/bin/bash
# Generate keystores for Airavata development:
#   - server.key / server.crt  — self-signed TLS cert (localhost + *.airavata.localhost)
#   - airavata.p12             — PKCS12 keystore with TLS cert + AES-256 symmetric key
#   - airavata.sym.p12         — standalone AES-256 symmetric keystore (credential encryption)
#
# Idempotent: skips if airavata.p12 already exists. Use --force to regenerate all.
# Does NOT require sudo — all files are local to this directory.
#
# Atomic: builds into temp files and renames on success, so a failure
# never leaves a partial keystore. Temp files are cleaned up via trap.
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

FORCE=false
[ "$1" = "--force" ] && FORCE=true

if [ -f airavata.p12 ] && [ -f airavata.sym.p12 ] && [ "$FORCE" != "true" ]; then
  echo "Keystores already exist (use --force to regenerate)"
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
cleanup() { rm -f aes.p12 airavata.p12.tmp airavata.sym.p12.tmp; }
trap cleanup EXIT

# --- 1. Self-signed certificate ---
if [ ! -f server.crt ] || [ ! -f server.key ] || [ "$FORCE" = "true" ]; then
  echo "Generating self-signed certificate (server.crt, server.key)..."
  openssl req -x509 -nodes -days 3650 -newkey rsa:2048 \
    -keyout server.key -out server.crt \
    -subj "/CN=localhost/OU=Airavata/O=Apache/L=Bloomington/ST=IN/C=US" \
    -addext "subjectAltName=DNS:localhost,DNS:airavata.localhost,DNS:*.airavata.localhost"
fi

# --- 2. AES-256 symmetric key (temporary, merged into both keystores) ---
keytool -genseckey -alias airavata -keyalg AES -keysize 256 \
  -keystore aes.p12 -storepass airavata -storetype PKCS12 2>/dev/null

# --- 3. airavata.p12 (TLS cert + AES key) ---
openssl pkcs12 -export -name tls -out airavata.p12.tmp \
  -passout pass:airavata -in server.crt -inkey server.key

keytool -importkeystore -srckeystore aes.p12 -destkeystore airavata.p12.tmp \
  -srcstorepass airavata -deststorepass airavata -noprompt 2>/dev/null

rm -f airavata.p12
mv airavata.p12.tmp airavata.p12

# --- 4. airavata.sym.p12 (standalone symmetric keystore for credential encryption) ---
cp aes.p12 airavata.sym.p12.tmp
rm -f airavata.sym.p12
mv airavata.sym.p12.tmp airavata.sym.p12

# Disable trap cleanup for the final files
trap - EXIT
rm -f aes.p12

echo "Generated:"
echo "  server.key, server.crt  (TLS: localhost, airavata.localhost, *.airavata.localhost)"
echo "  airavata.p12            (TLS + AES key, password=airavata)"
echo "  airavata.sym.p12        (AES key only, password=airavata, alias=airavata)"
