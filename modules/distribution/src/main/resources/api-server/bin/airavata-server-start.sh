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

JAVA_OPTS=""
AIRAVATA_COMMAND=""
EXTRA_ARGS=""
SERVERS=""
IS_DAEMON_MODE=false
LOGO=true
IS_SUBSET=false
SUBSET=""
DEFAULT_LOG_FILE="${AIRAVATA_HOME}/logs/output.log"
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
        -security)
            JAVA_OPTS="${JAVA_OPTS} -Djava.security.manager -Djava.security.policy=${AIRAVATA_HOME}/conf/axis2.policy -Daxis2.home=${AIRAVATA_HOME}"
            shift
        ;;
	    apiserver | gfac | orchestrator | credentialstore | regserver)
	        if [ -z ${SERVERS} ] ; then
	            SERVERS="${var}"
	        else
	            SERVERS="${SERVERS},${var}"
	        fi
            shift
        ;;
        all | api-orch | execution )
            IS_SUBSET=true
            SUBSET="${var}"
            shift
            ;;
        -d)
	        IS_DAEMON_MODE=true
	        shift
	        ;;
	    -nologo)
	        LOGO=false
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
            echo "Usage: airavata-server-start.sh [server-name/s] [command-options]"
            echo "Server names:"
            echo "  apiserver           Start apiserver"
            echo "  gfac                Start gfac server"
            echo "  orchestrator        Start orchestrator server"
            echo "  credentialstore     Start credentialstore server"
            echo "  regserver           Start registry server"
            echo "  all                 Start all servers in one JVM"

            echo "command options:"
	        echo "  -d                  Start server in daemon mode"
            echo "  -xdebug             Start Airavata Server under JPDA debugger"
            echo "  -nologo             Do not show airavata logo"
            echo "  -security           Enable Java 2 security"
	        echo "  --<key>[=<value>]   Server setting(s) to override or introduce (overrides values in airavata-server.properties)"
	        echo "  -log <LOG_FILE>     Where to redirect stdout/stderr (defaults to $DEFAULT_LOG_FILE)"
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

#Construct Airavata command arguments in proper order.
if ${IS_SUBSET} ; then
    AIRAVATA_COMMAND="--servers=${SUBSET} ${AIRAVATA_COMMAND} ${EXTRA_ARGS}"
else
    if [ -z ${SERVERS} ] ; then
        echo "You should provide at least one server component to start the airavata server. Please use -h option to get more details."
        exit -1
    else
        AIRAVATA_COMMAND="--servers=${SERVERS} ${AIRAVATA_COMMAND} ${EXTRA_ARGS}"
    fi
fi

#print logo file
if ${LOGO} ; then
	if [ -e ${LOGO_FILE} ]
	then
		cat ${LOGO_FILE}
	fi
fi


if ${IS_DAEMON_MODE} ; then
	echo "Starting airavata server/s in daemon mode..."
	echo "Redirecting output to $LOG_FILE"
	nohup java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" \
    org.apache.airavata.server.ServerMain ${AIRAVATA_COMMAND} $* > $LOG_FILE 2>&1 &
else
	java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" \
    org.apache.airavata.server.ServerMain ${AIRAVATA_COMMAND} $*
fi

