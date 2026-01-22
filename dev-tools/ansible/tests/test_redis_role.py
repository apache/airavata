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

"""Tests for the Redis role on all distributions."""

import pytest
import paramiko

from tests.fixtures.containers import ubuntu_container, rocky_container, centos_container


def _test_redis_role(container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Helper function to test Redis role on any distribution."""
    container_info = container.container_info
    inventory = inventory_generator(
        container_info=container_info,
        group_name="redis",
        extra_vars={
            "user": "airavata",
            "group": "airavata",
        },
    )
    
    # Use the Redis role
    playbook_content = """---
- name: Redis Test
  hosts: redis
  roles:
    - base
    - role: redis
      become: true
"""
    playbook_path = inventory.parent / "test_redis.yml"
    playbook_path.write_text(playbook_content)
    
    result = ansible_runner(
        playbook=str(playbook_path),
        inventory=inventory,
        group_name="redis",
        tags=["redis"],
    )
    
    assert result.returncode == 0, f"Ansible failed:\nSTDERR: {result.stderr}\nSTDOUT: {result.stdout}"
    
    # Verify Redis is running
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(
        container_info["host"],
        port=container_info["port"],
        username="root",
        key_filename=str(test_ssh_key),
    )
    
    try:
        stdin, stdout, stderr = ssh.exec_command("systemctl is-active redis || systemctl is-active redis-server")
        status = stdout.read().decode().strip()
        assert status == "active", f"Redis is not running: {status}"
        
        # Test Redis connection
        stdin, stdout, stderr = ssh.exec_command("redis-cli ping")
        output = stdout.read().decode().strip()
        assert output == "PONG", f"Redis is not responding: {output}"
    finally:
        ssh.close()


@pytest.mark.redis
@pytest.mark.ubuntu
def test_redis_role_ubuntu(ubuntu_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test Redis role deployment on Ubuntu."""
    _test_redis_role(ubuntu_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)


@pytest.mark.redis
@pytest.mark.rocky
def test_redis_role_rocky(rocky_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test Redis role deployment on Rocky Linux."""
    _test_redis_role(rocky_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)


@pytest.mark.redis
@pytest.mark.centos
def test_redis_role_centos(centos_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test Redis role deployment on CentOS."""
    _test_redis_role(centos_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)
