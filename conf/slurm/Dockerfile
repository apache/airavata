# A self-contained SLURM cluster image for the Airavata devstack.
#
# One image, many roles: the same image runs slurmctld, slurmd, slurmdbd and an
# sshd login/submit node — the role is selected by the first argument to
# docker-entrypoint.sh (see the `command:` of each compose service).
#
# We install SLURM from Ubuntu's archive rather than compiling from a SchedMD
# source tarball: the packaged build is pinned, reproducible, and fast, and it
# ships matched slurmctld/slurmd/slurmdbd/slurm-client + munge binaries.
FROM ubuntu:24.04

# SLURM upstream version shipped by Ubuntu 24.04 (noble) universe. Pinned for
# reproducibility; the trailing `-*` tolerates Ubuntu security-revision bumps
# (e.g. 23.11.4-1.2ubuntu5 -> ...ubuntu6) without breaking the build, while
# keeping the SLURM feature version fixed. `slurmctld -V` reports 23.11.4.
ARG SLURM_VERSION=23.11.4-*

ENV DEBIAN_FRONTEND=noninteractive

# - slurmctld / slurmd / slurmdbd / slurm-client: the SLURM daemons + client CLIs
#   (sbatch, squeue, sacct, scontrol, scancel, sinfo).
# - munge: shared-secret auth between every SLURM daemon.
# - openssh-server: the login/submit node sshd.
# - gosu: drop privileges from the root entrypoint to the slurm/airavata users.
# - mariadb-client: used by the entrypoint to wait for the accounting DB.
RUN apt-get update && apt-get install -y --no-install-recommends \
        ca-certificates \
        tzdata \
        gosu \
        munge \
        openssh-server \
        mariadb-client \
        bash coreutils procps iproute2 \
        slurm-client=${SLURM_VERSION} \
        slurmctld=${SLURM_VERSION} \
        slurmd=${SLURM_VERSION} \
        slurmdbd=${SLURM_VERSION} \
    && rm -rf /var/lib/apt/lists/*

# The Ubuntu packages create the `slurm` and `munge` system users. Create the
# `airavata` login user (uid 1000) that jobs run as and that ssh authenticates;
# matching the sftp service's airavata/uid-1000 keeps file ownership consistent
# across the shared scratch volume.
RUN userdel -r ubuntu 2>/dev/null || true; \
    groupadd -g 1000 airavata && \
    useradd  -u 1000 -g 1000 -m -d /home/airavata -s /bin/bash airavata

# Runtime + spool dirs SLURM expects (the deb ships some, but be explicit/idempotent).
RUN install -d -o slurm -g slurm /var/spool/slurmctld /var/spool/slurmd /var/log/slurm /var/run/slurm && \
    install -d -m 0755 /var/run/sshd

COPY slurm.conf       /etc/slurm/slurm.conf
COPY cgroup.conf      /etc/slurm/cgroup.conf
COPY slurmdbd.conf    /etc/slurm/slurmdbd.conf
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

# slurmdbd refuses to start unless slurmdbd.conf is 0600 and owned by SlurmUser.
RUN chown slurm:slurm /etc/slurm/slurmdbd.conf && chmod 0600 /etc/slurm/slurmdbd.conf && \
    chmod 0755 /usr/local/bin/docker-entrypoint.sh

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["slurmctld"]
