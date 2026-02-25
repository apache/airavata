# Multi-arch SFTP test server
FROM --platform=$BUILDPLATFORM ubuntu:22.04

ARG TARGETPLATFORM
ARG BUILDPLATFORM

ENV DEBIAN_FRONTEND=noninteractive

# Install OpenSSH server
RUN apt-get update && apt-get install -y \
    openssh-server \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Create test user
RUN useradd -m -s /bin/bash testuser && \
    echo "testuser:testpass" | chpasswd

# Configure SSH for SFTP
RUN mkdir -p /var/run/sshd && \
    ssh-keygen -A && \
    mkdir -p /home/testuser/.ssh && \
    mkdir -p /home/testuser/data && \
    chmod 700 /home/testuser/.ssh && \
    chmod 755 /home/testuser/data && \
    chown -R testuser:testuser /home/testuser

# Configure SSH daemon for SFTP
RUN sed -i 's/#Subsystem sftp.*/Subsystem sftp internal-sftp/' /etc/ssh/sshd_config && \
    echo "Port 22" >> /etc/ssh/sshd_config && \
    echo "PermitRootLogin no" >> /etc/ssh/sshd_config && \
    echo "PasswordAuthentication yes" >> /etc/ssh/sshd_config && \
    echo "PubkeyAuthentication yes" >> /etc/ssh/sshd_config && \
    echo "Match User testuser" >> /etc/ssh/sshd_config && \
    echo "    ChrootDirectory /home/testuser" >> /etc/ssh/sshd_config && \
    echo "    ForceCommand internal-sftp" >> /etc/ssh/sshd_config && \
    echo "    AllowTcpForwarding no" >> /etc/ssh/sshd_config && \
    echo "    X11Forwarding no" >> /etc/ssh/sshd_config

# Expose SSH port
EXPOSE 22

# Start SSH daemon
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
