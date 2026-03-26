#!/bin/bash
set -e

# Start munge authentication
service munge start

# Start SSH daemon
service ssh start

# Initialize SLURM database
if [ ! -f /var/spool/slurmctld/state ]; then
    touch /var/spool/slurmctld/state
    chown slurm:slurm /var/spool/slurmctld/state
fi

# Start SLURM controller
slurmctld -D &

# Wait for controller to be ready
sleep 5

# Start SLURM daemon
slurmd -D &

# Keep container running
tail -f /var/log/slurm/*.log
