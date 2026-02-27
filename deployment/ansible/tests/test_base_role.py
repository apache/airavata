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

"""Tests for the base role on all distributions."""

import pytest
import paramiko
from pathlib import Path

from tests.fixtures.containers import ubuntu_container, rocky_container, centos_container


def _test_base_role(container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Helper function to test base role on any distribution."""
    container_info = container.container_info
    inventory = inventory_generator(
        container_info=container_info,
        group_name="base",
        extra_vars={
            "user": "airavata",
            "group": "airavata",
            "db_password": "test_password",
            "mysql_root_password": "test_root_password",
        },
    )
    
    # Create a minimal playbook for base role
    playbook_content = """---
- name: Base Role Test
  hosts: base
  become: true
  roles:
    - base
"""
    playbook_path = inventory.parent / "test_base.yml"
    playbook_path.write_text(playbook_content)
    
    result = ansible_runner(
        playbook=str(playbook_path),
        inventory=inventory,
        group_name="base",
    )
    
    assert result.returncode == 0, f"Ansible failed:\nSTDERR: {result.stderr}\nSTDOUT: {result.stdout}"
    
    # Verify user was created
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(
        container_info["host"],
        port=container_info["port"],
        username="root",
        key_filename=str(test_ssh_key),
    )
    
    try:
        stdin, stdout, stderr = ssh.exec_command("id airavata")
        exit_code = stdout.channel.recv_exit_status()
        assert exit_code == 0, f"User airavata was not created: {stderr.read().decode()}"
        
        # Verify Java is installed
        stdin, stdout, stderr = ssh.exec_command("java -version")
        exit_code = stdout.channel.recv_exit_status()
        assert exit_code == 0, f"Java is not installed: {stderr.read().decode()}"
    finally:
        ssh.close()


@pytest.mark.base
@pytest.mark.ubuntu
def test_base_role_ubuntu(ubuntu_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test base role deployment on Ubuntu."""
    _test_base_role(ubuntu_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)


@pytest.mark.base
@pytest.mark.rocky
def test_base_role_rocky(rocky_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test base role deployment on Rocky Linux."""
    _test_base_role(rocky_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)


@pytest.mark.base
@pytest.mark.centos
def test_base_role_centos(centos_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test base role deployment on CentOS."""
    _test_base_role(centos_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)
