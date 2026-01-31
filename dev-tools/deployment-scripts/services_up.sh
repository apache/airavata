#!/bin/bash
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

# Simplified Airavata Service Startup Script
# 
# This script starts the unified Airavata API server.
# All services (API, Orchestrator, Registry, Workflow Managers) run in a single process.

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# ================================
# Start the Unified Airavata Server
# ================================
log "Starting Airavata API Server (unified service)..."

# The unified server includes:
# - Thrift Server
# - Orchestrator
# - Registry
# - Profile Service
# - Sharing Registry
# - Credential Store
# - All Workflow Managers
# - All Background Services

DIST_NAME="airavata-0.21-SNAPSHOT"
if [ -f "./${DIST_NAME}/bin/airavata.sh" ]; then
    ./${DIST_NAME}/bin/airavata.sh -d start
    log "Airavata API Server started."
else
    log "ERROR: Airavata distribution not found. Please deploy first using Ansible."
    exit 1
fi

log "All Airavata services are running in the unified server."
