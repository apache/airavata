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

"""Ansible execution fixtures and utilities."""

import subprocess
import tempfile
from pathlib import Path
from typing import Dict, Optional

import pytest

from tests.utils.inventory import create_test_inventory


@pytest.fixture
def ansible_config(ansible_root: Path, temp_inventory_dir: Path) -> Path:
    """Create a test-specific ansible.cfg file."""
    config_path = temp_inventory_dir / "ansible.cfg"
    
    config_content = f"""[defaults]
inventory = {temp_inventory_dir}
host_key_checking = False
retry_files_enabled = False
roles_path = {ansible_root / 'roles'}
collections_paths = ~/.ansible/collections:/usr/share/ansible/collections
"""
    
    config_path.write_text(config_content)
    return config_path


@pytest.fixture
def collection_verifier() -> Dict[str, str]:
    """Verify required Ansible collections are installed and return versions."""
    try:
        result = subprocess.run(
            ["ansible-galaxy", "collection", "list", "--format", "json"],
            capture_output=True,
            text=True,
            check=True,
        )
        
        import json
        collections = json.loads(result.stdout)
        
        required = {
            "ansible.posix": ">=2.1.0",
            "community.general": ">=12.2.0",
            "community.mysql": ">=4.0.1",
        }
        
        installed_versions = {}
        for collection_name, version_spec in required.items():
            namespace, collection = collection_name.split(".")
            found = False
            for coll in collections.get(namespace, {}).get(collection, []):
                installed_version = coll.get("version", "unknown")
                installed_versions[collection_name] = installed_version
                found = True
                break
            
            if not found:
                pytest.fail(f"Required collection {collection_name} not installed")
        
        return installed_versions
    except subprocess.CalledProcessError as e:
        pytest.fail(f"Failed to check collections: {e}")
    except FileNotFoundError:
        pytest.fail("ansible-galaxy not found in PATH")


def run_ansible_playbook(
    playbook_path: Path,
    inventory_path: Path,
    ansible_config: Optional[Path] = None,
    extra_vars: Optional[Dict] = None,
    tags: Optional[list] = None,
    check_mode: bool = False,
) -> subprocess.CompletedProcess:
    """Run an Ansible playbook and return the result.
    
    Args:
        playbook_path: Path to the playbook file
        inventory_path: Path to the inventory file
        ansible_config: Optional path to ansible.cfg
        extra_vars: Optional dict of extra variables
        tags: Optional list of tags to run
        check_mode: Run in check mode (dry-run)
    
    Returns:
        CompletedProcess with stdout, stderr, returncode
    """
    cmd = ["ansible-playbook", str(playbook_path)]
    
    if ansible_config:
        cmd.extend(["-c", str(ansible_config)])
    
    cmd.extend(["-i", str(inventory_path)])
    
    if extra_vars:
        vars_str = " ".join(f"{k}={v}" for k, v in extra_vars.items())
        cmd.extend(["--extra-vars", vars_str])
    
    if tags:
        cmd.extend(["--tags", ",".join(tags)])
    
    if check_mode:
        cmd.append("--check")
    
    result = subprocess.run(
        cmd,
        capture_output=True,
        text=True,
        cwd=playbook_path.parent,
    )
    
    return result


@pytest.fixture
def ansible_runner(ansible_root: Path, temp_inventory_dir: Path):
    """Fixture that provides a function to run Ansible playbooks."""
    def _run(
        playbook: str,
        inventory: Path,
        group_name: str = "test",
        extra_vars: Optional[Dict] = None,
        tags: Optional[list] = None,
        check_mode: bool = False,
    ) -> subprocess.CompletedProcess:
        playbook_path = ansible_root / playbook
        if not playbook_path.exists():
            playbook_path = ansible_root / "deploy.yml"
        
        config_path = temp_inventory_dir / "ansible.cfg"
        config_content = f"""[defaults]
inventory = {inventory}
host_key_checking = False
retry_files_enabled = False
roles_path = {ansible_root / 'roles'}
collections_paths = ~/.ansible/collections:/usr/share/ansible/collections
"""
        config_path.write_text(config_content)
        
        return run_ansible_playbook(
            playbook_path=playbook_path,
            inventory_path=inventory,
            ansible_config=config_path,
            extra_vars=extra_vars,
            tags=tags,
            check_mode=check_mode,
        )
    
    return _run


@pytest.fixture
def inventory_generator(test_ssh_key: Path, temp_inventory_dir: Path):
    """Fixture that provides a function to generate inventory files."""
    def _generate(
        container_info: Dict,
        group_name: str = "test",
        extra_vars: Optional[Dict] = None,
    ) -> Path:
        inventory_path = temp_inventory_dir / f"inventory_{group_name}.yml"
        create_test_inventory(
            inventory_path=inventory_path,
            container_info=container_info,
            group_name=group_name,
            ssh_key_path=test_ssh_key,
            extra_vars=extra_vars,
        )
        # Store ssh_key for later use
        _generate.ssh_key = test_ssh_key
        return inventory_path
    
    return _generate
