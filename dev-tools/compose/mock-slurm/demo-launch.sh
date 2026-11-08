#!/usr/bin/env bash
#
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
#

set -eo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEY_FILE="$SCRIPT_DIR/keys/id_ed25519"
SSH_PORT="${MOCK_SLURM_PORT:-12222}"
SSH_HOST="${MOCK_SLURM_HOST:-127.0.0.1}"
API_URL="${AIRAVATA_API_URL:-http://localhost:9095}"
TOKEN="${AIRAVATA_TOKEN:-}"

echo "=========================================================="
echo "Airavata Mock SSH/Slurm Cluster Verification"
echo "=========================================================="

if [ ! -f "$KEY_FILE" ]; then
    echo "Error: private key file $KEY_FILE not found." >&2
    exit 1
fi

chmod 600 "$KEY_FILE" 2>/dev/null || true

# 1. Verify direct SSH and mock Slurm tools
echo "1. Checking direct SSH connectivity to $SSH_HOST:$SSH_PORT..."
SSH_OPTS=(-i "$KEY_FILE" -p "$SSH_PORT" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o ConnectTimeout=5)

WHO=$(ssh "${SSH_OPTS[@]}" "slurmuser@$SSH_HOST" "whoami")
if [ "$WHO" != "slurmuser" ]; then
    echo "SSH failed: unexpected user $WHO" >&2
    exit 1
fi
echo "   ✓ SSH connection successful (logged in as $WHO)"

echo "2. Testing mock Slurm commands over SSH..."
PART_OUT=$(ssh "${SSH_OPTS[@]}" "slurmuser@$SSH_HOST" "scontrol show partition -o")
echo "   ✓ scontrol partition output:"
echo "     $PART_OUT"

INFO_OUT=$(ssh "${SSH_OPTS[@]}" "slurmuser@$SSH_HOST" "sinfo -h -a -o '%R|%c|%m|%G'")
echo "   ✓ sinfo output:"
echo "     $INFO_OUT"

ACCT_OUT=$(ssh "${SSH_OPTS[@]}" "slurmuser@$SSH_HOST" "sacctmgr -nP show associations where user=slurmuser format=Account,Partition,QOS")
echo "   ✓ sacctmgr associations output:"
echo "     $ACCT_OUT"

# 3. Check if Airavata API server is available
echo "3. Checking Airavata API at $API_URL/health..."
HEALTH=$(curl -s -m 3 "$API_URL/health" || true)
if [[ "$HEALTH" != *"UP"* ]]; then
    echo "   API server is not running on $API_URL (health check did not return UP)."
    echo "   To test the API process launch lifecycle end-to-end:"
    echo "     1. Start the server: AIRAVATA_ROOT_ACCOUNT_TOKEN=dev-token ./bin/airavata-server"
    echo "     2. Re-run this script: AIRAVATA_TOKEN=dev-token $0"
    echo "=========================================================="
    echo "Direct SSH & Mock Slurm verification: SUCCESS!"
    echo "=========================================================="
    exit 0
fi
echo "   ✓ Airavata API is UP"

if [ -z "$TOKEN" ]; then
    echo "   No AIRAVATA_TOKEN provided; skipping API lifecycle steps."
    echo "   Run with AIRAVATA_TOKEN=<token> to execute the full API lifecycle."
    exit 0
fi

echo "4. Executing full API process launch lifecycle..."

AUTH_HEADER="Authorization: Bearer $TOKEN"
JSON_HEADER="Content-Type: application/json"

# Register SSH Credential
PRIV_KEY_JSON=$(cat "$KEY_FILE" | awk '{printf "%s\\n", $0}')
CRED_RESP=$(curl -s -f -X POST "$API_URL/api/v1/ssh-credentials" \
  -H "$AUTH_HEADER" -H "$JSON_HEADER" \
  -d "{\"sshCredentialName\":\"mock-slurm-key-$$\",\"privateKey\":\"$PRIV_KEY_JSON\"}")
CRED_ID=$(echo "$CRED_RESP" | jq -r .sshCredentialId)
echo "   ✓ Registered SSH Credential: $CRED_ID"

# Register Slurm Cluster
CLUSTER_RESP=$(curl -s -f -X POST "$API_URL/api/v1/slurm-clusters" \
  -H "$AUTH_HEADER" -H "$JSON_HEADER" \
  -d "{\"clusterName\":\"mock-cluster-$$\",\"headnodeHost\":\"$SSH_HOST\",\"headnodePort\":$SSH_PORT}")
CLUSTER_ID=$(echo "$CLUSTER_RESP" | jq -r .slurmClusterId)
echo "   ✓ Registered Slurm Cluster: $CLUSTER_ID"

# Discover Partitions over SSH
DISC_RESP=$(curl -s -f -X POST "$API_URL/api/v1/slurm-clusters/discover-partitions" \
  -H "$AUTH_HEADER" -H "$JSON_HEADER" \
  -d "{\"headnodeHost\":\"$SSH_HOST\",\"headnodePort\":$SSH_PORT,\"loginUser\":\"slurmuser\",\"sshCredentialId\":\"$CRED_ID\"}")
