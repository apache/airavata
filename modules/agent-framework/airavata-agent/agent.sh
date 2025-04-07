#!/bin/bash

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
CONTAINER=""
LIBRARIES=""
PIP=""
BIND_OPTS=()

PARSED_OPTIONS=$(getopt -o '' --long server:,agent:,container:,libraries:,pip:,mounts:,bind: -n "$0" -- "$@")
if [ $? -ne 0 ]; then
    echo "Usage: $0 \
    --server SERVER \
    --agent AGENT \
    --container CONTAINER \
    --libraries LIBRARIES \
    --pip PIP \
    --mounts MOUNTS \
    [--bind BIND] ..."
    exit 1
fi
eval set -- "$PARSED_OPTIONS"

while true; do
    case "$1" in
        --server)    SERVER="$2";  shift 2 ;;
        --agent)     AGENT="$2";   shift 2 ;;
        --container) CONTAINER="$2"; shift 2 ;;
        --libraries) LIBRARIES="$2"; shift 2 ;;
        --pip)       PIP="$2"; shift 2 ;;
        --mounts)    
            IFS=',' read -ra MOUNTS <<< "$2"
            for MOUNT in "${MOUNTS[@]}"; do
              BIND_OPTS+=("--bind $(eval echo $MOUNT):ro")
            done
            shift 2 ;;
        --bind)      BIND_OPTS+=("--bind $2:ro"); shift 2 ;;
        --)          shift; break ;;
        *) echo "Unexpected option: $1"; exit 1 ;;
    esac
done

# Final values
echo "CS_HOME=$CS_HOME"
echo "AGENT=$AGENT"
echo "SERVER=$SERVER"
echo "CONTAINER=$CONTAINER"
echo "LIBRARIES=$LIBRARIES"
echo "PIP=$PIP"
echo "BIND_OPTS=$BIND_OPTS"

# ----------------------------------------------------------------------
# STEP 2 - RUN AGENT
# ----------------------------------------------------------------------
# Create overlay image (4GB) if it doesn't exist
if [ ! -f workspace.img ]; then
  singularity overlay create --size 4096 workspace.img
fi
# Start agent with the given params and overlay
KERNEL_SOCK=$(mktemp) singularity exec \
  --overlay workspace.img \
  ${BIND_OPTS[@]} \
  $CS_HOME/container/$CONTAINER /opt/airavata-agent \
    --server $SERVER:19900 \
    --agent $AGENT \
    --lib $LIBRARIES \
    --pip $PIP \
    $@
