# SLURM cluster 1 node for testing (controller + compute)
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# Install packages and create directories
RUN apt-get update && apt-get install -y slurm-wlm slurm-client munge supervisor netcat curl openssh-server \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /var/spool/slurm/{ctld,d,accounting,checkpoint,archive} /var/log/slurm/accounting /etc/slurm /etc/munge /var/run/slurm /run/munge /var/log/munge /var/lib/munge /var/run/sshd

# Create testuser and configure SSH
RUN useradd -m -s /bin/bash testuser \
    && mkdir -p /home/testuser/.ssh \
    && chown -R testuser:testuser /home/testuser \
    && chmod 700 /home/testuser/.ssh \
    && echo -e 'Port 22\nPermitRootLogin yes\nPasswordAuthentication yes\nPubkeyAuthentication yes\nAuthorizedKeysFile .ssh/authorized_keys\nStrictModes no' >> /etc/ssh/sshd_config

# Set permissions
RUN chown -R slurm:slurm /var/spool/slurm /var/log/slurm /var/run/slurm \
    && chown -R munge:munge /var/lib/munge /var/log/munge /run/munge \
    && chmod 700 /var/lib/munge 755 /run/munge

# Copy startup script
COPY start.sh /start.sh
RUN chmod +x /start.sh

EXPOSE 22 6817 6818
CMD ["/start.sh"]