#!/usr/bin/env bash
# Integration test for Armeria gRPC + REST server
# Prerequisites: server running on localhost:9090

set -e
SERVER="localhost:9090"

echo "=== Armeria Server Integration Tests ==="

# Test 1: Health endpoint
echo -n "Health check... "
curl -sf "http://$SERVER/internal/actuator/health" | grep -q "UP" && echo "PASS" || echo "FAIL"

# Test 2: DocService
echo -n "DocService available... "
curl -sf "http://$SERVER/docs/" | grep -q "Armeria" && echo "PASS" || echo "FAIL"

# Test 3: gRPC reflection (if grpcurl is available)
if command -v grpcurl &>/dev/null; then
    echo -n "gRPC reflection... "
    grpcurl -plaintext $SERVER list | grep -q "ExperimentService" && echo "PASS" || echo "FAIL"

    echo -n "gRPC service list... "
    SERVICES=$(grpcurl -plaintext $SERVER list | wc -l)
    echo "$SERVICES services found"
fi

# Test 4: REST transcoding - GET endpoints
echo -n "REST transcoding (GET /api/v1/gateways)... "
HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "http://$SERVER/api/v1/gateways" -H "Authorization: Bearer test")
[[ "$HTTP_CODE" == "200" || "$HTTP_CODE" == "401" ]] && echo "PASS (HTTP $HTTP_CODE)" || echo "FAIL (HTTP $HTTP_CODE)"

# Test 5: REST transcoding - another endpoint
echo -n "REST transcoding (GET /api/v1/experiments)... "
HTTP_CODE=$(curl -sf -o /dev/null -w "%{http_code}" "http://$SERVER/api/v1/experiments" -H "Authorization: Bearer test")
[[ "$HTTP_CODE" == "200" || "$HTTP_CODE" == "401" ]] && echo "PASS (HTTP $HTTP_CODE)" || echo "FAIL (HTTP $HTTP_CODE)"

echo "=== Done ==="
