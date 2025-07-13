#!/bin/bash

log() {
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# ================================
# Update the Distribution
# ================================
log "Updating the API Server..."
rm -rf ./apache-airavata-api-server-0.21-SNAPSHOT
tar -xvf ./apache-airavata-api-server-0.21-SNAPSHOT.tar.gz -C .
log "API Server updated."

log "Updating the Agent Service..."
rm -rf ./apache-airavata-agent-service-0.21-SNAPSHOT
tar -xvf ./apache-airavata-agent-service-0.21-SNAPSHOT.tar.gz -C .
log "Agent Service updated."

log "Updating the Research Service..."
rm -rf ./apache-airavata-research-service-0.21-SNAPSHOT
tar -xvf ./apache-airavata-research-service-0.21-SNAPSHOT.tar.gz -C .
log "Research Service updated."

log "Updating the File Service..."
rm -rf ./apache-airavata-file-server-0.21-SNAPSHOT
tar -xvf ./apache-airavata-file-server-0.21-SNAPSHOT.tar.gz -C .
log "File Service updated."

log "Updating the REST proxy..."
rm -rf ./apache-airavata-restproxy-0.21-SNAPSHOT
tar -xvf ./apache-airavata-restproxy-0.21-SNAPSHOT.tar.gz -C .
log "REST proxy updated."

# ================================
# Update the config files
# ================================
cp vault/airavata-server.properties ./apache-airavata-api-server-0.21-SNAPSHOT/conf/airavata-server.properties
cp vault/airavata.p12 ./apache-airavata-api-server-0.21-SNAPSHOT/conf/keystores/airavata.p12
cp vault/email-config.yaml ./apache-airavata-api-server-0.21-SNAPSHOT/conf/email-config.yaml
cp vault/log4j2.xml ./apache-airavata-api-server-0.21-SNAPSHOT/conf/log4j2.xml
log "API Server config files updated."

cp vault/application-agent-service.yml ./apache-airavata-agent-service-0.21-SNAPSHOT/conf/application.yml
cp vault/airavata.p12 ./apache-airavata-agent-service-0.21-SNAPSHOT/conf/airavata.p12
cp vault/log4j2.xml ./apache-airavata-agent-service-0.21-SNAPSHOT/conf/log4j2.xml
log "Agent Service config files updated."

cp vault/application-research-service.yml ./apache-airavata-research-service-0.21-SNAPSHOT/conf/application.yml
cp vault/airavata.p12 ./apache-airavata-research-service-0.21-SNAPSHOT/conf/airavata.p12
cp vault/log4j2.xml ./apache-airavata-research-service-0.21-SNAPSHOT/conf/log4j2.xml
log "Research Service config files updated."

cp vault/application-file-server.properties ./apache-airavata-file-server-0.21-SNAPSHOT/conf/application.properties
cp vault/log4j2.xml ./apache-airavata-file-server-0.21-SNAPSHOT/conf/log4j2.xml
log "File Service config files updated."

cp vault/application-restproxy.properties ./apache-airavata-restproxy-0.21-SNAPSHOT/conf/application.properties
cp vault/log4j2.xml ./apache-airavata-restproxy-0.21-SNAPSHOT/conf/log4j2.xml
log "REST proxy config files updated."