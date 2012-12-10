#!/bin/sh
pid=$(ps -fe | grep '[d]erby' | awk '{print $2}')
if [[ -n $pid ]]; then
    kill -TERM $pid
else
    echo "Does not exist"
fi
