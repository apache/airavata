#!/bin/bash
set -e

for f in /etc/slurm/slurm.conf /etc/supervisor/conf.d/supervisord.conf /etc/munge/munge.key.ro; do
    if [ ! -r "$f" ]; then
        echo "ERROR: Required file $f not found or not readable"
        exit 1
    fi
done

echo "Starting supervisord..."
/usr/bin/supervisord -c /etc/supervisor/conf.d/supervisord.conf &
sleep 10

echo "Waiting for munge to be ready..."
install -o munge -g munge -m 400 /etc/munge/munge.key.ro /etc/munge/munge.key
for i in {1..30}; do
    if munge -n | unmunge | grep -q "STATUS:.*Success"; then
        echo "Munge authentication successful"
        break
    fi
    echo "Waiting for munge authentication... (attempt $i/30)"
    sleep 2
done

if ! munge -n | unmunge | grep -q "STATUS:.*Success"; then
    echo "Munge authentication failed after 60 seconds"
    exit 1
fi

if [[ "$HOSTNAME" == slurmctl* ]]; then
    echo "Starting SLURM controller..."
    /usr/sbin/slurmctld -D &
    sleep 5
    echo "SLURM controller started"
else
    echo "Waiting for SLURM controller to be ready..."
    CONTROL_HOST=$(grep "^ControlMachine" /etc/slurm/slurm.conf | sed 's/ControlMachine=//')
    echo "Waiting for controller: $CONTROL_HOST"
    while ! nc -z "$CONTROL_HOST" 6817; do
        echo "Waiting for $CONTROL_HOST:6817..."
        sleep 2
    done
    echo "SLURM controller is ready, starting slurmd..."
    /usr/sbin/slurmd -D &
    sleep 2
    echo "SLURM daemon started"
fi

wait
