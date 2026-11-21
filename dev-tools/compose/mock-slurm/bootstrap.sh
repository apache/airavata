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

# Registers the mock-slurm container with a running airavata-server through its
# public REST API. Usage: AIRAVATA_TOKEN=<token> ./bootstrap.sh [--launch]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEY_FILE="$SCRIPT_DIR/keys/id_ed25519"

API_URL="${AIRAVATA_API_URL:-http://localhost:9095}"
TOKEN="${AIRAVATA_TOKEN:-}"
# Where this script reaches the container's sshd.
SSH_HOST="${MOCK_SLURM_SSH_HOST:-127.0.0.1}"
SSH_PORT="${MOCK_SLURM_SSH_PORT:-12222}"
# Where airavata-server reaches it; use mock-slurm:22 when the server runs inside compose.
CLUSTER_HOST="${MOCK_SLURM_CLUSTER_HOST:-$SSH_HOST}"
CLUSTER_PORT="${MOCK_SLURM_CLUSTER_PORT:-$SSH_PORT}"
WORK_ROOT=/home/slurmuser/scratch

LAUNCH=0
case "${1:-}" in
  "") ;;
  --launch) LAUNCH=1 ;;
  *) echo "usage: AIRAVATA_TOKEN=<token> $0 [--launch]" >&2; exit 2 ;;
esac

for cmd in curl jq ssh ssh-keygen; do
  command -v "$cmd" >/dev/null || { echo "error: $cmd is required" >&2; exit 1; }
done

if [ ! -f "$KEY_FILE" ]; then
  ssh-keygen -q -t ed25519 -N "" -C mock-slurm-dev -f "$KEY_FILE"
  echo "Generated $KEY_FILE"
fi
chmod 600 "$KEY_FILE"

SSH_OPTS=(-i "$KEY_FILE" -p "$SSH_PORT" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null
  -o LogLevel=ERROR -o ConnectTimeout=5 -o BatchMode=yes)
remote() { ssh "${SSH_OPTS[@]}" "slurmuser@$SSH_HOST" "$@"; }

echo "Checking SSH to slurmuser@$SSH_HOST:$SSH_PORT"
remote "sinfo -h -a -o '%R|%c|%m|%G'" >/dev/null

if [ -z "$TOKEN" ]; then
  echo "SSH works. Set AIRAVATA_TOKEN to register the cluster with airavata-server." >&2
  exit 1
fi
curl -sSf -m 5 "$API_URL/health" >/dev/null || { echo "error: no airavata-server at $API_URL" >&2; exit 1; }

api() {
  local method=$1 path=$2 body=${3:-}
  local args=(-sS --fail-with-body -X "$method" "$API_URL$path" -H "Authorization: Bearer $TOKEN")
  if [ -n "$body" ]; then args+=(-H "Content-Type: application/json" -d "$body"); fi
  curl "${args[@]}"
}

SUFFIX="$(date +%Y%m%d%H%M%S)"

CRED_ID=$(api POST /api/v1/ssh-credentials "$(jq -n \
  --arg name "mock-slurm-$SUFFIX" --rawfile pub "$KEY_FILE.pub" --rawfile priv "$KEY_FILE" \
  '{sshCredentialName: $name, publicKey: $pub, privateKey: $priv}')" | jq -r .sshCredentialId)
echo "SSH credential:   $CRED_ID"

CLUSTER_ID=$(api POST /api/v1/slurm-clusters "$(jq -n \
  --arg name "mock-slurm-$SUFFIX" --arg host "$CLUSTER_HOST" --argjson port "$CLUSTER_PORT" \
  '{clusterName: $name, headnodeHost: $host, headnodePort: $port,
    partitions: [{name: "debug", isDefaultQueue: true, maxRunTime: 60, maxNodes: 1, maxProcessors: 4, cpuPerNode: 4}]}')" \
  | jq -r .slurmClusterId)
echo "Slurm cluster:    $CLUSTER_ID"

CONFIG_ID=$(api POST /api/v1/slurm-cluster-configs "$(jq -n \
  --arg cluster "$CLUSTER_ID" --arg cred "$CRED_ID" --arg root "$WORK_ROOT" \
  '{slurmClusterId: $cluster, loginUser: "slurmuser", workRoot: $root, sshCredentialId: $cred}')" \
  | jq -r .slurmClusterConfigId)
echo "Cluster config:   $CONFIG_ID"

ALLOCS=$(api POST "/api/v1/slurm-cluster-configs/$CONFIG_ID/discover-allocations" | jq -c '[.allocations[]?]')
echo "Allocations:      $ALLOCS (discovered by the server over SSH)"

TEMPLATE_ID=$(api POST /api/v1/application-templates "$(jq -n --arg name "echo-$SUFFIX" \
  '{templateName: $name, templateDescription: "Echo app on the mock Slurm cluster"}')" | jq -r .templateId)
echo "Template:         $TEMPLATE_ID"

JOB_CONFIG='{"wallTimeMinutes": 10, "nodes": 1, "cpus": 1, "allocation": "default", "partition": "debug"}'
DEPLOYMENT_ID=$(api POST /api/v1/slurm-deployments "$(jq -n \
  --arg tmpl "$TEMPLATE_ID" --arg cluster "$CLUSTER_ID" --argjson cfg "$JOB_CONFIG" \
  '{templateId: $tmpl, slurmClusterId: $cluster, defaultBatchJobConfig: $cfg,
    slurmRunSection: "echo \"Hello from mock Slurm\" > output.txt"}')" | jq -r .deploymentId)
echo "Deployment:       $DEPLOYMENT_ID"

[ "$LAUNCH" = 1 ] || exit 0

PROCESS_ID=$(api POST /api/v1/processes "$(jq -n \
  --arg dep "$DEPLOYMENT_ID" --arg config "$CONFIG_ID" --arg root "$WORK_ROOT" --argjson cfg "$JOB_CONFIG" \
  '{processType: "BATCH_JOB",
    batchProcess: {deploymentId: $dep, slurmClusterConfigId: $config, baseWorkDir: $root, batchJobConfig: $cfg}}')" \
  | jq -r .processId)
api POST "/api/v1/processes/$PROCESS_ID/launch" >/dev/null
echo "Launched process: $PROCESS_ID"

for _ in $(seq 1 30); do
  JOB_ID=$(api GET "/api/v1/processes/$PROCESS_ID/job-submission-tasks" | jq -r '[.[].jobId // empty][0] // empty')
  if [ -n "$JOB_ID" ] && remote "test -s $WORK_ROOT/$PROCESS_ID/output.txt"; then
    echo "Slurm job $JOB_ID wrote: $(remote "cat $WORK_ROOT/$PROCESS_ID/output.txt")"
    exit 0
  fi
  sleep 1
done
echo "error: job did not produce $WORK_ROOT/$PROCESS_ID/output.txt within 30s" >&2
exit 1
