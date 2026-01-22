#!/bin/bash
# Keycloak Configuration Setup Script
# Configures Keycloak using REST API calls for version-independent setup
#
# Required environment variables (must be set by docker-compose):
#   KEYCLOAK_URL, KEYCLOAK_ADMIN, KEYCLOAK_ADMIN_PASSWORD, REALM_NAME
#   PGA_CLIENT_SECRET, DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD
#
# Optional environment variables:
#   INCLUDE_CILOGON (default: false)
#   INCLUDE_SIDECAR_AGENT (default: false)
#   INCLUDE_JUPYTERLAB (default: false)
#   CILOGON_CLIENT_ID, CILOGON_CLIENT_SECRET (required if INCLUDE_CILOGON=true)
#   JUPYTERLAB_CLIENT_SECRET (required if INCLUDE_JUPYTERLAB=true)
#
set -e

# Validate required environment variables
required_vars=(
    "KEYCLOAK_URL"
    "KEYCLOAK_ADMIN"
    "KEYCLOAK_ADMIN_PASSWORD"
    "REALM_NAME"
    "PGA_CLIENT_SECRET"
    "DEFAULT_ADMIN_USERNAME"
    "DEFAULT_ADMIN_PASSWORD"
)

missing_vars=()
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        missing_vars+=("$var")
    fi
done

if [ ${#missing_vars[@]} -ne 0 ]; then
    echo "ERROR: Missing required environment variables:"
    for var in "${missing_vars[@]}"; do
        echo "  - $var"
    done
    exit 1
fi

# Optional variables with defaults
INCLUDE_CILOGON="${INCLUDE_CILOGON:-false}"
INCLUDE_SIDECAR_AGENT="${INCLUDE_SIDECAR_AGENT:-false}"
INCLUDE_JUPYTERLAB="${INCLUDE_JUPYTERLAB:-false}"
DEFAULT_ADMIN_EMAIL="${DEFAULT_ADMIN_EMAIL:-${DEFAULT_ADMIN_USERNAME}@${REALM_NAME}}"

# Validate CILogon vars if enabled
if [ "$INCLUDE_CILOGON" = "true" ]; then
    if [ -z "$CILOGON_CLIENT_ID" ] || [ -z "$CILOGON_CLIENT_SECRET" ]; then
        echo "ERROR: INCLUDE_CILOGON=true but CILOGON_CLIENT_ID or CILOGON_CLIENT_SECRET not set"
        exit 1
    fi
fi

# Validate JupyterLab vars if enabled
if [ "$INCLUDE_JUPYTERLAB" = "true" ]; then
    if [ -z "$JUPYTERLAB_CLIENT_SECRET" ]; then
        echo "ERROR: INCLUDE_JUPYTERLAB=true but JUPYTERLAB_CLIENT_SECRET not set"
        exit 1
    fi
fi

echo "=== Keycloak Configuration Setup ==="
echo "URL: $KEYCLOAK_URL | Realm: $REALM_NAME"

# Wait for Keycloak to be ready
wait_for_keycloak() {
    echo "Waiting for Keycloak..."
    local max_attempts=60
    for ((i=1; i<=max_attempts; i++)); do
        if curl -sf "$KEYCLOAK_URL/realms/master" >/dev/null 2>&1; then
            echo "Keycloak is ready!"
            return 0
        fi
        echo "  Attempt $i/$max_attempts..."
        sleep 2
    done
    echo "ERROR: Keycloak not ready" && exit 1
}

# Get admin access token
get_admin_token() {
    curl -sf -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=$KEYCLOAK_ADMIN&password=$KEYCLOAK_ADMIN_PASSWORD&grant_type=password&client_id=admin-cli" \
        | jq -r '.access_token'
}

# Check if realm exists
realm_exists() {
    [ "$(curl -sf -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $1" "$KEYCLOAK_URL/admin/realms/$REALM_NAME")" = "200" ]
}

# Create realm with essential settings
create_realm() {
    local token="$1"
    echo "Creating realm: $REALM_NAME"
    curl -sf -X POST "$KEYCLOAK_URL/admin/realms" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d '{
            "realm": "'"$REALM_NAME"'",
            "enabled": true,
            "registrationAllowed": false,
            "loginWithEmailAllowed": true,
            "duplicateEmailsAllowed": false,
            "sslRequired": "external",
            "accessTokenLifespan": 7200,
            "ssoSessionIdleTimeout": 604800,
            "ssoSessionMaxLifespan": 604800
        }'
    echo "  Realm created"
}

# Create realm roles
create_realm_roles() {
    local token="$1"
    echo "Creating realm roles..."
    for role in admin admin-read-only gateway-user gateway-provider user-pending; do
        curl -sf -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d '{"name": "'"$role"'"}' 2>/dev/null || true
    done
    echo "  Roles created"
}

# Create pga client (primary OAuth client)
create_pga_client() {
    local token="$1"
    echo "Creating pga client..."
    curl -sf -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d '{
            "clientId": "pga",
            "name": "Airavata Portal Client",
            "enabled": true,
            "clientAuthenticatorType": "client-secret",
            "secret": "'"$PGA_CLIENT_SECRET"'",
            "redirectUris": ["http://localhost:3000/*", "http://localhost:8080/callback*", "https://localhost:8080/auth/callback*"],
            "webOrigins": ["*"],
            "publicClient": false,
            "standardFlowEnabled": true,
            "directAccessGrantsEnabled": true,
            "serviceAccountsEnabled": true,
            "authorizationServicesEnabled": true,
            "protocol": "openid-connect",
            "attributes": {
                "oauth2.device.authorization.grant.enabled": "true",
                "post.logout.redirect.uris": "http://localhost:3000/*##http://localhost:8080/*",
                "frontchannel.logout.session.required": "false",
                "backchannel.logout.session.required": "false"
            },
            "fullScopeAllowed": true
        }'
    echo "  pga client created"
}

