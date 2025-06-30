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

# Source the common environment and functions
. $(dirname $0)/setenv.sh

# Client-specific configuration
MAIN_CLASS="org.apache.airavata.tools.load.LoadClient"
JAVA_OPTS="-Dairavata.config.dir=${AIRAVATA_HOME}/conf -Dairavata.home=${AIRAVATA_HOME}"

# Parse client-specific arguments
while (($# > 0)); do
  case "$1" in
  -xdebug)
    JAVA_OPTS+=" -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=8000"
    shift
    ;;
  -h)
    echo "Usage: load-client.sh"
    echo ""
    echo "command options:"
    echo "  -config             Load configuration file in yml format"
    echo "  -apiHost            API Server host name"
    echo "  -apiPort            API Server port"
    echo "  -privateKeyPath     SSH private key path to communicate with storage resources (Defaults to user private key in ~/.ssh/id_rsa)"
    echo "  -publicKeyPath      SSH public key path to communicate with storage resources (Defaults to user public key in ~/.ssh/id_rsa.pub)"
    echo "  -passPhrase         SSH private key pass phrase (if any)"
    echo "  -xdebug             Start under JPDA debugger"
    echo "  -h                  Display this help and exit"
    exit 0
    ;;
  *)
    # Pass all other arguments to the Java application
    break
    ;;
  esac
done

# Run the load client
java ${JAVA_OPTS} -classpath "${CLASSPATH}" ${MAIN_CLASS} "$@"
