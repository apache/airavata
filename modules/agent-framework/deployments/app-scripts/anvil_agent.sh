#!/bin/bash -x

# #####################################################################
# Standalone Airavata Agent
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
while getopts a:s:p: option; do
  case $option in
  a) AGENT_ID=$OPTARG ;;
  s) SERVER_URL=$OPTARG ;;
  p) PROCESS_ID=$OPTARG ;;
  \?) cat <<ENDCAT ;;
>! Usage: $0  [-a AGENT_ID ]    !<
>!            [-s SERVER_URL]   !<
>!            [-p PROCESS_ID]   !<
ENDCAT
  esac
done

echo "AGENT_ID=$AGENT_ID"
echo "SERVER_URL=$SERVER_URL"
echo "PROCESS_ID=$PROCESS_ID"

# ----------------------------------------------------------------------
# STEP 2 - RUN AGENT
# ----------------------------------------------------------------------
SCRATCH_DIR="/home/x-scigap/scratch/cs_workdirs"
SIF_PATH="/home/x-scigap/agent-framework/container/airavata-agent.sif"
singularity exec --bind $SCRATCH_DIR:/scratch $SIF_PATH \
  /opt/airavata-agent "$SERVER_URL":19900 "$AGENT_ID"