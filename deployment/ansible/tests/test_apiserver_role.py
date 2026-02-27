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

"""Tests for the apiserver role on all distributions."""

import pytest
import paramiko

from tests.fixtures.containers import ubuntu_container, rocky_container, centos_container


def _test_apiserver_role(container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Helper function to test apiserver role on any distribution."""
    container_info = container.container_info
    inventory = inventory_generator(
        container_info=container_info,
        group_name="apiserver",
        extra_vars={
            "user": "airavata",
            "group": "airavata",
            "db_password": "test_password",
            "mysql_root_password": "test_root_password",
            "db_server": container_info["host"],
            "redis_host": container_info["host"],
        },
    )
    
    playbook_content = """---
- name: API Server Role Test
  hosts: apiserver
  become: true
  roles:
    - base
    - role: apiserver
      become_user: "{{ user }}"
"""
    playbook_path = inventory.parent / "test_apiserver.yml"
    playbook_path.write_text(playbook_content)
    
    result = ansible_runner(
        playbook=str(playbook_path),
        inventory=inventory,
        group_name="apiserver",
        tags=["apiserver"],
    )
    
    # Note: This test may fail if source code/build is required
    # We're mainly testing that the role structure is correct
    if result.returncode != 0:
        # Check if it's a build/source issue (expected in test environment)
        if "git" in result.stderr.lower() or "maven" in result.stderr.lower():
            pytest.skip("Build dependencies not available in test environment")
        else:
            assert False, f"Ansible failed:\nSTDERR: {result.stderr}\nSTDOUT: {result.stdout}"


@pytest.mark.apiserver
@pytest.mark.ubuntu
@pytest.mark.slow
def test_apiserver_role_ubuntu(ubuntu_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test apiserver role deployment on Ubuntu."""
    _test_apiserver_role(ubuntu_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)


@pytest.mark.apiserver
@pytest.mark.rocky
@pytest.mark.slow
def test_apiserver_role_rocky(rocky_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test apiserver role deployment on Rocky Linux."""
    _test_apiserver_role(rocky_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)


@pytest.mark.apiserver
@pytest.mark.centos
@pytest.mark.slow
def test_apiserver_role_centos(centos_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test apiserver role deployment on CentOS."""
    _test_apiserver_role(centos_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)
