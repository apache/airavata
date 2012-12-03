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
if [ -e $LOGO_FILE ]
then
	cat $LOGO_FILE
fi

JAVA_OPTS=""
while [ $# -ge 1 ]; do
    case $1 in
        -xdebug)
            JAVA_OPTS="$JAVA_OPTS -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=8000"
            shift
        ;;
        -security)
            JAVA_OPTS="$JAVA_OPTS -Djava.security.manager -Djava.security.policy=$AIRAVATA_HOME/conf/axis2.policy -Daxis2.home=$AIRAVATA_HOME"
            shift
        ;;
        -h)
            echo "Usage: airavata-server.sh"
            echo "commands:"
            echo "  -xdebug    Start Airavata Server under JPDA debugger"
            echo "  -security  Enable Java 2 security"
            echo "  -h         help"
            shift
            exit 0
        ;;
        *)
            echo "Error: unknown command:$1"
            echo "For help: airavata-server.sh -h"
            shift
            exit 1
    esac
done

java $JAVA_OPTS -classpath "$XBAYA_CLASSPATH" \
    -Djava.endorsed.dirs="$AIRAVATA_HOME/lib/endorsed":"$JAVA_HOME/jre/lib/endorsed":"$JAVA_HOME/lib/endorsed" \
    org.apache.airavata.server.ServerMain \
    -repo "$AIRAVATA_HOME"/repository/services -conf "$AIRAVATA_HOME"/conf/axis2.xml $*

