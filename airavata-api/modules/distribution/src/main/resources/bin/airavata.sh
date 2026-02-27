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
# Airavata CLI (tarball + fat JAR)
# ==========================================================================
# Invokes Airavata with the passed args. You can pass --home and --config-dir
# as arguments; otherwise AIRAVATA_HOME and AIRAVATA_CONFIG_DIR env vars
# (or script-relative defaults) are used.
# ==========================================================================

PRG="$0"
while [ -L "$PRG" ]; do
  PRG=$(readlink "$PRG")
done
PRGDIR=$(dirname "$PRG")

# Defaults: parent of bin/ and conf under home
[ -z "$AIRAVATA_HOME" ] && AIRAVATA_HOME=$(cd "$PRGDIR/.." && pwd)
[ -z "$AIRAVATA_CONFIG_DIR" ] && AIRAVATA_CONFIG_DIR="${AIRAVATA_HOME}/conf"

# Parse optional --home and --config-dir from args (so they work as arguments)
ARGS=()
while [[ $# -gt 0 ]]; do
  case "$1" in
    --home)
      AIRAVATA_HOME="$2"
      AIRAVATA_CONFIG_DIR="${AIRAVATA_CONFIG_DIR:-$AIRAVATA_HOME/conf}"
      shift 2
      ;;
    --config-dir)
      AIRAVATA_CONFIG_DIR="$2"
      shift 2
      ;;
    *)
      ARGS+=("$1")
      shift
      ;;
  esac
done

# Find fat JAR (single executable JAR with all dependencies)
JAR_FILE=$(find "${AIRAVATA_HOME}/lib" -name "airavata-*.jar" -type f | head -1)
if [[ -z "$JAR_FILE" ]]; then
  echo "Error: Could not find Airavata JAR in ${AIRAVATA_HOME}/lib"
  exit 1
fi

JAVA_OPTS="-Dairavata.home=${AIRAVATA_HOME} -Dairavata.config.dir=${AIRAVATA_CONFIG_DIR}"
exec java $JAVA_OPTS -jar "$JAR_FILE" "${ARGS[@]}"
