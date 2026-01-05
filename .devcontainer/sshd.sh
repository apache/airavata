#!/bin/bash

set -e

# Install openssh-server
apt-get update && apt-get install -y openssh-server

# Setup sshd directory and root password
mkdir -p /var/run/sshd
echo 'root:root' | chpasswd

# Allow root login over ssh
sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# SSH login fix
sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd

# Set environment variable for login profile visibility
echo "export VISIBLE=now" >> /etc/profile

# Create data directories
mkdir -p /var/www/portals/gateway-user-data
mkdir -p /root/.ssh

# Copy in the authorized_keys file
cp /path/to/authorized_keys /root/.ssh/authorized_keys

# Set permissions
chmod 700 /root/.ssh
chmod 644 /root/.ssh/authorized_keys

# Expose SSH port
echo "To expose port 22, run the container with: -p 22:22"

# Start the SSH service
/usr/sbin/sshd -D
