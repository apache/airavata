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

# update classpath
REG_MIGRATE_CLASSPATH="$AIRAVATA_HOME/lib"
for f in $AIRAVATA_HOME/lib/*.jar
do
  REG_MIGRATE_CLASSPATH=$REG_MIGRATE_CLASSPATH:$f
done

java -server -Xms128M -Xmx128M \
   $XDEBUG \
   $TEMP_PROPS \
   -classpath $REG_MIGRATE_CLASSPATH \
   -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5000,suspend=n \
   org.apache.airavata.registry.tool.DBMigrator $*
