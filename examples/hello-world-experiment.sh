#!/usr/bin/env bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# =============================================================================
# Hello World Experiment — Airavata REST API Example
# =============================================================================
#
# Demonstrates the full experiment lifecycle against the Airavata REST API:
#   1. Create an application module
#   2. Create an application interface
#   3. Register a compute resource with SSH/SLURM job submission
#   4. Add an SSH credential
#   5. Create an application deployment
#   6. Set up a group resource profile
#   7. Create a project
#   8. Create and launch an experiment
#   9. Poll for terminal state
#
# Prerequisites:
#   - Airavata server running (./scripts/run.sh)
#   - curl and jq installed
#
# Usage:
#   ./examples/hello-world-experiment.sh [BASE_URL]
#
# Default BASE_URL: http://localhost:8080/api/v1
# =============================================================================

set -euo pipefail

BASE_URL="${1:-http://localhost:8080/api/v1}"
GATEWAY_ID="default"
USERNAME="default-admin"

echo "=== Airavata Hello World Experiment ==="
echo "API: $BASE_URL"
echo

# ---------- helper ----------
api() {
  local method="$1" path="$2"; shift 2
  curl -sf -X "$method" \
    -H "Content-Type: application/json" \
    -H "X-Gateway-ID: $GATEWAY_ID" \
    -H "X-User-Name: $USERNAME" \
    "$BASE_URL$path" "$@"
}

# ---------- 1. Application Module ----------
echo "1. Creating application module..."
MODULE_ID=$(api POST "/application-modules?gatewayId=$GATEWAY_ID" -d '{
  "appModuleName": "HelloWorld",
  "appModuleVersion": "1.0",
  "appModuleDescription": "Simple echo application"
}' | jq -r '.moduleId')
echo "   Module ID: $MODULE_ID"

# ---------- 2. Application Interface ----------
echo "2. Creating application interface..."
APP_ID=$(api POST "/application-interfaces?gatewayId=$GATEWAY_ID" -d "{
  \"applicationName\": \"HelloWorld\",
  \"applicationDescription\": \"Echo application - reads message and writes to stdout\",
  \"applicationModules\": [\"$MODULE_ID\"],
  \"applicationInputs\": [{
    \"name\": \"message\",
    \"type\": \"STRING\",
    \"userFriendlyDescription\": \"Message to echo\",
    \"inputOrder\": 0,
    \"isRequired\": true,
    \"requiredToAddedToCommandLine\": true
  }],
  \"applicationOutputs\": []
}" | jq -r '.applicationInterfaceId // .interfaceId')
echo "   Application ID: $APP_ID"

# ---------- 3. Compute Resource ----------
# Job submission interfaces are embedded in the compute resource JSON
# (no separate endpoint for adding them).
echo "3. Registering compute resource with SSH/SLURM job submission..."
COMPUTE_ID=$(api POST /compute-resources -d '{
  "hostName": "localhost",
  "resourceDescription": "Local SLURM cluster",
  "enabled": true,
  "batchQueues": [{
    "queueName": "normal",
    "queueDescription": "Default queue",
    "maxRunTime": 60,
    "maxNodes": 1,
    "maxProcessors": 4,
    "defaultNodeCount": 1,
    "defaultCPUCount": 1,
    "defaultWalltime": 5,
    "isDefaultQueue": true
  }],
  "jobSubmissionInterfaces": [{
    "jobSubmissionProtocol": "SSH",
    "priorityOrder": 0
  }]
}' | jq -r '.computeResourceId')
echo "   Compute Resource ID: $COMPUTE_ID"

# ---------- 4. SSH Credential ----------
echo "4. Creating SSH credential..."
CRED_TOKEN=$(api POST /credentials/ssh -d "{
  \"gatewayId\": \"$GATEWAY_ID\",
  \"userId\": \"$USERNAME\",
  \"description\": \"Hello-world demo credential\"
}" | jq -r '.token')
echo "   Credential Token: $CRED_TOKEN"

