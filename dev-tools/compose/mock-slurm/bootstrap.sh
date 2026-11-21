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
# public REST API. Safe to re-run: records are found by name and reused.
# Usage: AIRAVATA_TOKEN=<token> ./bootstrap.sh [--launch]

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

NAME=mock-slurm
LOGIN_USER=slurmuser
WORK_ROOT=/home/slurmuser/scratch
INPUT_PATH=/home/slurmuser/data/message.txt

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
remote() { ssh "${SSH_OPTS[@]}" "$LOGIN_USER@$SSH_HOST" "$@"; }

echo "Checking SSH to $LOGIN_USER@$SSH_HOST:$SSH_PORT"
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

# find <list path> <jq filter selecting one record> <id field>
find_id() { api GET "$1" | jq -r "[.[] | select($2)][0].$3 // empty"; }

# The credential name carries the key's fingerprint, so a regenerated key gets a new one.
FINGERPRINT=$(ssh-keygen -lf "$KEY_FILE.pub" | awk '{print $2}' | tr -dc 'A-Za-z0-9' | cut -c7-18)
CRED_NAME="$NAME-$FINGERPRINT"
CRED_ID=$(find_id /api/v1/ssh-credentials ".sshCredentialName == \"$CRED_NAME\"" sshCredentialId)
if [ -z "$CRED_ID" ]; then
  CRED_ID=$(api POST /api/v1/ssh-credentials "$(jq -n \
    --arg name "$CRED_NAME" --rawfile pub "$KEY_FILE.pub" --rawfile priv "$KEY_FILE" \
    '{sshCredentialName: $name, publicKey: $pub, privateKey: $priv}')" | jq -r .sshCredentialId)
fi
echo "SSH credential:   $CRED_ID"

CLUSTER_ID=$(find_id /api/v1/slurm-clusters ".clusterName == \"$NAME\"" slurmClusterId)
if [ -z "$CLUSTER_ID" ]; then
  PARTITIONS=$(api POST /api/v1/slurm-clusters/discover-partitions "$(jq -n \
    --arg host "$CLUSTER_HOST" --argjson port "$CLUSTER_PORT" --arg user "$LOGIN_USER" --arg cred "$CRED_ID" \
    '{headnodeHost: $host, headnodePort: $port, loginUser: $user, sshCredentialId: $cred}')" | jq -c .partitions)
  CLUSTER_ID=$(api POST /api/v1/slurm-clusters "$(jq -n \
    --arg name "$NAME" --arg host "$CLUSTER_HOST" --argjson port "$CLUSTER_PORT" --argjson parts "$PARTITIONS" \
    '{clusterName: $name, headnodeHost: $host, headnodePort: $port, partitions: $parts}')" | jq -r .slurmClusterId)
fi
echo "Slurm cluster:    $CLUSTER_ID ($(api GET "/api/v1/slurm-clusters/$CLUSTER_ID" | jq -r '[.partitions[].name] | join(", ")'))"

# Config and storage are updated in place when they point at an older credential.
CONFIG_BODY=$(jq -n --arg name "$NAME" --arg cluster "$CLUSTER_ID" --arg cred "$CRED_ID" \
  --arg user "$LOGIN_USER" --arg root "$WORK_ROOT" \
  '{name: $name, slurmClusterId: $cluster, loginUser: $user, workRoot: $root, sshCredentialId: $cred}')
CONFIG=$(api GET /api/v1/slurm-cluster-configs/me | jq -c "[.[] | select(.name == \"$NAME\")][0] // empty")
if [ -z "$CONFIG" ]; then
  CONFIG_ID=$(api POST /api/v1/slurm-cluster-configs "$CONFIG_BODY" | jq -r .slurmClusterConfigId)
else
  CONFIG_ID=$(jq -r .slurmClusterConfigId <<<"$CONFIG")
  if [ "$(jq -r .sshCredentialId <<<"$CONFIG")" != "$CRED_ID" ]; then
    api PUT "/api/v1/slurm-cluster-configs/$CONFIG_ID" "$CONFIG_BODY" >/dev/null
  fi
fi
echo "Cluster config:   $CONFIG_ID"

ALLOCS=$(api POST "/api/v1/slurm-cluster-configs/$CONFIG_ID/discover-allocations" | jq -r '[.allocations[].account] | join(", ")')
echo "Allocations:      $ALLOCS (discovered by the server over SSH)"

STORAGE_BODY=$(jq -n --arg name "$NAME" --arg host "$CLUSTER_HOST" --argjson port "$CLUSTER_PORT" \
  --arg user "$LOGIN_USER" --arg cred "$CRED_ID" \
  '{dataName: $name, hostName: $host, port: $port, loginUser: $user, sshCredentialId: $cred}')
STORAGE=$(api GET /api/v1/scp-data-storages/me | jq -c "[.[] | select(.dataName == \"$NAME\")][0] // empty")
if [ -z "$STORAGE" ]; then
  STORAGE_ID=$(api POST /api/v1/scp-data-storages "$STORAGE_BODY" | jq -r .dataId)
