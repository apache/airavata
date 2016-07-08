#!/usr/bin/env bash

#SBATCH -J remote_logger     # Job name
#SBATCH -o remote_logger.o%j # Name of stdout output file(%j expands to jobId) 
#SBATCH -e remote_logger.o%j # Name of stderr output file(%j expands to jobId)
#SBATCH -p development         # large queue for jobs > 256 nodes
#SBATCH -t 00:10:00      # Run time (hh:mm:ss) - 1.5 hours
#SBATCH -n 1             # Nodes to use

module use "/home1/03947/tg832463/modulefiles"
module load openmm

touch test.txt

python log_streamer.py test.txt &

while true; do
    bash generate_logs.sh
    sleep 5
done

