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
COPY ${api.server.dist.name}-bin.tar.gz /opt/
RUN tar -xzf /opt/${api.server.dist.name}-bin.tar.gz && \
    rm /opt/${api.server.dist.name}-bin.tar.gz && \
    mv /opt/${api.server.dist.name} /opt/apache-airavata

# Copy wait-for-it script for container orchestration
COPY wait-for-it.sh /opt/apache-airavata/wait-for-it.sh
RUN chmod +x /opt/apache-airavata/wait-for-it.sh

# Add required configuration
RUN echo "api.server.monitoring.enabled=false" >> /opt/apache-airavata/bin/airavata-server.properties

# Set environment variables
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV AIRAVATA_HOME=/opt/apache-airavata

# Expose the default API server port
EXPOSE 8960 8930 7878 8962 8970

# Set the entrypoint to use the shell script
ENTRYPOINT ["/opt/apache-airavata/bin/airavata-server-start.sh"]
CMD ["api-orch"]
