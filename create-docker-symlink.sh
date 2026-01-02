#!/bin/bash
# Script to create Docker socket symlink for Testcontainers
# This requires sudo access

echo "Creating symlink from /var/run/docker.sock to ~/.rd/docker.sock"
echo "This requires sudo access. Please enter your password when prompted."

sudo ln -sf ~/.rd/docker.sock /var/run/docker.sock

if [ $? -eq 0 ]; then
    echo "✓ Symlink created successfully"
    ls -la /var/run/docker.sock
else
    echo "✗ Failed to create symlink"
    exit 1
fi
