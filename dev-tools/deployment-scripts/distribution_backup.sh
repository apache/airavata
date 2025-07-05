#!/bin/bash

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# ================================
# Create the Backup Directory
# ================================
BACKUP_DIR="./backup/$(date +%Y-%m-%d)"
mkdir -p $BACKUP_DIR

# ================================
# Backup the Distribution
# ================================
log "Backing up the Distribution..."
cp -r ./apache-airavata-api-server-0.21-SNAPSHOT $BACKUP_DIR/
cp -r ./apache-airavata-agent-service-0.21-SNAPSHOT $BACKUP_DIR/
cp -r ./apache-airavata-research-service-0.21-SNAPSHOT $BACKUP_DIR/
cp -r ./apache-airavata-file-server-0.21-SNAPSHOT $BACKUP_DIR/
log "Distribution backed up."
