#!/bin/bash

TARGET_DIR="/home/jovyan/work"
SHARED_TMP="/cybershuttle_data"

mkdir -p "$TARGET_DIR"

if [ ! -f "$TARGET_DIR/.initialized" ]; then
    chown -R jovyan:users "$TARGET_DIR"

    # If $GIT_URL is set, clone the repo into the workspace
    if [ -n "$GIT_URL" ]; then
        echo "Cloning repo from $GIT_URL..."
        cd "$TARGET_DIR"
        git clone "$GIT_URL" .
        chown -R jovyan:users .
    fi
    touch "$TARGET_DIR/.initialized"
else
    echo "Docker default files already exist, skipping copy."
fi

if [ -d "$SHARED_TMP" ]; then
    echo "Linking shared data to workspace..."
    ln -s "$SHARED_TMP" "$TARGET_DIR/cybershuttle_data"
fi

exec "$@"
