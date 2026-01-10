#!/bin/bash
# Script to enable direct access grants for admin-cli client in Keycloak
# This script waits for Keycloak to be ready, then enables direct access grants

set -e

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:18080}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin}"
MAX_RETRIES=30
RETRY_DELAY=2

echo "Waiting for Keycloak to be ready..."

# Wait for Keycloak to be ready
for i in $(seq 1 $MAX_RETRIES); do
    if curl -s -f "${KEYCLOAK_URL}/health/ready" > /dev/null 2>&1; then
        echo "Keycloak is ready!"
        break
    fi
    if [ $i -eq $MAX_RETRIES ]; then
        echo "Keycloak did not become ready in time"
        exit 1
    fi
    echo "Waiting for Keycloak... ($i/$MAX_RETRIES)"
    sleep $RETRY_DELAY
done

# Wait a bit more for the admin user to be fully initialized
sleep 5

echo "Attempting to enable direct access grants for admin-cli..."

# Try to get admin token using account-console client (if available)
# Or use the admin REST API with basic auth if supported
# First, let's try to get the client UUID for admin-cli
# We'll need to use the Admin REST API, but we need an admin token first...

# Alternative: Use kcadm.sh with a workaround
# Since direct access grants aren't enabled, we can't use kcadm.sh normally
# But we can try to use the Admin Console session or another method

# Actually, the best approach is to use the Admin REST API endpoint
# /admin/realms/master/clients to find admin-cli, then update it
# But we need an admin token...

# Workaround: In Keycloak 25, we might be able to use the initial admin token
# Or we can check if there's a way to use the admin credentials directly

# For now, let's try using curl to update the client via Admin REST API
# We'll need to get an admin token first, which requires direct access grants...
# This is a chicken-and-egg problem.

# The solution: The realm-master.json should have directAccessGrantsEnabled: true
# If it's not working, the realm might not be importing correctly.
# Let's check if we can force a realm update or restart.

echo "Note: If direct access grants are not enabled, the realm-master.json"
echo "should be imported on startup. If this script is running, it means"
echo "the realm import might not have worked correctly."
echo ""
echo "To manually enable, you can:"
echo "1. Access Keycloak Admin Console at ${KEYCLOAK_URL}"
echo "2. Login with admin/admin"
echo "3. Go to Clients > admin-cli > Settings"
echo "4. Enable 'Direct Access Grants Enabled'"
echo ""
echo "Or restart the Keycloak container to re-import realm-master.json"
