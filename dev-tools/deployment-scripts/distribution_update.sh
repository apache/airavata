#!/bin/bash

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# ================================
# Update the Distribution
# ================================
log "Updating the Airavata Server..."
rm -rf ./airavata-server-0.21-SNAPSHOT
tar -xvf ./airavata-server-0.21-SNAPSHOT.tar.gz -C .
log "Airavata Server updated."

# ================================
# Update the config files
# ================================
mkdir -p ./airavata-server-0.21-SNAPSHOT/conf/keystores/
cp vault/airavata-server.properties ./airavata-server-0.21-SNAPSHOT/conf/airavata-server.properties
cp vault/airavata.sym.p12 ./airavata-server-0.21-SNAPSHOT/conf/keystores/airavata.sym.p12
cp vault/email-config.yml ./airavata-server-0.21-SNAPSHOT/conf/email-config.yml
cp vault/logback.xml ./airavata-server-0.21-SNAPSHOT/conf/logback.xml
log "Airavata Server config files updated."
