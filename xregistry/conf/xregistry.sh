#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


#Set the classpath with all jars in the lib folder
for i in lib/*.jar
do
  CLASSPATH=$CLASSPATH:$i
done

#Configure necessary properties
PID_FILE=xregistry.pid
SERVER_START_CMD="$JAVA_HOME/bin/java -Dlog=xregistry.logger:ALL -classpath  $CLASSPATH org.apache.airavata.xregistry.impl.XregistryServer"

#For restarts - shutdown, previosuly running instances
echo "Shutting down any previously running server...."
if [ -f $PID_FILE ]; then
	 kill -9 `cat $PID_FILE` 2>/dev/null
fi

echo "Starting server..."

######## Standard Deployment ##########

LOG_DIR=`pwd`
LOG_FILE=$LOG_DIR/xregistry.log
nohup $SERVER_START_CMD $*>$LOG_FILE 2>$LOG_FILE &


######## Gateway Deployment ##########
#Gateway deployment using common log folder for all services and cronolog for daily log rotation
#Comment the parameters in Standard deployment and uncomment the following.

#LOG_DIR=/home/ogce/ogce_gateway_deployment/logs
#LOG_FILE=$LOG_DIR/xregistry-gateway-%Y-%m-%d.log
#nohup $START_CMD 2>&1 | cronolog $LOG_FILE &

#Capture process id and print logs
echo $! > $PID_FILE

echo "XRegistry startup log:"

sleep 2

cat $LOG_FILE

echo "XRegistry start completed.  Check the logs in $LOG_FILE to verify the startup procedure and get the service URL."

