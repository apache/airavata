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

# Simplified Distribution Backup Script
# 
# Backs up the unified Airavata server distribution and configuration.

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# ================================
# Create the Backup Directory
# ================================
BACKUP_DIR="./backup/$(date +%Y-%m-%d_%H%M%S)"
mkdir -p $BACKUP_DIR

log "Creating backup in: $BACKUP_DIR"

# ================================
# Backup the Unified Distribution
# ================================
DIST_NAME="airavata-0.21-SNAPSHOT"

if [ -d "./${DIST_NAME}" ]; then
    log "Backing up distribution..."
    cp -r ./${DIST_NAME} $BACKUP_DIR/
    log "Distribution backed up."
else
    log "WARNING: Distribution directory not found: ${DIST_NAME}"
fi

# ================================
# Backup Configuration Files
# ================================
if [ -d "./conf" ]; then
    log "Backing up configuration files..."
    cp -r ./conf $BACKUP_DIR/
    log "Configuration files backed up."
else
    log "WARNING: conf directory not found. No configuration files to backup."
fi

# ================================
# Backup Logs (optional)
# ================================
if [ -d "./${DIST_NAME}/logs" ]; then
    log "Backing up logs..."
    mkdir -p $BACKUP_DIR/logs
    cp -r ./${DIST_NAME}/logs/* $BACKUP_DIR/logs/ 2>/dev/null || true
    log "Logs backed up."
fi

log "Backup completed: $BACKUP_DIR"
