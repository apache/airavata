# Multi-arch SLURM test cluster
FROM --platform=$BUILDPLATFORM ubuntu:22.04

ARG TARGETPLATFORM
ARG BUILDPLATFORM

ENV DEBIAN_FRONTEND=noninteractive

# Install SLURM and dependencies
RUN apt-get update && apt-get install -y \
    slurm-wlm \
    slurm-wlm-basic-plugins \
    slurmctld \
    slurmd \
    openssh-server \
    sudo \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Create test user
RUN useradd -m -s /bin/bash testuser && \
    echo "testuser:testpass" | chpasswd && \
    echo "testuser ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

# Configure SSH
RUN mkdir -p /var/run/sshd && \
    ssh-keygen -A && \
    mkdir -p /home/testuser/.ssh && \
    chmod 700 /home/testuser/.ssh

# Copy SLURM configuration
COPY slurm.conf /etc/slurm/slurm.conf
RUN chmod 644 /etc/slurm/slurm.conf && \
    mkdir -p /var/spool/slurmctld && \
    mkdir -p /var/spool/slurmd && \
    chown -R slurm:slurm /var/spool/slurm* && \
    mkdir -p /var/log/slurm && \
    chown -R slurm:slurm /var/log/slurm

# Expose ports
EXPOSE 22 6817 6818

# Start services
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
