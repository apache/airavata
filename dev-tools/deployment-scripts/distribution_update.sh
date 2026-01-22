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

# Simplified Distribution Update Script
# 
# Updates the unified Airavata server distribution.

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# ================================
# Update the Unified Distribution
# ================================
log "Updating the Airavata unified server distribution..."

DIST_NAME="apache-airavata-server-0.21-SNAPSHOT"
DIST_ARCHIVE="${DIST_NAME}-bin.tar.gz"

if [ ! -f "./${DIST_ARCHIVE}" ]; then
    log "ERROR: Distribution archive not found: ${DIST_ARCHIVE}"
    exit 1
fi

# Stop the server if running
if [ -f "./${DIST_NAME}/bin/airavata-server-stop.sh" ]; then
    log "Stopping current server..."
    ./${DIST_NAME}/bin/airavata-server-stop.sh -f
    sleep 5
fi

# Remove old distribution
if [ -d "./${DIST_NAME}" ]; then
    log "Removing old distribution..."
    rm -rf ./${DIST_NAME}
fi

# Extract new distribution
log "Extracting new distribution..."
tar -xvf ./${DIST_ARCHIVE}
log "Distribution extracted."

# ================================
# Update Configuration Files
# ================================
log "Updating configuration files..."

# Create conf directory structure
mkdir -p ./${DIST_NAME}/conf/keystores/

# Copy configuration files from conf (if exists)
if [ -d "./conf" ]; then
    if [ -f "./conf/airavata.properties" ]; then
        cp ./conf/airavata.properties ./${DIST_NAME}/conf/airavata.properties
        log "Configuration file updated."
    fi
    
    if [ -f "./conf/airavata.sym.p12" ]; then
        cp ./conf/airavata.sym.p12 ./${DIST_NAME}/conf/keystores/airavata.sym.p12
        log "Keystore file updated."
    fi
    
    if [ -f "./conf/logback.xml" ]; then
        cp ./conf/logback.xml ./${DIST_NAME}/conf/logback.xml
        log "Logback configuration updated."
    fi
else
    log "WARNING: conf directory not found. Configuration files not updated."
    log "Please ensure configuration is set up before starting the server."
fi

log "Distribution update completed."
log "You can now start the server using: ./${DIST_NAME}/bin/airavata-server-start.sh"
