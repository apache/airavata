#!/usr/bin/env bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

. $(dirname $0)/setenv.sh

SERVICE_NAME="airavata-server"
MAIN_CLASS="org.apache.airavata.server.ServerMain"
JAVA_OPTS="-Dairavata.config.dir=${AIRAVATA_HOME}/conf -Dairavata.home=${AIRAVATA_HOME} -Dlog4j.configurationFile=file:${AIRAVATA_HOME}/conf/log4j2.xml"

SERVERS=""
ARGS=()
while [[ $# -gt 0 ]]; do
  case $1 in
  -security)
    JAVA_OPTS+=" -Djava.security.manager -Djava.security.policy=${AIRAVATA_HOME}/conf/axis2.policy -Daxis2.home=${AIRAVATA_HOME}"
    shift
    ;;
  -enableLegacyTLS)
    JAVA_OPTS+=" -Djava.security.policy=${AIRAVATA_HOME}/bin/enableLegacyTLS.security -Djava.security.disableSystemPropertiesFile=true"
    shift
    ;;
  apiserver | gfac | orchestrator | credentialstore | regserver)
    if [ -z "$SERVERS" ]; then SERVERS="$1"; else SERVERS="$SERVERS,$1"; fi
    shift
    ;;
  all | api-orch | execution)
    SERVERS="$1"
    shift
    ;;
  *)
    ARGS+=("$1")
    shift
    ;;
  esac
done
CONSTRUCTED_ARGS=()
if [[ " ${ARGS[*]} " =~ " start " ]]; then
  if [ -n "$SERVERS" ]; then
    CONSTRUCTED_ARGS+=("--servers=${SERVERS}")
  else
    echo "You should provide at least one server component to start the airavata server. Please use -h option to get more details."
    exit 1
  fi
fi

run_service "$SERVICE_NAME" "$MAIN_CLASS" "$JAVA_OPTS" "${ARGS[@]}" "${CONSTRUCTED_ARGS[@]}"
