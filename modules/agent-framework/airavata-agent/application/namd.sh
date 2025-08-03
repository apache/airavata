#!/bin/bash -x
set -euo pipefail

# ----------------------------------------------------------------------
# SETUP
# ----------------------------------------------------------------------
export PATH=$PWD:$PATH
export WORKDIR=$PWD
export CS_HOME=$HOME/cybershuttle
export MAMBA_ROOT_PREFIX=$CS_HOME/scratch
export TMPDIR=$CS_HOME/scratch/tmp

# initialize scratch/tmp and scratch/envs (node-local)
CS_TEMP=$(readlink $CS_HOME/scratch/tmp)
CS_ENVS=$(readlink $CS_HOME/scratch/envs)
[ -n "$CS_TEMP" ] && mkdir -p $CS_TEMP
[ -n "$CS_ENVS" ] && mkdir -p $CS_ENVS
NAMD_EXTRA_ARGS=()
FIFO=$(mktemp -u)
mkfifo $FIFO

# ----------------------------------------------------------------------
# PARSE COMMAND LINE ARGUMENTS
# ----------------------------------------------------------------------

required_vars=("NAMD_CPU_PATH" "NAMD_GPU_PATH" "NAMD_CPU_MODULES" "NAMD_GPU_MODULES")
for var in "${required_vars[@]}"; do
  if [ -z "${!var}" ]; then
    echo "$var is not set"
    exit 2
  fi
done

while getopts t:n:i:a:s: option; do
  case $option in
    t) 
      if [[ "$OPTARG" != "CPU" && "$OPTARG" != "GPU" ]]; then
        echo "invalid argument -t $OPTARG: must be CPU|GPU."
        exit 2
      fi
      EXECUTION_TYPE=$OPTARG
      echo "EXECUTION_TYPE=$EXECUTION_TYPE"
      module reset
      if [ $EXECUTION_TYPE = "CPU" ]; then
        # one replica at a time
        echo 0 > $FIFO &
        NAMD_PATH=$NAMD_CPU_PATH
        module load $NAMD_CPU_MODULES
      elif [ $EXECUTION_TYPE = "GPU" ]; then
        # one replica per GPU
        for ((i=0; i<${SLURM_GPUS_ON_NODE:-0}; i++)); do echo "$i" > $FIFO & done
        NAMD_PATH=$NAMD_GPU_PATH
        NAMD_EXTRA_ARGS+=("--CUDASOAintegrate" "on")
        module load $NAMD_GPU_MODULES
      fi
      module list
      ;;
    n) 
      NUM_REPLICAS=$OPTARG
      echo "NUM_REPLICAS=$NUM_REPLICAS"
      ;;
    i) 
      NAMD_INPUT_FILES=$(find $WORKDIR -maxdepth 1 -type f ! -name "*slurm*" ! -name "*.stdout" ! -name "*.stderr")
      NAMD_CONF_FILE=$OPTARG
      echo "NAMD_INPUT_FILES=$NAMD_INPUT_FILES"
      echo "NAMD_CONF_FILE=$NAMD_CONF_FILE"
      ;;
    a) 
      AGENT_ID=$OPTARG
      echo "AGENT_ID=$AGENT_ID"
      ;;
    s) 
      AGENT_SERVER=$OPTARG
      echo "AGENT_SERVER=$AGENT_SERVER"
      ;;
    \?) 
      echo 1>&2 "Usage: $0"
      echo 1>&2 "  -t [CPU|GPU]"
      echo 1>&2 "  -n [NUM_REPLICAS]"
      echo 1>&2 "  -i [NAMD_CONF_FILE]"
      echo 1>&2 "  -a [AGENT_ID]"
      echo 1>&2 "  -s [AGENT_SERVER]"
      exit 2
      ;;
  esac
done
shift $((OPTIND - 1))

# ----------------------------------------------------------------------
# RUN AGENT
# ----------------------------------------------------------------------

wget -q https://github.com/cyber-shuttle/binaries/releases/download/1.0.1/airavata-agent-linux-amd64 -O $WORKDIR/airavata-agent
wget -q https://github.com/cyber-shuttle/binaries/releases/download/1.0.1/kernel.py -O $WORKDIR/kernel.py
wget -q https://github.com/mamba-org/micromamba-releases/releases/download/2.3.0-1/micromamba-linux-64 -O $WORKDIR/micromamba
chmod +x $WORKDIR/airavata-agent $WORKDIR/micromamba
$WORKDIR/airavata-agent --server "$AGENT_SERVER:19900" --agent "$AGENT_ID" --environ "$AGENT_ID" --lib "" --pip "" &
AGENT_PID=$!
trap 'kill -TERM $AGENT_PID' EXIT
echo "Agent started with PID $AGENT_PID"


# ----------------------------------------------------------------------
# RUN NAMD3
# ----------------------------------------------------------------------
PIDS=()
for REPLICA_ID in $(seq 1 $NUM_REPLICAS); do
  (
  read TOKEN <$FIFO

  REPLICA_DIR=$WORKDIR/$REPLICA_ID
  mkdir $REPLICA_DIR
  cp $NAMD_INPUT_FILES $REPLICA_DIR/

  [[ $EXECUTION_TYPE == "GPU" ]] && export CUDA_VISIBLE_DEVICES=$TOKEN
  $NAMD_PATH/namd3 +setcpuaffinity +p $SLURM_CPUS_ON_NODE --cwd $REPLICA_DIR "${NAMD_EXTRA_ARGS[@]}" \
  $REPLICA_DIR/$NAMD_CONF_FILE >$REPLICA_DIR/$NAMD_CONF_FILE.out 2>$REPLICA_DIR/$NAMD_CONF_FILE.err
  [[ $EXECUTION_TYPE == "GPU" ]] && unset CUDA_VISIBLE_DEVICES
  
  echo $TOKEN > $FIFO &

  for FILE in $(ls $REPLICA_DIR/*.*); do
    mv $FILE $REPLICA_ID"_"$(basename $FILE)
  done
  rm -rf $REPLICA_DIR/

  ) &
  PIDS+=($!)
done
wait "${PIDS[@]}"
