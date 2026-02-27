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

# Simplified Airavata Service Shutdown Script
# 
# This script stops the unified Airavata API server.

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# ================================
# Stop the Unified Airavata Server
# ================================
log "Stopping Airavata API Server (unified service)..."

DIST_NAME="airavata-0.21-SNAPSHOT"
if [ -f "./${DIST_NAME}/bin/airavata.sh" ]; then
    ./${DIST_NAME}/bin/airavata.sh -d stop
    log "Airavata API Server stopped."
else
    log "ERROR: Airavata distribution not found."
    exit 1
fi

log "All Airavata services have been stopped."
