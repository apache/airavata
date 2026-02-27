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

"""Tests for the database role on all distributions."""

import pytest
import paramiko

from tests.fixtures.containers import ubuntu_container, rocky_container, centos_container


def _test_database_role(container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Helper function to test database role on any distribution."""
    container_info = container.container_info
    inventory = inventory_generator(
        container_info=container_info,
        group_name="database",
        extra_vars={
            "user": "airavata",
            "group": "airavata",
            "db_password": "test_password",
            "mysql_root_password": "test_root_password",
            "db_name": "airavata",
        },
    )
    
    playbook_content = """---
- name: Database Role Test
  hosts: database
  become: true
  roles:
    - base
    - role: database
      become_user: "{{ user }}"
"""
    playbook_path = inventory.parent / "test_database.yml"
    playbook_path.write_text(playbook_content)
    
    result = ansible_runner(
        playbook=str(playbook_path),
        inventory=inventory,
        group_name="database",
        tags=["database"],
    )
    
    assert result.returncode == 0, f"Ansible failed:\nSTDERR: {result.stderr}\nSTDOUT: {result.stdout}"
    
    # Verify MariaDB is running
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(
        container_info["host"],
        port=container_info["port"],
        username="root",
        key_filename=str(test_ssh_key),
    )
    
    try:
        stdin, stdout, stderr = ssh.exec_command("systemctl is-active mariadb || systemctl is-active mysql")
        status = stdout.read().decode().strip()
        assert status == "active", f"MariaDB/MySQL is not running: {status}"
        
        # Verify database exists
        stdin, stdout, stderr = ssh.exec_command(
            "mysql -u root -ptest_root_password -e 'SHOW DATABASES LIKE \"airavata\";'"
        )
        output = stdout.read().decode()
        assert "airavata" in output, f"Database 'airavata' was not created: {output}"
    finally:
        ssh.close()


@pytest.mark.database
@pytest.mark.ubuntu
def test_database_role_ubuntu(ubuntu_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test database role deployment on Ubuntu."""
    _test_database_role(ubuntu_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)


@pytest.mark.database
@pytest.mark.rocky
def test_database_role_rocky(rocky_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test database role deployment on Rocky Linux."""
    _test_database_role(rocky_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)


@pytest.mark.database
@pytest.mark.centos
def test_database_role_centos(centos_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier):
    """Test database role deployment on CentOS."""
    _test_database_role(centos_container, ansible_runner, inventory_generator, test_ssh_key, collection_verifier)
