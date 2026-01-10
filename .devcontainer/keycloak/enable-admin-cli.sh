#!/bin/bash
# Script to enable direct access grants for admin-cli client in Keycloak master realm
# Run this inside the Keycloak container or via docker exec

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:18080}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin}"

echo "Enabling direct access grants for admin-cli client..."

# Get admin token using account-console or create a temporary admin user token
# Note: This requires Keycloak Admin REST API to be enabled

# Try using kcadm.sh if available
if command -v kcadm.sh &> /dev/null; then
    kcadm.sh config credentials --server "$KEYCLOAK_URL" --realm master --user "$ADMIN_USER" --password "$ADMIN_PASS"
    kcadm.sh update clients/master -s '{"directAccessGrantsEnabled":true}' -r master
    echo "Direct access grants enabled via kcadm.sh"
    exit 0
fi

# Fallback: Use Admin REST API with basic auth (if enabled)
echo "Note: Keycloak container needs to be restarted with realm-master.json to enable direct access grants."
echo "Or run: docker exec keycloak /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:18080 --realm master --user admin --password admin"
echo "Then: docker exec keycloak /opt/keycloak/bin/kcadm.sh update clients/master -s '{\"directAccessGrantsEnabled\":true}' -r master"
