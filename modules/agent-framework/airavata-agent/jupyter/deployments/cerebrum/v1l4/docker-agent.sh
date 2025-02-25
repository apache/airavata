#!/bin/bash

if [ ! -d "/tmp/v1l4" ]; then
    mkdir -p /tmp/v1l4
fi

cp -r /opt/v1l4/* /tmp/v1l4/

/opt/airavata-agent scigap02.sciencegateways.iu.edu:19900 agent2