else
  STORAGE_ID=$(jq -r .dataId <<<"$STORAGE")
  if [ "$(jq -r .sshCredentialId <<<"$STORAGE")" != "$CRED_ID" ]; then
    api PUT "/api/v1/scp-data-storages/$STORAGE_ID" "$STORAGE_BODY" >/dev/null
  fi
fi
echo "SCP storage:      $STORAGE_ID"

INPUT_NAME="$NAME-message"
INPUT_ID=$(find_id /api/v1/data-products/me ".dataName == \"$INPUT_NAME\"" dataId)
if [ -z "$INPUT_ID" ]; then
  INPUT_ID=$(api POST /api/v1/data-products "$(jq -n \
    --arg name "$INPUT_NAME" --arg path "$INPUT_PATH" --arg storage "$STORAGE_ID" \
    '{dataName: $name, dataDescription: "Sample input for the echo template", isFile: true, path: $path, dataStorageId: $storage}')" \
    | jq -r .dataId)
fi
printf 'hello from airavata\n' | curl -sS --fail-with-body -X PUT "$API_URL/api/v1/data-products/$INPUT_ID/content" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/octet-stream" --data-binary @- >/dev/null
echo "Input product:    $INPUT_ID ($INPUT_PATH, uploaded through the API)"

TEMPLATE_NAME="$NAME-echo"
TEMPLATE=$(api GET /api/v1/application-templates | jq -c "[.[] | select(.templateName == \"$TEMPLATE_NAME\")][0] // empty")
if [ -z "$TEMPLATE" ]; then
  TEMPLATE=$(api POST /api/v1/application-templates "$(jq -n --arg name "$TEMPLATE_NAME" \
    '{templateName: $name, templateDescription: "Upper-cases a staged input file into an output file",
      inputs: [{inputName: "message.txt", inputType: "FILE", required: true}],
      outputs: [{outputName: "greeting.txt", outputType: "FILE"}]}')")
fi
TEMPLATE_ID=$(jq -r .templateId <<<"$TEMPLATE")
TEMPLATE_INPUT_ID=$(jq -r '.inputs[0].inputId' <<<"$TEMPLATE")
TEMPLATE_OUTPUT_ID=$(jq -r '.outputs[0].outputId' <<<"$TEMPLATE")
echo "Template:         $TEMPLATE_ID"

JOB_CONFIG='{"wallTimeMinutes": 10, "nodes": 1, "cpus": 1, "allocation": "default", "partition": "debug"}'
DEPLOYMENT_ID=$(find_id /api/v1/slurm-deployments \
  ".templateId == \"$TEMPLATE_ID\" and .slurmClusterId == \"$CLUSTER_ID\"" deploymentId)
if [ -z "$DEPLOYMENT_ID" ]; then
  DEPLOYMENT_ID=$(api POST /api/v1/slurm-deployments "$(jq -n \
    --arg tmpl "$TEMPLATE_ID" --arg cluster "$CLUSTER_ID" --argjson cfg "$JOB_CONFIG" \
    '{templateId: $tmpl, slurmClusterId: $cluster, defaultBatchJobConfig: $cfg,
      slurmRunSection: "tr a-z A-Z < message.txt > greeting.txt"}')" | jq -r .deploymentId)
fi
echo "Deployment:       $DEPLOYMENT_ID"

[ "$LAUNCH" = 1 ] || exit 0

PROCESS_ID=$(api POST /api/v1/processes "$(jq -n \
  --arg dep "$DEPLOYMENT_ID" --arg config "$CONFIG_ID" --arg root "$WORK_ROOT" --argjson cfg "$JOB_CONFIG" \
  --arg in "$TEMPLATE_INPUT_ID" --arg inval "$INPUT_ID" --arg out "$TEMPLATE_OUTPUT_ID" \
  '{processType: "BATCH_JOB",
    batchProcess: {deploymentId: $dep, slurmClusterConfigId: $config, baseWorkDir: $root, batchJobConfig: $cfg,
      inputMappings: [{templateInputId: $in, value: $inval}],
      outputMappings: [{templateOutputId: $out, value: "TRACK_IN_PLACE"}]}}')" \
  | jq -r .processId)
api POST "/api/v1/processes/$PROCESS_ID/launch" >/dev/null
echo "Launched process: $PROCESS_ID"

OUTPUT="$WORK_ROOT/$PROCESS_ID/greeting.txt"
for _ in $(seq 1 30); do
  JOB_ID=$(api GET "/api/v1/processes/$PROCESS_ID/job-submission-tasks" | jq -r '[.[].jobId // empty][0] // empty')
  if [ -n "$JOB_ID" ] && remote "test -s $OUTPUT"; then
    echo "Slurm job $JOB_ID staged message.txt in and wrote $OUTPUT: $(remote "cat $OUTPUT")"
    exit 0
  fi
  sleep 1
done
echo "error: job did not produce $OUTPUT within 30s" >&2
exit 1
