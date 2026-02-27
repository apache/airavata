#!/bin/bash
#
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

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANSIBLE_DIR="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo "Airavata Ansible Test Runner"
echo "=========================================="

# Check if virtual environment exists
if [ ! -d "$ANSIBLE_DIR/ENV" ]; then
    echo "Creating virtual environment..."
    cd "$ANSIBLE_DIR"
    python3 -m venv ENV
    source ENV/bin/activate
    
    echo "Installing dependencies..."
    pip install --upgrade pip
    pip install -e ".[dev]"
    
    echo "Installing Ansible collections..."
    ansible-galaxy collection install -r requirements.yml
else
    echo "Activating existing virtual environment..."
    source "$ANSIBLE_DIR/ENV/bin/activate"
fi

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed or not in PATH"
    exit 1
fi

if ! docker ps &> /dev/null; then
    echo "ERROR: Docker daemon is not running"
    exit 1
fi

echo ""
echo "Running tests..."
echo ""

cd "$SCRIPT_DIR"

# Run pytest with options
pytest "$@" \
    --verbose \
    --tb=short \
    --timeout=1800 \
    -m "not slow" \
    "$SCRIPT_DIR"

echo ""
echo "=========================================="
echo "Tests completed"
echo "=========================================="
