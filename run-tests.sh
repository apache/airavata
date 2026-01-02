#!/bin/bash
export DOCKER_HOST=unix://$HOME/.lima/default/sock/docker.sock
cd /Users/pjayawardana3/Projects/airavata
mm activate airavata
mvn test -pl airavata-api "$@"
