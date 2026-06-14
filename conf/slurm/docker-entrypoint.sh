#!/usr/bin/env bash
# Entrypoint for the Airavata devstack SLURM image. The first arg selects the
# role: slurmdbd | slurmctld | slurmd | login. The same image runs every role.
#
# Idempotent + restartable: safe to run repeatedly. munge key, DB registration,
# and ssh host keys are all created only if missing.
set -euo pipefail

log() { echo "[entrypoint $(date +%H:%M:%S)] $*"; }

# --- munge: shared-secret auth across all SLURM daemons --------------------
# The munge key is shared via the slurm_munge volume mounted at /etc/munge.
# The first container to start generates it; the rest reuse it. We must never
# regenerate it once jobs/daemons are running, so generate only if absent.
setup_munge() {
  install -d -o munge -g munge -m 0700 /etc/munge /var/log/munge /var/lib/munge
  install -d -o munge -g munge -m 0755 /run/munge
  if [ ! -s /etc/munge/munge.key ]; then
    log "generating munge.key"
    # /dev/urandom keying avoids the mungekey/haveged entropy dependency.
    dd if=/dev/urandom bs=1 count=1024 of=/etc/munge/munge.key 2>/dev/null
  fi
  chown munge:munge /etc/munge/munge.key
  chmod 0400 /etc/munge/munge.key
  log "starting munged"
  gosu munge /usr/sbin/munged --force
  # brief readiness wait
  for _ in $(seq 1 10); do munge -n >/dev/null 2>&1 && break; sleep 0.3; done
}

wait_for_tcp() {
  local host=$1 port=$2 name=${3:-$1:$2}
  log "waiting for $name ..."
  until bash -c ">/dev/tcp/$host/$port" 2>/dev/null; do sleep 1; done
  log "$name is up"
}

# slurmctld resolves each node's NodeAddr exactly once, when it reads slurm.conf at
# startup. In this compose stack the controller can come up before the compute
# containers are DNS-resolvable (or while a prior run's stale container IPs are still
# cached), so it can latch onto the wrong address for a node and keep it for its whole
# lifetime. Batch-job launches are then delivered to the wrong slurmd, which rejects
# them ("Host cX not in hostlist cY"); slurmctld reads that as a node failure and
# requeues the job until it is held (JobHoldMaxRequeue) — so jobs never actually run.
# Once the compute nodes are up, a single `scontrol reconfigure` re-resolves every
# NodeAddr against current DNS and corrects the routing. Runs in the background since
# slurmctld is exec'd in the foreground.
reresolve_node_addrs() {
  # Wait until our own controller is accepting connections.
  until scontrol ping >/dev/null 2>&1; do sleep 1; done
  # Wait until every configured compute node resolves in DNS (its container is up).
  local node
  for node in $(grep -oE '^NodeName=[^[:space:]]+' /etc/slurm/slurm.conf | sed 's/NodeName=//' | grep -vx 'DEFAULT'); do
    until getent hosts "$node" >/dev/null 2>&1; do sleep 1; done
  done
  # Let DNS settle, then force a fresh resolve of every NodeAddr.
  sleep 2
  if scontrol reconfigure >/dev/null 2>&1; then
    log "re-resolved compute NodeAddrs (scontrol reconfigure) once nodes were up"
  fi
}

# --- ssh login/submit node setup -------------------------------------------
# Configures sshd so the airavata-server can ssh in as `airavata` (using the
# devstack id_rsa keypair) to run sbatch/squeue/sacct.
setup_sshd() {
  # Generate host keys on first boot (persisted nowhere — fine for devstack).
  ssh-keygen -A >/dev/null 2>&1 || true
  install -d -o airavata -g airavata -m 0700 /home/airavata/.ssh
  # authorized_keys is mounted read-only at /etc/airavata/authorized_keys; copy
  # it into place with the right ownership/mode (sshd rejects loose perms).
  if [ -f /etc/airavata/authorized_keys ]; then
    cp /etc/airavata/authorized_keys /home/airavata/.ssh/authorized_keys
    chown airavata:airavata /home/airavata/.ssh/authorized_keys
    chmod 0600 /home/airavata/.ssh/authorized_keys
  fi
  # Key-only login for airavata; no passwords.
  sed -i 's/^#\?PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config
  sed -i 's/^#\?PubkeyAuthentication.*/PubkeyAuthentication yes/'     /etc/ssh/sshd_config
  log "starting sshd"
  /usr/sbin/sshd
}

ROLE=${1:-slurmctld}
log "role=$ROLE"

case "$ROLE" in

  slurmdbd)
    setup_munge
    # slurmdbd needs its accounting DB reachable before it will start.
    wait_for_tcp slurm-dbmysql 3306 "accounting mysql"
    # Wait until the DB actually accepts our credentials (init may still run).
    until mariadb -h slurm-dbmysql -uslurm -pslurm -e 'SELECT 1' slurm_acct_db >/dev/null 2>&1; do
      log "waiting for slurm_acct_db to accept connections ..."; sleep 2
    done
    log "starting slurmdbd (foreground)"
    exec gosu slurm /usr/sbin/slurmdbd -D
    ;;

  slurmctld)
    setup_munge
    wait_for_tcp slurmdbd 6819 "slurmdbd"
    # Register the cluster in the accounting DB so sacct has somewhere to record
    # jobs. sacctmgr is idempotent (-i = no prompt); ignore "already exists".
    log "registering cluster 'airavata' in accounting db"
    sacctmgr -i add cluster airavata >/dev/null 2>&1 || true
    # Stand up an account + association so submitted jobs are accounted.
    sacctmgr -i add account airavata Description="airavata devstack" >/dev/null 2>&1 || true
    sacctmgr -i add user airavata account=airavata >/dev/null 2>&1 || true
    # slurmctld also doubles as the ssh login/submit node (network alias `slurm`).
    setup_sshd
    # Self-heal the startup NodeAddr-resolution race once the compute nodes are up.
    reresolve_node_addrs &
    log "starting slurmctld (foreground)"
    exec gosu slurm /usr/sbin/slurmctld -D
    ;;

  slurmd)
    setup_munge
    wait_for_tcp slurmctld 6817 "slurmctld"
    # Compute nodes also run sshd so jobs/files behave like a normal cluster node
    # and the shared scratch is reachable the same way everywhere.
    setup_sshd
    log "starting slurmd (foreground) as $(hostname)"
    # -N pins the registered node name to the container hostname (c1/c2).
    exec /usr/sbin/slurmd -D -N "$(hostname)"
    ;;

  login)
    # Standalone login/submit node (only used if you split it off slurmctld).
    setup_munge
    wait_for_tcp slurmctld 6817 "slurmctld"
    setup_sshd
    log "login node ready; tailing to stay alive"
    exec tail -f /dev/null
    ;;

  *)
    log "unknown role '$ROLE'; exec-ing as-is"
    exec "$@"
    ;;
esac
