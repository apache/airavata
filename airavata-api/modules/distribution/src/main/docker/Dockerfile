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

ARG DIST_NAME=airavata-0.21-SNAPSHOT

FROM eclipse-temurin:25-jre
ARG DIST_NAME

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /opt

COPY ${DIST_NAME}.tar.gz /opt/
RUN tar -xzf /opt/${DIST_NAME}.tar.gz && \
    rm /opt/${DIST_NAME}.tar.gz && \
    mv /opt/${DIST_NAME} /opt/apache-airavata && \
    chmod +x /opt/apache-airavata/bin/*.sh

# airavata-agent is a pre-built Go binary used to proxy SCP/SFTP operations
# to remote compute servers on behalf of the API server. It must be present
# in the Docker build context (distribution/target/) before running docker build.
COPY airavata-agent /opt/apache-airavata/bin/airavata-agent
RUN chmod +x /opt/apache-airavata/bin/airavata-agent

ENV AIRAVATA_HOME=/opt/apache-airavata

# Unified HTTP server
EXPOSE 8090
# Unified gRPC server
EXPOSE 9090
# Dapr gRPC (DaprClient uses gRPC-only mode)
EXPOSE 50001

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s \
  CMD curl -f http://localhost:8090/actuator/health || exit 1

ENTRYPOINT ["/opt/apache-airavata/bin/airavata.sh"]
CMD ["serve"]
