#!/bin/bash
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

# Full environment setup wrapper script for Airavata deployment

set -e

if [ -z "$1" ]; then
    echo "Usage: $0 <environment>"
    echo ""
    echo "Deploys complete Airavata environment from scratch including all infrastructure components."
    echo ""
    echo "Examples:"
    echo "  $0 prod"
    echo "  $0 dev"
    echo "  $0 staging"
    exit 1
fi

ENV=$1
INVENTORY="inventories/$ENV"

if [ ! -d "$INVENTORY" ]; then
    echo "Error: Inventory directory '$INVENTORY' not found"
    echo ""
    echo "Available inventories:"
    ls -1 inventories/ 2>/dev/null || echo "  (none found)"
    exit 1
fi

echo "=========================================="
echo "Full Airavata Setup Deployment"
echo "=========================================="
echo "Environment: $ENV"
echo "Inventory: $INVENTORY"
echo ""
echo "This will set up a complete Airavata environment from scratch,"
echo "including Java, Maven, Zookeeper, Kafka, RabbitMQ, MariaDB,"
echo "SSL certificates, and all Airavata services."
echo ""
read -p "Continue? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Deployment cancelled."
    exit 1
fi

echo "Starting deployment..."
ansible-playbook -i "$INVENTORY" airavata_setup.yml --ask-vault-pass

echo ""
echo "=========================================="
echo "Deployment completed!"
echo "=========================================="

