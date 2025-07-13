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

# Set working directory
WORKDIR /opt

# Copy the extracted distribution files
COPY ${api.server.dist.name}.tar.gz /opt/
RUN tar -xzf /opt/${api.server.dist.name}-bin.tar.gz && \
    rm /opt/${api.server.dist.name}-bin.tar.gz && \
    mv /opt/${api.server.dist.name} /opt/apache-airavata

# Copy server start scripts
COPY ./*.sh /opt/apache-airavata/
RUN chmod +x /opt/apache-airavata/*.sh

# Set environment variables
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV AIRAVATA_HOME=/opt/apache-airavata

# sharing registry service
EXPOSE 7878
# tunnel service
EXPOSE 8000
# tunnel service (ingress)
EXPOSE 17000
# file service
EXPOSE 8050
# api service
EXPOSE 8930
# cred store service
EXPOSE 8960
# profile service
EXPOSE 8962
# registry service
EXPOSE 8970
# agent service (http)
EXPOSE 18800
# agent service (gRPC)
EXPOSE 19900
# research service (http)
EXPOSE 18889
# research service (gRPC)
EXPOSE 19908

CMD ["/opt/apache-airavata/services_up.sh && sleep infinity"]
