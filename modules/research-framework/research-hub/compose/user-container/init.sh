#!/bin/bash

TARGET_DIR="/home/jovyan/work"
SHARED_TMP="/home/jovyan/shared_data_tmp"

mkdir -p "$TARGET_DIR"

if [ ! -f "$TARGET_DIR/.initialized" ]; then
    echo "Copying default Docker files into the mounted workspace..."
#    cp -r /tmp/default_data/. "$TARGET_DIR/"
    touch "$TARGET_DIR/.initialized"
    chown -R jovyan:users "$TARGET_DIR"

    # If $GIT_URL is set, clone the repo into the workspace
    if [ -n "$GIT_URL" ]; then
        echo "Cloning repo from $GIT_URL..."
        cd "$TARGET_DIR"
        git clone "$GIT_URL" repo
        chown -R jovyan:users "repo"
    fi
else
    echo "Docker default files already exist, skipping copy."
fi

# If there's a shared_data_tmp directory mounted, symlink them
if [ -d "$SHARED_TMP" ]; then
    echo "Creating symlinks for shared read-only data inside $TARGET_DIR..."
    for item in "$SHARED_TMP"/*; do
        ln -s "$item" "$TARGET_DIR/$(basename "$item")"
    done
fi

exec "$@"
