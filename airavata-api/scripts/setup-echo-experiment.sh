#!/bin/bash
set -e

BASE_URL="http://localhost:8090/api/v1"
KEYCLOAK_URL="http://localhost:18080/realms/default/protocol/openid-connect/token"
CLIENT_ID="pga"
CLIENT_SECRET="m36BXQIxX3j3VILadeHMK5IvbOeRlCCc"
USERNAME="default-admin"
PASSWORD="admin123"
GATEWAY_ID="default"

PASS=0
FAIL=0

# Helper function: authenticated API call
api() {
  local method=$1 url=$2 data=$3
  if [ -n "$data" ]; then
    curl -sf -X "$method" "$BASE_URL$url" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "$data"
  else
    curl -sf -X "$method" "$BASE_URL$url" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json"
  fi
}

check() {
  local name=$1 result=$2
  if [ -n "$result" ] && [ "$result" != "null" ] && [ "$result" != "" ]; then
    echo "  PASS: $name"
    PASS=$((PASS + 1))
  else
    echo "  FAIL: $name"
    FAIL=$((FAIL + 1))
  fi
}

echo "================================================="
echo "  Airavata REST API Smoke Test"
echo "================================================="

echo ""
echo "=== 1. Health check ==="
HEALTH=$(curl -sf "$BASE_URL/health" | python3 -c "import sys,json; print(json.load(sys.stdin)['status'])")
check "Health endpoint returns UP" "$HEALTH"

echo ""
echo "=== 2. Config endpoint ==="
CONFIG=$(curl -sf "$BASE_URL/config" | python3 -c "import sys,json; print(json.load(sys.stdin)['defaultGatewayId'])")
check "Config returns defaultGatewayId" "$CONFIG"

echo ""
echo "=== 3. Authentication (Keycloak) ==="
TOKEN=$(curl -sf -X POST "$KEYCLOAK_URL" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&username=$USERNAME&password=$PASSWORD" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")
check "Keycloak token obtained" "$TOKEN"

echo ""
echo "=== 4. Create SSH credential ==="
CRED_RESPONSE=$(api POST "/credentials/ssh" '{"description": "Smoke test SSH credential", "gatewayId": "default"}')
CRED_TOKEN=$(echo "$CRED_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
check "SSH credential created (token=$CRED_TOKEN)" "$CRED_TOKEN"

echo ""
echo "=== 5. Retrieve SSH credential ==="
CRED_DETAILS=$(api GET "/credentials/ssh/$CRED_TOKEN?gatewayId=$GATEWAY_ID")
PUBLIC_KEY=$(echo "$CRED_DETAILS" | python3 -c "import sys,json; print(json.load(sys.stdin)['publicKey'])")
check "SSH public key retrieved" "$PUBLIC_KEY"

echo ""
echo "=== 6. Deploy public key to test container ==="
SLURM_CONTAINER=$(docker ps --format '{{.Names}}' | grep -i slurm | head -1)
if [ -n "$SLURM_CONTAINER" ]; then
  docker exec "$SLURM_CONTAINER" bash -c "
    mkdir -p /home/testuser/.ssh && \
    echo '$PUBLIC_KEY' >> /home/testuser/.ssh/authorized_keys && \
    chmod 700 /home/testuser/.ssh && \
    chmod 600 /home/testuser/.ssh/authorized_keys && \
    chown -R testuser:testuser /home/testuser/.ssh
  "
  check "Public key deployed to $SLURM_CONTAINER" "ok"
else
  echo "  SKIP: No SLURM container found (start test profile)"
fi

echo ""
echo "=== 7. Create project ==="
PROJECT_RESPONSE=$(api POST "/projects?gatewayId=$GATEWAY_ID" '{
  "projectName": "Smoke Test Project",
  "description": "API smoke test project",
  "userName": "default-admin"
}')
PROJECT_ID=$(echo "$PROJECT_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['projectId'])")
check "Project created (id=$PROJECT_ID)" "$PROJECT_ID"

echo ""
echo "=== 8. List projects ==="
PROJECTS=$(api GET "/projects?gatewayId=$GATEWAY_ID")
PROJECT_COUNT=$(echo "$PROJECTS" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))")
check "Projects listed (count=$PROJECT_COUNT)" "$PROJECT_COUNT"

echo ""
echo "=== 9. Create resource ==="
RESOURCE_RESPONSE=$(api POST "/resources" '{
  "gatewayId": "default",
  "name": "Test SSH Host",
  "hostName": "localhost",
  "port": 10022,
  "description": "Local SSH test resource",
  "capabilities": {"compute": {}, "storage": {}}
}')
RESOURCE_ID=$(echo "$RESOURCE_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['resourceId'])")
check "Resource created (id=$RESOURCE_ID)" "$RESOURCE_ID"

