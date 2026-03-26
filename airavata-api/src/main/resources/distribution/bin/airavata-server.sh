#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/setenv.sh"

run_service "airavata" "org.apache.airavata.api.server.AiravataServer" "$@"
