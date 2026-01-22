#!/bin/bash
set -e

# Create data directory
mkdir -p /home/testuser/data
chown testuser:testuser /home/testuser/data

# Start SSH daemon in foreground
exec /usr/sbin/sshd -D
