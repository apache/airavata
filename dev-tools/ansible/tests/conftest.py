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

"""Pytest configuration and shared fixtures for Ansible deployment tests."""

import os
import tempfile
import subprocess
from pathlib import Path
from typing import Generator

import pytest


@pytest.fixture(scope="session")
def project_root() -> Path:
    """Return the project root directory."""
    return Path(__file__).parent.parent


@pytest.fixture(scope="session")
def ansible_root(project_root: Path) -> Path:
    """Return the Ansible directory."""
    return project_root


@pytest.fixture(scope="session")
def test_ssh_key() -> Generator[Path, None, None]:
    """Generate a temporary SSH key pair for container access."""
    with tempfile.TemporaryDirectory() as tmpdir:
        key_path = Path(tmpdir) / "test_key"
        pub_key_path = Path(tmpdir) / "test_key.pub"
        
        # Generate SSH key pair
        subprocess.run(
            ["ssh-keygen", "-t", "rsa", "-b", "4096", "-f", str(key_path), "-N", "", "-q"],
            check=True,
            capture_output=True,
        )
        
        yield key_path


@pytest.fixture(scope="session")
def docker_client():
    """Get Docker client for container management."""
    try:
        import docker
        client = docker.from_env()
        client.ping()  # Test connection
        return client
    except Exception as e:
        pytest.skip(f"Docker not available: {e}")


@pytest.fixture(scope="session")
def ansible_collections_installed() -> bool:
    """Verify that required Ansible collections are installed."""
    try:
        result = subprocess.run(
            ["ansible-galaxy", "collection", "list"],
            capture_output=True,
            text=True,
            check=True,
        )
        output = result.stdout
        
        required_collections = {
            "ansible.posix": ">=2.1.0",
            "community.general": ">=12.2.0",
            "community.mysql": ">=4.0.1",
        }
        
        for collection, version_spec in required_collections.items():
            if collection not in output:
                pytest.skip(f"Required collection {collection} not installed")
        
        return True
    except subprocess.CalledProcessError:
        pytest.skip("ansible-galaxy not available")
    except FileNotFoundError:
        pytest.skip("ansible-galaxy not found in PATH")


@pytest.fixture(scope="function")
def temp_inventory_dir() -> Generator[Path, None, None]:
    """Create a temporary directory for test inventory files."""
    with tempfile.TemporaryDirectory() as tmpdir:
        yield Path(tmpdir)
