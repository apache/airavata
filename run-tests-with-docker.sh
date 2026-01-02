#!/bin/bash
# Script to run tests with Docker/nerdctl support
# This sets up the environment entirely outside of Java code

export DOCKER_HOST=unix://$HOME/.lima/default/sock/docker.sock

# Verify Docker is accessible
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not accessible at $DOCKER_HOST"
    echo "Please ensure Lima is running and Docker socket is available"
    exit 1
fi

echo "DOCKER_HOST is set to: $DOCKER_HOST"
echo "Running tests..."

cd /Users/pjayawardana3/Projects/airavata
mm activate airavata
mvn test -pl airavata-api "$@"
