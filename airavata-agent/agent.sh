#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

# #####################################################################
# Airavata Agent Bootstrapper
# #####################################################################
#
# ----------------------------------------------------------------------
# CONTRIBUTORS
# ----------------------------------------------------------------------
# * Dimuthu Wannipurage
# * Lahiru Jayathilake
# * Yasith Jayawardana
# ######################################################################

#-----------------------------------------------------------------------
# STEP 1 - PARSE COMMAND LINE ARGS
#-----------------------------------------------------------------------

# Default values
CS_HOME=$HOME/cybershuttle
AGENT=""
SERVER=""
LIBRARIES=""
ENVIRON=""
PIP=""
MOUNTS=()

# setup application directory
ln -s $CS_HOME/application $PWD/application

# parse command line args
PARSED_OPTIONS=$(getopt -o '' --long server:,agent:,libraries:,pip:,mounts:,environ: -n "$0" -- "$@")
if [ $? -ne 0 ]; then
    echo "Usage: $0 \
    --server SERVER \
    --agent AGENT \
    --libraries LIBRARIES \
    --pip PIP \
    --mounts MOUNTS \
    --environ ENVIRON"
    exit 1
fi
eval set -- "$PARSED_OPTIONS"
while true; do
    case "$1" in
        --server)    SERVER="$2";  shift 2 ;;
        --agent)     AGENT="$2";   shift 2 ;;
        --libraries) LIBRARIES="$2"; shift 2 ;;
        --pip)       PIP="$2"; shift 2 ;;
        --mounts)    
            IFS=',' read -ra MOUNTS <<< "$2"
            for MOUNT in "${MOUNTS[@]}"; do
              IFS=':' read -r SRC DEST <<< "$MOUNT"
              mkdir -p "$(dirname ".$DEST")"
              ln -s "$CS_HOME/dataset/$SRC" ".$DEST"
            done
            shift 2 ;;
        --environ)   ENVIRON="$2"; shift 2 ;;
        --)          shift; break ;;
        *) echo "Unexpected option: $1"; exit 1 ;;
    esac
done

# Final values
echo "CS_HOME=$CS_HOME"
echo "AGENT=$AGENT"
echo "SERVER=$SERVER"
echo "LIBRARIES=$LIBRARIES"
echo "PIP=$PIP"
echo "MOUNTS=${MOUNTS[@]}"

# ----------------------------------------------------------------------
# STEP 2 - RUN USING AGENT
# ----------------------------------------------------------------------

# initialize scratch/tmp and scratch/envs (node-local)
CS_TEMP=$(readlink $CS_HOME/scratch/tmp)
CS_ENVS=$(readlink $CS_HOME/scratch/envs)
[ -n "$CS_TEMP" ] && mkdir -p $CS_TEMP
[ -n "$CS_ENVS" ] && mkdir -p $CS_ENVS

# cleanup old envs and workspaces (node-local)
find $CS_ENVS -mindepth 1 -maxdepth 1 -type d -ctime +3 | xargs -n1 -I{} rm -rf {}
find $CS_HOME/workspace -mindepth 1 -maxdepth 1 -type d -ctime +3 | xargs -n1 -I{} rm -rf {}

# fetch binaries
wget -q https://github.com/cyber-shuttle/binaries/releases/download/1.0.1/airavata-agent-linux-amd64 -O airavata-agent
wget -q https://github.com/cyber-shuttle/binaries/releases/download/1.0.1/kernel.py -O kernel.py
wget -q https://github.com/mamba-org/micromamba-releases/releases/download/2.3.0-1/micromamba-linux-64 -O micromamba

chmod +x airavata-agent micromamba

# sync data
rsync -av --delete ubuntu@hub.cybershuttle.org:~/mnt/ $CS_HOME/dataset

# define environment variables
export MAMBA_ROOT_PREFIX=$CS_HOME/scratch
export TMPDIR=$CS_HOME/scratch/tmp
export PATH=$PWD:$PATH

# run agent
airavata-agent --server "$SERVER:19900" --agent "$AGENT" --environ "$ENVIRON" --lib "$LIBRARIES" --pip "$PIP"
