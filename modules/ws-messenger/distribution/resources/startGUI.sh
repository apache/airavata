#!/bin/sh

#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

DISTRIBUTION_ROOT=$(dirname `pwd`) 

BROKER_CLASS_PATH=""

#broker libraries 
for f in "$DISTRIBUTION_ROOT"/client-api/lib/airavata-messagebroker*.jar
do
  BROKER_CLASS_PATH="$BROKER_CLASS_PATH":$f
done


#axis libraries 
for f in "$DISTRIBUTION_ROOT"/standalone-server/lib/*.jar
do
  BROKER_CLASS_PATH="$BROKER_CLASS_PATH":$f
done

echo $BROKER_CLASS_PATH


java -classpath "$BROKER_CLASS_PATH" org.apache.airavata.wsmg.gui.NotificationViewer


