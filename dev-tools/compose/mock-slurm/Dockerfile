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

FROM debian:bookworm-slim

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
    openssh-server \
    openssh-client \
    bash \
    coreutils \
    procps \
    sudo \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Create slurmuser and configure passwords for mock SSH
RUN useradd -m -s /bin/bash -u 1000 slurmuser && \
    echo "slurmuser:slurmuser" | chpasswd && \
    echo "root:root" | chpasswd && \
    echo "slurmuser ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/slurmuser && \
    chmod 0440 /etc/sudoers.d/slurmuser

# Configure OpenSSH
COPY sshd_config /etc/ssh/sshd_config

# Install mock slurm commands
COPY bin/ /usr/local/bin/
RUN chmod +x /usr/local/bin/s* && \
    mkdir -p /opt/slurm/bin && \
    for f in /usr/local/bin/s*; do \
        ln -sf "$f" "/opt/slurm/bin/$(basename "$f")"; \
        ln -sf "$f" "/opt/slurm/$(basename "$f")"; \
    done

# Copy development public key for authorized_keys setup
RUN mkdir -p /etc/mock-slurm
COPY keys/id_ed25519.pub /etc/mock-slurm/id_ed25519.pub

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 22

ENTRYPOINT ["/entrypoint.sh"]
