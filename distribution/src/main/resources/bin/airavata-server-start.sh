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
cd ${AIRAVATA_HOME}/bin
LOGO_FILE="logo.txt"

JAVA_OPTS=""
AIRAVATA_COMMAND=""
EXTRA_ARGS=""
IS_DAEMON_MODE=false
LOGO=true

# parse command arguments
for var in "$@"
do
    case ${var} in
        -xdebug)
        	AIRAVATA_COMMAND="${AIRAVATA_COMMAND}"
            JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=8000"
            shift
        ;;
        -security)
            JAVA_OPTS="${JAVA_OPTS} -Djava.security.manager -Djava.security.policy=${AIRAVATA_HOME}/conf/axis2.policy -Daxis2.home=${AIRAVATA_HOME}"
            shift
        ;;
	    api | gfac | orchestrator)
		    AIRAVATA_COMMAND="${AIRAVATA_COMMAND} ${var}"
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
        -h)
            echo "Usage: airavata-server-start.sh [server-name/s] [command-options]"
            echo "Server names:"
            echo "  api                 Start api server"
            echo "  gfac                Start api server"
            echo "  orchestrator        Start api server"

            echo "command options:"
	        echo "  -d                  Start server in daemon mode"
	        echo "  --<key>[=<value>]   Server setting(s) to override or introduce (overrides values in airavata-server.properties)"
            echo "  -nologo             Do not show airavata logo"
            echo "  -xdebug             Start Airavata Server under JPDA debugger"
            echo "  -security           Enable Java 2 security"
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

#add extra argument to the
AIRAVATA_COMMAND="${AIRAVATA_COMMAND} ${EXTRA_ARGS}"

#print logo file
if ${LOGO} ; then
	if [ -e ${LOGO_FILE} ]
	then
		cat ${LOGO_FILE}
	fi
fi


if ${IS_DAEMON_MODE} ; then
	echo "Starting airavata server/s in daemon mode..."
	nohup java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" \
    org.apache.airavata.server.ServerMain ${AIRAVATA_COMMAND} $* > /dev/null 2>&1 &
else
	java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" \
    org.apache.airavata.server.ServerMain ${AIRAVATA_COMMAND} $*
fi

