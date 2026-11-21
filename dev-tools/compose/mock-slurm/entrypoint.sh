#!/usr/bin/env bash
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

set -e

# Generate SSH host keys if not present
if [ ! -f /etc/ssh/ssh_host_ed25519_key ]; then
    ssh-keygen -t ed25519 -f /etc/ssh/ssh_host_ed25519_key -N ""
fi
if [ ! -f /etc/ssh/ssh_host_rsa_key ]; then
    ssh-keygen -t rsa -b 2048 -f /etc/ssh/ssh_host_rsa_key -N ""
fi

mkdir -p /var/run/sshd
mkdir -p /var/run/mock-slurm/jobs
chmod 777 /var/run/mock-slurm /var/run/mock-slurm/jobs

mkdir -p /home/slurmuser/scratch /home/slurmuser/work /scratch
chmod 777 /home/slurmuser/scratch /home/slurmuser/work /scratch
chown -R slurmuser:slurmuser /home/slurmuser

echo "Mock SSH/Slurm cluster container initialized and starting sshd on port 22..."
exec /usr/sbin/sshd -D -e "$@"