# Create sidecar-agent client (optional)
create_sidecar_agent_client() {
    local token="$1"
    echo "Creating sidecar-agent client..."
    curl -sf -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d '{
            "clientId": "sidecar-agent",
            "name": "Sidecar Agent",
            "enabled": true,
            "publicClient": true,
            "standardFlowEnabled": false,
            "directAccessGrantsEnabled": false,
            "attributes": {"oauth2.device.authorization.grant.enabled": "true"},
            "fullScopeAllowed": true
        }' 2>/dev/null || true
    echo "  sidecar-agent client created"
}

# Create cs-jupyterlab client (optional)
create_jupyterlab_client() {
    local token="$1"
    echo "Creating cs-jupyterlab client..."
    curl -sf -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d '{
            "clientId": "cs-jupyterlab",
            "name": "JupyterLab",
            "enabled": true,
            "clientAuthenticatorType": "client-secret",
            "secret": "'"$JUPYTERLAB_CLIENT_SECRET"'",
            "redirectUris": ["/*", "http://localhost:20000/hub/oauth_callback"],
            "publicClient": false,
            "standardFlowEnabled": true,
            "directAccessGrantsEnabled": true,
            "serviceAccountsEnabled": true,
            "fullScopeAllowed": true
        }' 2>/dev/null || true
    echo "  cs-jupyterlab client created"
}

# Create CILogon identity provider (optional)
create_cilogon_idp() {
    local token="$1"
    echo "Creating CILogon identity provider..."
    curl -sf -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/identity-provider/instances" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d '{
            "alias": "oidc",
            "displayName": "CILogon",
            "providerId": "oidc",
            "enabled": true,
            "trustEmail": true,
            "storeToken": true,
            "addReadTokenRoleOnCreate": true,
            "firstBrokerLoginFlowAlias": "first broker login",
            "config": {
                "tokenUrl": "https://cilogon.org/oauth2/token",
                "issuer": "https://cilogon.org",
                "clientAuthMethod": "client_secret_post",
                "syncMode": "IMPORT",
                "clientSecret": "'"$CILOGON_CLIENT_SECRET"'",
                "defaultScope": "openid profile email org.cilogon.userinfo",
                "userInfoUrl": "https://cilogon.org/oauth2/userinfo",
                "clientId": "'"$CILOGON_CLIENT_ID"'",
                "authorizationUrl": "https://cilogon.org/authorize",
                "logoutUrl": "https://cilogon.org/logout",
                "backchannelSupported": "false"
            }
        }'
    
    # Add mappers
    for claim_attr in "given_name:firstName" "family_name:lastName"; do
        IFS=':' read -r claim attr <<< "$claim_attr"
        curl -sf -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/identity-provider/instances/oidc/mappers" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d '{"name": "'"$claim"'", "identityProviderAlias": "oidc", "identityProviderMapper": "oidc-user-attribute-idp-mapper", "config": {"syncMode": "INHERIT", "claim": "'"$claim"'", "user.attribute": "'"$attr"'"}}' 2>/dev/null || true
    done
    echo "  CILogon configured"
}

