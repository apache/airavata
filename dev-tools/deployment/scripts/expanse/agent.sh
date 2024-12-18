#!/bin/bash -x

# #####################################################################
# Standalone Airavata Agent for Expanse
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
>!            [-w PROCESS_ID]     !<
ENDCAT
  esac
done

echo "AGENT_ID=$AGENT_ID"
echo "SERVER_URL=$SERVER_URL"
echo "PROCESS_ID=$PROCESS_ID"

# ----------------------------------------------------------------------
# STEP 2 - RUN AGENT
# ----------------------------------------------------------------------
SIF_PATH=/home/scigap/agent-framework/airavata-agent.sif
module load singularitypro
singularity exec --bind /expanse/lustre/scratch/scigap/temp_project/neuro-workdirs/$PROCESS_ID:/data $SIF_PATH bash -c "/opt/airavata-agent $SERVER_URL:19900 $AGENT_ID"