echo ""
echo "=== 10. Get resource ==="
RESOURCE=$(api GET "/resources/$RESOURCE_ID")
RESOURCE_NAME=$(echo "$RESOURCE" | python3 -c "import sys,json; print(json.load(sys.stdin)['name'])")
check "Resource retrieved (name=$RESOURCE_NAME)" "$RESOURCE_NAME"

echo ""
echo "=== 11. Create resource binding ==="
BINDING_RESPONSE=$(api POST "/bindings" "{
  \"credentialId\": \"$CRED_TOKEN\",
  \"resourceId\": \"$RESOURCE_ID\",
  \"loginUsername\": \"testuser\",
  \"enabled\": true,
  \"gatewayId\": \"$GATEWAY_ID\"
}")
BINDING_ID=$(echo "$BINDING_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['bindingId'])")
check "Resource binding created (id=$BINDING_ID)" "$BINDING_ID"

echo ""
echo "=== 12. Create application ==="
APP_RESPONSE=$(api POST "/applications" '{
  "gatewayId": "default",
  "ownerName": "default-admin",
  "name": "Echo",
  "version": "1.0",
  "description": "Simple echo application for testing",
  "inputs": [
    {"name": "message", "type": "STRING", "description": "Message to echo", "required": true}
  ],
  "outputs": [
    {"name": "stdout", "type": "STRING", "description": "Standard output"}
  ],
  "runScript": "#!/bin/bash\necho \"$message\"",
  "scope": "GATEWAY"
}')
APP_ID=$(echo "$APP_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['applicationId'])")
check "Application created (id=$APP_ID)" "$APP_ID"

echo ""
echo "=== 13. Get application ==="
APP=$(api GET "/applications/$APP_ID")
APP_NAME=$(echo "$APP" | python3 -c "import sys,json; print(json.load(sys.stdin)['name'])")
check "Application retrieved (name=$APP_NAME)" "$APP_NAME"

echo ""
echo "=== 14. List applications ==="
APPS=$(api GET "/applications?gatewayId=$GATEWAY_ID")
APP_COUNT=$(echo "$APPS" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))")
check "Applications listed (count=$APP_COUNT)" "$APP_COUNT"

echo ""
echo "=== 15. Create application installation ==="
INSTALL_RESPONSE=$(api POST "/installations" "{
  \"applicationId\": \"$APP_ID\",
  \"resourceId\": \"$RESOURCE_ID\",
  \"loginUsername\": \"testuser\",
  \"installPath\": \"/bin/echo\",
  \"status\": \"INSTALLED\"
}")
INSTALL_ID=$(echo "$INSTALL_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['installationId'])")
check "Installation created (id=$INSTALL_ID)" "$INSTALL_ID"

echo ""
echo "=== 16. Create experiment ==="
EXPERIMENT_RESPONSE=$(api POST "/experiments" "{
  \"experimentName\": \"Echo Smoke Test $(date +%s)\",
  \"projectId\": \"$PROJECT_ID\",
  \"gatewayId\": \"$GATEWAY_ID\",
  \"userName\": \"default-admin\",
  \"description\": \"API smoke test experiment\",
  \"applicationId\": \"$APP_ID\",
  \"bindingId\": \"$BINDING_ID\",
  \"inputs\": [{\"name\": \"message\", \"value\": \"Hello from Airavata\", \"type\": \"STRING\"}],
  \"scheduling\": {\"totalCPUCount\": 1, \"nodeCount\": 1, \"wallTimeLimit\": 15}
}")
EXPERIMENT_ID=$(echo "$EXPERIMENT_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['experimentId'])")
check "Experiment created (id=$EXPERIMENT_ID)" "$EXPERIMENT_ID"

echo ""
echo "=== 17. Get experiment ==="
EXPERIMENT=$(api GET "/experiments/$EXPERIMENT_ID")
EXP_NAME=$(echo "$EXPERIMENT" | python3 -c "import sys,json; print(json.load(sys.stdin)['experimentName'])")
check "Experiment retrieved (name=$EXP_NAME)" "$EXP_NAME"

echo ""
echo "=== 18. Swagger UI accessible ==="
SWAGGER_STATUS=$(curl -sf -o /dev/null -w "%{http_code}" "${BASE_URL%/api/v1}/swagger-ui/index.html" 2>/dev/null || echo "000")
check "Swagger UI accessible (HTTP $SWAGGER_STATUS)" "$([ "$SWAGGER_STATUS" = "200" ] || [ "$SWAGGER_STATUS" = "302" ] && echo ok)"

echo ""
echo "================================================="
echo "  Results: $PASS passed, $FAIL failed"
echo "================================================="
echo ""
echo "=== Resource Summary ==="
echo "Credential Token: $CRED_TOKEN"
echo "Project ID:       $PROJECT_ID"
echo "Resource ID:      $RESOURCE_ID"
echo "Binding ID:       $BINDING_ID"
echo "Application ID:   $APP_ID"
echo "Installation ID:  $INSTALL_ID"
echo "Experiment ID:    $EXPERIMENT_ID"

if [ $FAIL -gt 0 ]; then
  exit 1
fi