# ---------- 5. Application Deployment ----------
echo "5. Creating application deployment..."
DEPLOY_ID=$(api POST "/application-deployments?gatewayId=$GATEWAY_ID" -d "{
  \"appModuleId\": \"$MODULE_ID\",
  \"computeResourceId\": \"$COMPUTE_ID\",
  \"executablePath\": \"/bin/echo\",
  \"parallelism\": \"SERIAL\",
  \"appDeploymentDescription\": \"HelloWorld on local SLURM\",
  \"defaultQueueName\": \"normal\",
  \"defaultNodeCount\": 1,
  \"defaultCPUCount\": 1,
  \"defaultWalltime\": 5
}" | jq -r '.deploymentId')
echo "   Deployment ID: $DEPLOY_ID"

# ---------- 6. Group Resource Profile ----------
echo "6. Creating group resource profile..."
PROFILE_ID=$(api POST /group-resource-profiles -d "{
  \"gatewayId\": \"$GATEWAY_ID\",
  \"groupResourceProfileName\": \"HelloWorld-Profile\",
  \"computePreferences\": [{
    \"computeResourceId\": \"$COMPUTE_ID\",
    \"overridebyAiravata\": true,
    \"loginUserName\": \"root\",
    \"scratchLocation\": \"/tmp\",
    \"resourceSpecificCredentialStoreToken\": \"$CRED_TOKEN\",
    \"preferredJobSubmissionProtocol\": \"SSH\"
  }]
}" | jq -r '.groupResourceProfileId')
echo "   Profile ID: $PROFILE_ID"

# ---------- 7. Create Project ----------
echo "7. Creating project..."
PROJECT_ID=$(api POST "/projects?gatewayId=$GATEWAY_ID" -d "{
  \"name\": \"HelloWorld-Demo\",
  \"gatewayId\": \"$GATEWAY_ID\",
  \"owner\": \"$USERNAME\",
  \"description\": \"Demo project for hello-world experiment\"
}" | jq -r '.projectId')
echo "   Project ID: $PROJECT_ID"

# ---------- 8. Create Experiment ----------
echo "8. Creating experiment..."
EXP_ID=$(api POST /experiments -d "{
  \"experimentName\": \"HelloWorld-$(date +%s)\",
  \"projectId\": \"$PROJECT_ID\",
  \"gatewayId\": \"$GATEWAY_ID\",
  \"userName\": \"$USERNAME\",
  \"description\": \"Hello-world demo experiment\",
  \"experimentType\": \"SINGLE_APPLICATION\",
  \"executionId\": \"$APP_ID\",
  \"experimentInputs\": [{
    \"name\": \"message\",
    \"value\": \"Hello from Airavata!\",
    \"type\": \"STRING\"
  }],
  \"userConfigurationData\": {
    \"computationalResourceScheduling\": {
      \"resourceHostId\": \"$COMPUTE_ID\",
      \"nodeCount\": 1,
      \"totalCPUCount\": 1,
      \"wallTimeLimit\": 5,
      \"queueName\": \"normal\"
    },
    \"groupResourceProfileId\": \"$PROFILE_ID\",
    \"airavataAutoSchedule\": false,
    \"overrideManualScheduledParams\": false
  }
}" | jq -r '.experimentId')
echo "   Experiment ID: $EXP_ID"

# ---------- 9. Launch Experiment ----------
echo "9. Launching experiment..."
api POST "/experiments/$EXP_ID/launch" > /dev/null
echo "   Launched."

# ---------- 10. Poll for Completion ----------
echo "10. Polling for terminal state (timeout 5 min)..."
TIMEOUT=300
ELAPSED=0
while [ $ELAPSED -lt $TIMEOUT ]; do
  # Status is embedded in the experiment — experimentStatus is a list, take last entry
  STATE=$(api GET "/experiments/$EXP_ID" | jq -r '
    (.experimentStatus // [])
    | if length > 0 then last.state // "UNKNOWN" else "UNKNOWN" end
  ')
  echo "   [$ELAPSED s] State: $STATE"
  case "$STATE" in
    COMPLETED|FAILED|CANCELED) break ;;
  esac
  sleep 5
  ELAPSED=$((ELAPSED + 5))
done

echo
echo "=== Done ==="
echo "Final state: $STATE"
echo "Experiment ID: $EXP_ID"
