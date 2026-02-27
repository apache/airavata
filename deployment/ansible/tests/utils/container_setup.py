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

"""Utilities for setting up containers for Ansible testing."""

import time
import socket
import subprocess
from pathlib import Path
from typing import Optional

import paramiko


def install_python3(container, os_family: str) -> None:
    """Install Python 3 in the container based on OS family."""
    if os_family == "Debian":
        container.exec_run("apt-get update", tty=True)
        container.exec_run("apt-get install -y python3 python3-pip", tty=True)
    elif os_family == "RedHat":
        container.exec_run("yum install -y python3 python3-pip", tty=True)
    else:
        raise ValueError(f"Unsupported OS family: {os_family}")


def setup_ssh_server(container, os_family: str, ssh_pub_key: Path) -> None:
    """Setup SSH server in the container and add public key."""
    # Install SSH server
    if os_family == "Debian":
        container.exec_run("apt-get update -qq", tty=True)
        result = container.exec_run(
            "apt-get install -y openssh-server",
            tty=True,
            environment={"DEBIAN_FRONTEND": "noninteractive"},
        )
        if result.exit_code != 0:
            raise RuntimeError(f"Failed to install openssh-server: {result.output.decode()}")
        # Generate host keys if they don't exist
        container.exec_run("ssh-keygen -A", tty=True)
        # Configure SSH to allow root login
        container.exec_run("sed -i 's/#PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config", tty=True)
        container.exec_run("sed -i 's/PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config", tty=True)
        container.exec_run("sed -i 's/#PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config", tty=True)
        container.exec_run("sed -i 's/PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config", tty=True)
        container.exec_run("mkdir -p /var/run/sshd", tty=True)
    elif os_family == "RedHat":
        result = container.exec_run("yum install -y openssh-server openssh-clients", tty=True)
        if result.exit_code != 0:
            raise RuntimeError(f"Failed to install openssh-server: {result.output.decode()}")
        container.exec_run("ssh-keygen -A", tty=True)
        container.exec_run("sed -i 's/#PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config", tty=True)
        container.exec_run("sed -i 's/PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config", tty=True)
        container.exec_run("sed -i 's/#PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config", tty=True)
        container.exec_run("sed -i 's/PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config", tty=True)
        container.exec_run("mkdir -p /var/run/sshd", tty=True)
    
    # Create .ssh directory and add public key
    container.exec_run("mkdir -p /root/.ssh", tty=True)
    container.exec_run("chmod 700 /root/.ssh", tty=True)
    
    # Read and add public key - write directly to avoid shell escaping issues
    pub_key_content = ssh_pub_key.read_text().strip()
    # Use a here-document to write the key safely
    container.exec_run(
        f"bash -c 'cat > /root/.ssh/authorized_keys << EOFKEY\n{pub_key_content}\nEOFKEY'",
        tty=True,
    )
    container.exec_run("chmod 600 /root/.ssh/authorized_keys", tty=True)
    
    # Start SSH daemon in background using nohup to keep it running
    container.exec_run("bash -c 'nohup /usr/sbin/sshd -D > /dev/null 2>&1 &'", tty=True)
    # Give SSH a moment to start and verify it's running
    time.sleep(5)
    # Verify sshd is running
    result = container.exec_run("pgrep -f 'sshd.*-D' || pgrep sshd", tty=True)
    if result.exit_code != 0:
        # Try starting again without nohup
        container.exec_run("bash -c '/usr/sbin/sshd'", tty=True)
        time.sleep(3)


def install_basic_packages(container, os_family: str) -> None:
    """Install basic packages needed for Ansible."""
    packages = ["git", "curl", "wget", "sudo"]
    
    if os_family == "Debian":
        container.exec_run("apt-get update", tty=True)
        container.exec_run(f"apt-get install -y {' '.join(packages)}", tty=True)
    elif os_family == "RedHat":
        container.exec_run(f"yum install -y {' '.join(packages)}", tty=True)


def wait_for_ssh(host: str, port: int, timeout: int = 120) -> bool:
    """Wait for SSH to be available on the container."""
    start_time = time.time()
    
    while time.time() - start_time < timeout:
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.settimeout(2)
            result = sock.connect_ex((host, port))
            sock.close()
            if result == 0:
                # Port is open, try SSH connection
                transport = paramiko.Transport((host, port))
                transport.set_missing_host_key_policy(paramiko.AutoAddPolicy())
                transport.connect(username="root", look_for_keys=False, allow_agent=False)
                transport.close()
                return True
        except Exception:
            pass
        time.sleep(2)
    
    return False


def setup_container_for_ansible(
    container,
    os_family: str,
    ssh_pub_key: Path,
    wait_ssh: bool = True,
) -> dict:
    """Setup container for Ansible testing.
    
    Returns:
        dict with container info: host, port, etc.
    """
    # Install Python 3
    install_python3(container, os_family)
    
    # Install basic packages
    install_basic_packages(container, os_family)
    
    # Setup SSH
    setup_ssh_server(container, os_family, ssh_pub_key)
    
    # Get container info
    container.reload()
    ports = container.attrs["NetworkSettings"]["Ports"]
    
    # Try to get port from bindings first (for exposed ports)
    if "22/tcp" in ports and ports["22/tcp"]:
        host = "localhost"
        port = int(ports["22/tcp"][0]["HostPort"])
    else:
        # Fallback to container IP
        host = container.attrs["NetworkSettings"]["IPAddress"]
        if not host:
            raise RuntimeError("Could not determine container host/port")
        port = 22
    
    # Wait for SSH if requested
    if wait_ssh:
        if not wait_for_ssh(host, port):
            raise RuntimeError(f"SSH not available on {host}:{port} after timeout")
    
    return {
        "host": host,
        "port": port,
        "user": "root",
    }
