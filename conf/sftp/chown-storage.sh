#!/bin/sh
# atmoz/sftp runs /etc/sftp.d/*.sh as root on container startup, before sshd
# drops to the chrooted user. The named volume mounted at /home/airavata/storage
# is created root-owned by Docker, so the chrooted "airavata" user (uid 1000)
# cannot create directories or write files there. Chown it on every startup so
# the storage-service's SFTP adaptor can upload.
chown -R airavata:airavata /home/airavata/storage