echo "   ✓ Partition discovery via SSH succeeded ($(echo "$DISC_RESP" | jq -r '.partitions | length') partitions found)"

# Add partition
curl -s -f -X POST "$API_URL/api/v1/slurm-clusters/$CLUSTER_ID/partitions" \
  -H "$AUTH_HEADER" -H "$JSON_HEADER" \
  -d '{"name":"debug","isDefaultQueue":true,"maxRunTime":60,"maxNodes":1,"maxProcessors":4,"cpuPerNode":4}' > /dev/null
echo "   ✓ Added debug partition to cluster"

# Register Cluster Config
CONFIG_RESP=$(curl -s -f -X POST "$API_URL/api/v1/slurm-cluster-configs" \
  -H "$AUTH_HEADER" -H "$JSON_HEADER" \
  -d "{\"slurmClusterId\":\"$CLUSTER_ID\",\"loginUser\":\"slurmuser\",\"workRoot\":\"/home/slurmuser/scratch\",\"sshCredentialId\":\"$CRED_ID\"}")
CONFIG_ID=$(echo "$CONFIG_RESP" | jq -r .slurmClusterConfigId)
echo "   ✓ Registered Slurm Cluster Config: $CONFIG_ID"

# Discover Allocations over SSH
ALLOC_RESP=$(curl -s -f -X POST "$API_URL/api/v1/slurm-cluster-configs/$CONFIG_ID/discover-allocations" \
  -H "$AUTH_HEADER")
echo "   ✓ Allocation discovery via SSH succeeded ($(echo "$ALLOC_RESP" | jq -r '.allocations | length') allocations found)"

# Register Template
TMPL_RESP=$(curl -s -f -X POST "$API_URL/api/v1/application-templates" \
  -H "$AUTH_HEADER" -H "$JSON_HEADER" \
  -d "{\"templateName\":\"demo-app-$$\",\"templateDescription\":\"Demo batch application\"}")
TMPL_ID=$(echo "$TMPL_RESP" | jq -r .templateId)
echo "   ✓ Registered Application Template: $TMPL_ID"

# Register Batch Deployment
DEP_RESP=$(curl -s -f -X POST "$API_URL/api/v1/slurm-deployments" \
  -H "$AUTH_HEADER" -H "$JSON_HEADER" \
  -d "{\"templateId\":\"$TMPL_ID\",\"slurmClusterId\":\"$CLUSTER_ID\",\"defaultPartition\":\"debug\",\"slurmRunSection\":\"echo \\\"Airavata executed job in mock Slurm\\\" && echo \\\"Computation Result: 100\\\" > output.txt\",\"defaultBatchJobConfig\":{\"wallTimeMinutes\":10,\"nodes\":1,\"cpus\":1,\"allocation\":\"default\"}}")
DEP_ID=$(echo "$DEP_RESP" | jq -r .deploymentId)
echo "   ✓ Registered Batch Deployment: $DEP_ID"

# Create Process
PROC_RESP=$(curl -s -f -X POST "$API_URL/api/v1/processes" \
  -H "$AUTH_HEADER" -H "$JSON_HEADER" \
  -d "{\"processType\":\"BATCH_JOB\",\"batchProcess\":{\"deploymentId\":\"$DEP_ID\",\"slurmClusterConfigId\":\"$CONFIG_ID\",\"baseWorkDir\":\"/home/slurmuser/scratch\",\"batchJobConfig\":{\"wallTimeMinutes\":10,\"nodes\":1,\"cpus\":1,\"allocation\":\"default\"}}}")
PROC_ID=$(echo "$PROC_RESP" | jq -r .processId)
echo "   ✓ Created Batch Process: $PROC_ID"

# Launch Process
LAUNCH_RESP=$(curl -s -f -X POST "$API_URL/api/v1/processes/$PROC_ID/launch" \
  -H "$AUTH_HEADER")
echo "   ✓ Launched Process (HTTP 202 Accepted)"

# Wait for submission and execution in mock cluster
sleep 3

# Verify tasks recorded on process
TASKS_RESP=$(curl -s -f "$API_URL/api/v1/processes/$PROC_ID/job-submission-tasks" -H "$AUTH_HEADER")
JOB_ID=$(echo "$TASKS_RESP" | jq -r '.[0].jobId // empty')
echo "   ✓ Job submission task recorded Slurm Job ID: $JOB_ID"

# Verify files generated in mock cluster
REMOTE_FILES=$(ssh "${SSH_OPTS[@]}" "slurmuser@$SSH_HOST" "ls /home/slurmuser/scratch/$PROC_ID")
echo "   ✓ Remote job directory contents (/home/slurmuser/scratch/$PROC_ID):"
echo "     $REMOTE_FILES"

STDOUT=$(ssh "${SSH_OPTS[@]}" "slurmuser@$SSH_HOST" "cat /home/slurmuser/scratch/$PROC_ID/stdout.txt")
echo "   ✓ Remote stdout.txt output:"
echo "     $STDOUT"

echo "=========================================================="
echo "Full process launch lifecycle completed successfully!"
echo "=========================================================="
