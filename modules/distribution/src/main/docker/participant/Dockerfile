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

FROM eclipse-temurin:17-jre

WORKDIR /opt

COPY ${participant.dist.name}-bin.tar.gz .
RUN tar -xzf ${participant.dist.name}-bin.tar.gz && \
    mv ${participant.dist.name} apache-airavata-participant && \
    rm ${participant.dist.name}-bin.tar.gz

COPY wait-for-it.sh /tmp/
RUN chmod +x /tmp/wait-for-it.sh

ENV JAVA_HOME=/opt/java/openjdk
ENV AIRAVATA_HOME=/opt/apache-airavata-participant

# Expose monitoring port
EXPOSE 8080

ENTRYPOINT ["/opt/apache-airavata-participant/bin/participant.sh"]