# Grant service account roles
grant_service_account_roles() {
    local token="$1"
    echo "Configuring service account roles..."
    
    local pga_id=$(curl -sf "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients?clientId=pga" -H "Authorization: Bearer $token" | jq -r '.[0].id')
    local realm_mgmt_id=$(curl -sf "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients?clientId=realm-management" -H "Authorization: Bearer $token" | jq -r '.[0].id')
    
    [ -z "$pga_id" ] || [ "$pga_id" = "null" ] && return 0
    
    local sa_id=$(curl -sf "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients/$pga_id/service-account-user" -H "Authorization: Bearer $token" | jq -r '.id')
    [ -z "$sa_id" ] || [ "$sa_id" = "null" ] && return 0
    
    local manage_users=$(curl -sf "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients/$realm_mgmt_id/roles/manage-users" -H "Authorization: Bearer $token")
    curl -sf -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$sa_id/role-mappings/clients/$realm_mgmt_id" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "[$manage_users]" 2>/dev/null || true
    echo "  Service account configured"
}

# Create default admin user
create_admin_user() {
    local token="$1"
    echo "Creating admin user: $DEFAULT_ADMIN_USERNAME"
    
    curl -sf -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "'"$DEFAULT_ADMIN_USERNAME"'",
            "email": "'"$DEFAULT_ADMIN_EMAIL"'",
            "emailVerified": true,
            "enabled": true,
            "firstName": "Admin",
            "lastName": "User"
        }' 2>/dev/null || { echo "  User may exist"; return 0; }
    
    local user_id=$(curl -sf "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users?username=$DEFAULT_ADMIN_USERNAME" -H "Authorization: Bearer $token" | jq -r '.[0].id')
    [ -n "$user_id" ] && [ "$user_id" != "null" ] && \
        curl -sf -X PUT "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$user_id/reset-password" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d '{"type": "password", "value": "'"$DEFAULT_ADMIN_PASSWORD"'", "temporary": false}'
    
    [ -n "$user_id" ] && [ "$user_id" != "null" ] && {
        local admin_role=$(curl -sf "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles/admin" -H "Authorization: Bearer $token")
        curl -sf -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$user_id/role-mappings/realm" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "[$admin_role]" 2>/dev/null || true
    }
    echo "  Admin user created"
}

# Main
main() {
    wait_for_keycloak
    
    echo ""
    local token=$(get_admin_token)
    [ -z "$token" ] && echo "ERROR: Failed to get admin token" && exit 1
    
    if realm_exists "$token"; then
        echo "Realm '$REALM_NAME' exists. Skipping setup."
    else
        create_realm "$token"
        token=$(get_admin_token)
        create_realm_roles "$token"
        create_pga_client "$token"
        
        [ "$INCLUDE_SIDECAR_AGENT" = "true" ] && create_sidecar_agent_client "$token"
        [ "$INCLUDE_JUPYTERLAB" = "true" ] && create_jupyterlab_client "$token"
        [ "$INCLUDE_CILOGON" = "true" ] && create_cilogon_idp "$token"
        
        grant_service_account_roles "$token"
        create_admin_user "$token"
    fi
    
    echo ""
    echo "=== Setup Complete ==="
    echo "Admin Console: $KEYCLOAK_URL/admin/$REALM_NAME/console/"
    echo "User: $DEFAULT_ADMIN_USERNAME"
    echo "Client: pga"
}

main "$@"
