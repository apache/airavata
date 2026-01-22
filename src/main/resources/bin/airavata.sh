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

# ==========================================================================
# Airavata CLI
# ==========================================================================
# This program invokes Airavata CLI with the passed args
# and ensures that AIRAVATA_HOME is correctly set.
# ==========================================================================


# Get the script location
PRG="$0"
while [ -L "$PRG" ]; do
  PRG=$(readlink "$PRG")
done
PRGDIR=$(dirname "$PRG")

# Set AIRAVATA_HOME (defaults to parent directory of this script)
[ -z "$AIRAVATA_HOME" ] && AIRAVATA_HOME=$(cd "$PRGDIR/.." && pwd)

# Set AIRAVATA_CONFIG_DIR (defaults to AIRAVATA_HOME/conf)
[ -z "$AIRAVATA_CONFIG_DIR" ] && AIRAVATA_CONFIG_DIR="${AIRAVATA_HOME}/conf"

# Find JAR file
JAR_FILE=$(find "${AIRAVATA_HOME}/lib" -name "airavata-*.jar" | head -1)
if [[ -z "$JAR_FILE" ]]; then
  echo "Error: Could not find Airavata JAR file in ${AIRAVATA_HOME}/lib"
  exit 1
fi

# Define JAVA_OPTS
JAVA_OPTS="-Dairavata.home=${AIRAVATA_HOME} -Dairavata.config.dir=${AIRAVATA_CONFIG_DIR}"

# Start Airavata
java $JAVA_OPTS -jar "$JAR_FILE" "$@"
