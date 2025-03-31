#!/bin/bash

TARGET_DIR="/home/jovyan/work"

mkdir -p "$TARGET_DIR"


if [ ! -f "$TARGET_DIR/.initialized" ]; then
    echo "Copying default Docker files into the mounted workspace..."
    cp -r /tmp/default_data/. "$TARGET_DIR/"
    touch "$TARGET_DIR/.initialized"
    chown -R jovyan:users "$TARGET_DIR"
else
    echo "Docker default files already exist, skipping copy."
fi

exec "$@"
