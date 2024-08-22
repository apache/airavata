#!/bin/sh -x

while getopts i:d: option
 do
  case $option in
    i ) AgentId=$OPTARG ;;
    d ) ServerUrl=$OPTARG ;;
     \? ) cat << ENDCAT1
>! Usage: $0  [-i Agent ID ]    !<
>!            [-d  Server URL ]      !<
ENDCAT1
#   exit 1 ;;
  esac
done

module load singularitypro
singularity exec /expanse/lustre/scratch/gridchem/temp_project/containers/airavata-agent.sif /opt/airavata-agent $ServerUrl:19900 $AgentId