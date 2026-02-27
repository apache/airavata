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

"""Utilities for generating Ansible inventory files from containers."""

from pathlib import Path
from typing import Dict, List, Optional


def generate_inventory(
    inventory_path: Path,
    hosts: Dict[str, Dict],
    group_vars: Optional[Dict] = None,
) -> None:
    """Generate an Ansible inventory file from container information.
    
    Args:
        inventory_path: Path where inventory file will be written
        hosts: Dict mapping host groups to list of host info dicts
               Each host info dict should have: host, port, user, ssh_key_path
        group_vars: Optional dict of group variables
    """
    lines = []
    
    for group_name, host_list in hosts.items():
        lines.append(f"[{group_name}]")
        for host_info in host_list:
            host = host_info["host"]
            port = host_info.get("port", 22)
            user = host_info.get("user", "root")
            ssh_key = host_info.get("ssh_key_path")
            
            # Build ansible_host line
            host_line = f"{host}"
            if port != 22:
                host_line += f" ansible_port={port}"
            if user != "root":
                host_line += f" ansible_user={user}"
            if ssh_key:
                host_line += f" ansible_ssh_private_key_file={ssh_key}"
            
            lines.append(host_line)
        lines.append("")
    
    # Add group_vars if provided
    if group_vars:
        lines.append("[all:vars]")
        for key, value in group_vars.items():
            if isinstance(value, str) and " " in value:
                lines.append(f'{key}="{value}"')
            else:
                lines.append(f"{key}={value}")
        lines.append("")
    
    inventory_path.write_text("\n".join(lines))


def create_test_inventory(
    inventory_path: Path,
    container_info: Dict,
    group_name: str,
    ssh_key_path: Path,
    extra_vars: Optional[Dict] = None,
) -> None:
    """Create a simple inventory file for a single container.
    
    Args:
        inventory_path: Path where inventory file will be written
        container_info: Dict with host, port, user from container setup
        group_name: Ansible host group name
        ssh_key_path: Path to SSH private key
        extra_vars: Optional extra variables to add
    """
    hosts = {
        group_name: [{
            **container_info,
            "ssh_key_path": str(ssh_key_path),
        }],
    }
    
    group_vars = extra_vars or {}
    group_vars.update({
        "ansible_connection": "ssh",
        "ansible_host_key_checking": "False",
        "ansible_python_interpreter": "/usr/bin/python3",
    })
    
    generate_inventory(inventory_path, hosts, group_vars)
