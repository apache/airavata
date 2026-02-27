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

"""Container fixtures using testcontainers-python."""

import pytest
from testcontainers.core.container import DockerContainer

from tests.utils.container_setup import setup_container_for_ansible


@pytest.fixture(scope="function")
def ubuntu_container(test_ssh_key) -> DockerContainer:
    """Create an Ubuntu 22.04 container with SSH setup."""
    container = (
        DockerContainer("ubuntu:22.04")
        .with_exposed_ports(22)
        .with_command("/bin/bash -c 'while true; do sleep 3600; done'")
    )
    
    container.start()
    
    try:
        # Setup container for Ansible
        docker_container = container.get_wrapped_container()
        container_info = setup_container_for_ansible(
            docker_container,
            os_family="Debian",
            ssh_pub_key=test_ssh_key.with_suffix(".pub"),
        )
        
        # Store container info as attribute
        container.container_info = container_info
        
        yield container
    finally:
        container.stop()


@pytest.fixture(scope="function")
def rocky_container(test_ssh_key) -> DockerContainer:
    """Create a Rocky Linux 8 container with SSH setup."""
    container = (
        DockerContainer("rockylinux:8")
        .with_exposed_ports(22)
        .with_command("/bin/bash -c 'while true; do sleep 3600; done'")
    )
    
    container.start()
    
    try:
        # Setup container for Ansible
        docker_container = container.get_wrapped_container()
        container_info = setup_container_for_ansible(
            docker_container,
            os_family="RedHat",
            ssh_pub_key=test_ssh_key.with_suffix(".pub"),
        )
        
        # Store container info as attribute
        container.container_info = container_info
        
        yield container
    finally:
        container.stop()


@pytest.fixture(scope="function")
def centos_container(test_ssh_key) -> DockerContainer:
    """Create a CentOS 7 container with SSH setup."""
    # Try quay.io first, fallback to regular centos:7
    try:
        container = (
            DockerContainer("quay.io/centos/centos:7")
            .with_exposed_ports(22)
            .with_command("/bin/bash -c 'while true; do sleep 3600; done'")
        )
    except Exception:
        container = (
            DockerContainer("centos:7")
            .with_exposed_ports(22)
            .with_command("/bin/bash -c 'while true; do sleep 3600; done'")
        )
    
    container.start()
    
    try:
        # Setup container for Ansible
        docker_container = container.get_wrapped_container()
        container_info = setup_container_for_ansible(
            docker_container,
            os_family="RedHat",
            ssh_pub_key=test_ssh_key.with_suffix(".pub"),
        )
        
        # Store container info as attribute
        container.container_info = container_info
        
        yield container
    finally:
        container.stop()


@pytest.fixture(params=["ubuntu", "rocky", "centos"])
def any_container(request, ubuntu_container, rocky_container, centos_container):
    """Parametrized fixture to test against all distributions."""
    fixtures = {
        "ubuntu": ubuntu_container,
        "rocky": rocky_container,
        "centos": centos_container,
    }
    return fixtures[request.param]
