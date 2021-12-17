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
LOGO=true
IS_SUBSET=false
SUBSET=""
DEFAULT_LOG_FILE="${AIRAVATA_HOME}/logs/airavata-daemon.out"
LOG_FILE=$DEFAULT_LOG_FILE

SERVICE_NAME="Helix Controller"
PID_PATH_NAME="${AIRAVATA_HOME}/bin/service-pid"

case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            nohup java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" \
            org.apache.airavata.helix.impl.controller.HelixController ${AIRAVATA_COMMAND} $* > $LOG_FILE 2>&1 &
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill $PID;
            RETRY=0
            while kill -0 $PID 2> /dev/null; do
                echo "Waiting for the process $PID to be stopped"
                RETRY=`expr ${RETRY} + 1`
                if [ "${RETRY}" -gt "20" ]
                then
                    echo "Forcefully killing the process as it is not responding ..."
                    kill -9 $PID
                fi
                sleep 1
            done
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            RETRY=0
            while kill -0 $PID 2> /dev/null; do
                echo "Waiting for the process $PID to be stopped"
                RETRY=`expr ${RETRY} + 1`
                if [ "${RETRY}" -gt "20" ]
                then
                    echo "Forcefully killing the process as it is not responding ..."
                    kill -9 $PID
                fi
                sleep 1
            done
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            nohup java ${JAVA_OPTS} -classpath "${AIRAVATA_CLASSPATH}" \
            org.apache.airavata.helix.impl.controller.HelixController ${AIRAVATA_COMMAND} $* > $LOG_FILE 2>&1 &
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    -h)
        echo "Usage: controller-daemon.sh"

        echo "command options:"
        echo "  start               Start server in daemon mode"
        echo "  stop                Stop server running in daemon mode"
        echo "  restart             Restart server in daemon mode"
	    echo "  -log <LOG_FILE>     Where to redirect stdout/stderr (defaults to $DEFAULT_LOG_FILE)"
        echo "  -h                  Display this help and exit"
        shift
        exit 0
    ;;
esac

