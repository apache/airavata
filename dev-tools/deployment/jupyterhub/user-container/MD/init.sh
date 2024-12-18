#!/bin/bash

TARGET_DIR="/home/jovyan/work"

if [ -z "$(ls -A $TARGET_DIR)" ]; then
    echo "Initializing user environment with default files..."
    cp -r /tmp/default_data "$TARGET_DIR/data"
    cp /tmp/default_configs/poc.ipynb "$TARGET_DIR/"
    cp /tmp/default_configs/settings.ini "$TARGET_DIR/"
    chown -R jovyan:users "$TARGET_DIR"
else
    echo "User environment already initialized."
fi

exec "$@"
