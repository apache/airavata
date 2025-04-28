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

JAVA_OPTS=""
AIRAVATA_COMMAND=""
FORCE=false

for var in "$@"
do
    case ${var} in
    	-f | --force)
	        FORCE=true
            shift
        ;;
        -h)
            echo "Usage: airavata-server-stop.sh [command-options]"
            echo "command options:"
	        echo "  -f , --force       Force stop all airavata servers."
	        echo "  --<key>[=<value>]  Server setting(s) to override or introduce (overrides values in airavata-server.properties)"
            echo "  -h                 Display this help and exit"
            shift
            exit 0
        ;;
	*)
            shift
    esac
done

if ${FORCE} ; then
	for f in `find . -name "server_start_*"`; do
	    # split file name using "_" underscore
		f_split=(${f//_/ });
		echo "Found process file : $f"
		echo -n "    Sending kill signals to process ${f_split[2]}..."
		out=`kill -9 ${f_split[2]} 2>&1`
		if [ -z "$out" ]; then
		    echo "done"
		else
		    echo "failed (REASON: $out)"
		fi
		echo -n "    Removing process file..."
		out=`rm ${f} 2>&1`
		if [ -z "$out" ]; then
		    echo "done"
		else
		    echo "failed (REASON: $out)"
		fi
	done
else
    java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" \
    org.apache.airavata.server.ServerMain stop ${AIRAVATA_COMMAND} $*
fi
