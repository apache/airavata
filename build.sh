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

# todo Add environment specific docker image creation and create docker image per component (api-server, orchestrator, gfac etc)

echo $MAVEN_HOME
echo $PATH

cd $WORKSPACE/airavata-head/

/home/jenkins/tools/maven/apache-maven-3.3.9/bin/mvn  clean install -Dmaven.test.skip=true
if [ -d "docker-build" ]; then
    printf '%s\n' "Removing old docker-build directory"
    rm -rf docker-build
fi

mkdir docker-build
cp modules/distribution/target/apache-airavata-server*.zip docker-build

unzip docker-build/apache-airavata-server*.zip -d docker-build/airavata
rm docker-build/apache-airavata-server*.zip

cp deploy/images/airavata/Dockerfile docker-build/airavata/*/

cd docker-build/airavata/*/

# disable embedded zookeeper configuration
echo  embedded.zk=false >> bin/airavata-server.properties

component_name="all"
if [ $# -gt 0 ]
  then
      docker build --build-arg COMPONENT=${component_name} -t airavata-${component_name} .
      # docker push scigap/airavata-${component_name}
fi

docker build --build-arg COMPONENT=apiserver -t scigap/${environment}-airavata-apiserver .
# docker push scigap/airavata-apiserver

docker build --build-arg COMPONENT=gfac -t scigap/${environment}-airavata-gfac .
# docker push scigap/airavata-gfac

docker build --build-arg COMPONENT=orchestrator -t scigap/${environment}-airavata-orchestrator .
# docker push scigap/airavata-orchestrator

docker build --build-arg COMPONENT=credentialstore -t scigap/${environment}-airavata-credentialstore .
# docker push scigap/airavata-credentialstore


