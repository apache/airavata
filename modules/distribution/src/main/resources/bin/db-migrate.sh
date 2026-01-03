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

# Resolve symlinks to get the real script location
PRG="$0"
while [ -L "$PRG" ]; do
  PRG=$(readlink "$PRG")
done
PRGDIR=$(dirname "$PRG")

# Set AIRAVATA_HOME if not already set
[ -z "$AIRAVATA_HOME" ] && AIRAVATA_HOME=$(cd "$PRGDIR/.." && pwd)

# Build CLASSPATH from all JAR files
CLASSPATH=$(printf "%s:" "$AIRAVATA_HOME"/lib/*.jar)
CLASSPATH=${CLASSPATH%:} # Remove trailing colon

export AIRAVATA_HOME CLASSPATH

# Default Java options
JAVA_OPTS="-Dairavata.config.dir=${AIRAVATA_HOME}/conf -Dairavata.home=${AIRAVATA_HOME} -Dlog4j.configurationFile=file:${AIRAVATA_HOME}/conf/log4j2.xml"

# Main class - use AiravataServer
MAIN_CLASS="org.apache.airavata.AiravataServer"

# Parse arguments
URL=""
USER=""
PWD=""
VERSION=""

while [[ $# -gt 0 ]]; do
  case $1 in
    -url|--url)
      URL="$2"
      shift 2
      ;;
    -user|--user)
      USER="$2"
      shift 2
      ;;
    -pwd|--pwd)
      PWD="$2"
      shift 2
      ;;
    -v|--version)
      VERSION="$2"
      shift 2
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: $0 -url <JDBC_URL> -user <USERNAME> -pwd <PASSWORD> -v <VERSION>"
      exit 1
      ;;
  esac
done

if [ -z "$URL" ] || [ -z "$USER" ] || [ -z "$PWD" ] || [ -z "$VERSION" ]; then
  echo "Error: Missing required parameters"
  echo "Usage: $0 -url <JDBC_URL> -user <USERNAME> -pwd <PASSWORD> -v <VERSION>"
  exit 1
fi

# Run migration
cd "${AIRAVATA_HOME}/bin"
java $JAVA_OPTS -classpath "$CLASSPATH" "$MAIN_CLASS" \
  --spring.main.web-application-type=none \
  --migrate.enabled=true \
  --url="$URL" \
  --user="$USER" \
  --pwd="$PWD" \
  --version="$VERSION"

