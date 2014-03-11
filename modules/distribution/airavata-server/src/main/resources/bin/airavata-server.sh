#!/bin/sh

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
cd $AIRAVATA_HOME/bin
LOGO_FILE="logo.txt"

JAVA_OPTS=""
AIRAVATA_COMMAND=""
IS_DAEMON_MODE=false
LOGO=true
for var in "$@"
do
    case $var in
        -xdebug)
            JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=8000"
            shift
        ;;
        -security)
            JAVA_OPTS="$JAVA_OPTS -Djava.security.manager -Djava.security.policy=$AIRAVATA_HOME/conf/axis2.policy -Daxis2.home=$AIRAVATA_HOME"
            shift
        ;;
	start)
	    IS_DAEMON_MODE=true
            shift
        ;;
	stop)
	    LOGO=false
	    AIRAVATA_COMMAND="$AIRAVATA_COMMAND $var"
            shift
        ;;
	-nologo)
	    LOGO=false
            shift
        ;;
        -h)
            echo "Usage: airavata-server.sh [command-options]"
            echo "command options:"
	    echo "  start                            Start server in daemon mode"
	    echo "  stop [--serverIndex n] [--force] Stop all airavata servers. Specify serverIndex stop a particular instance"
	    echo "  --<key>[=<value>]         	     Server setting(s) to override or introduce (overrides values in airavata-server.properties)"
            echo "  -nologo                 	     Do not show airavata logo"
            echo "  -xdebug                 	     Start Airavata Server under JPDA debugger"
            echo "  -security               	     Enable Java 2 security"
            echo "  -h                               Display this help and exit"
            shift
            exit 0
        ;;
	*)
	    AIRAVATA_COMMAND="$AIRAVATA_COMMAND $var"
            shift
    esac
done
if $LOGO ; then
	if [ -e $LOGO_FILE ]
	then
		cat $LOGO_FILE
	fi
fi
if $IS_DAEMON_MODE ; then
	echo "Starting airavata server in daemon mode..."
	nohup java $JAVA_OPTS -classpath "$XBAYA_CLASSPATH" \
	    -Djava.endorsed.dirs="$AIRAVATA_HOME/lib/endorsed":"$JAVA_HOME/jre/lib/endorsed":"$JAVA_HOME/lib/endorsed" \
	    org.apache.airavata.server.ServerMain $AIRAVATA_COMMAND $* > airavata-server.out & 
	echo
	echo
else
	java $JAVA_OPTS -classpath "$XBAYA_CLASSPATH" \
	    -Djava.endorsed.dirs="$AIRAVATA_HOME/lib/endorsed":"$JAVA_HOME/jre/lib/endorsed":"$JAVA_HOME/lib/endorsed" \
	    org.apache.airavata.server.ServerMain $AIRAVATA_COMMAND $*
fi

