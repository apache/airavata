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

. `dirname $0`/setenv.sh
# Capture user's working dir before changing directory
CWD="$PWD"
cd ${AIRAVATA_HOME}/bin
LOGO_FILE="logo.txt"

JAVA_OPTS="-Dairavata.config.dir=${AIRAVATA_HOME}/conf -Dairavata.home=${AIRAVATA_HOME} -Dlog4j.configurationFile=file:${AIRAVATA_HOME}/conf/log4j2.xml"
AIRAVATA_COMMAND=""
EXTRA_ARGS=""
SERVERS=""
IS_SUBSET=false
SUBSET=""
DEFAULT_LOG_FILE="${AIRAVATA_HOME}/logs/airavata.out"
LOG_FILE=$DEFAULT_LOG_FILE

# parse command arguments
for var in "$@"
do
    case ${var} in
        -xdebug)
        	AIRAVATA_COMMAND="${AIRAVATA_COMMAND}"
            JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=*:8000"
            shift
	    ;;
        -log)
            shift
            LOG_FILE="$1"
            shift
            # If relative path, expand to absolute path using the user's $CWD
            if [ -z "`echo "$LOG_FILE" | egrep "^/"`" ]; then
                LOG_FILE="${CWD}/${LOG_FILE}"
            fi
        ;;
        -h)
            echo "Usage: pre-wm.sh"

            echo "command options:"
            echo "  -xdebug             Start Pre Workflow Manager under JPDA debugger"
            echo "  -h                  Display this help and exit"
            shift
            exit 0
        ;;
	    *)
	        EXTRA_ARGS="${EXTRA_ARGS} ${var}"
            shift
        ;;
    esac
done

java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" \
    org.apache.airavata.helix.impl.workflow.PreWorkflowManager ${AIRAVATA_COMMAND} $*

