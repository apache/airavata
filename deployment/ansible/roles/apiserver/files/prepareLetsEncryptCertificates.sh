#!/bin/bash

umask 077

# Loop through all Let's Encrypt certificates
for CERTIFICATE in `find /etc/letsencrypt/live/* -type d`; do

  CERTIFICATE=`basename $CERTIFICATE`

  # Combine certificate and private key to single file
  mkdir -p /etc/ssl/$CERTIFICATE/
  cat /etc/letsencrypt/live/$CERTIFICATE/fullchain.pem /etc/letsencrypt/live/$CERTIFICATE/privkey.pem > /etc/ssl/$CERTIFICATE/$CERTIFICATE.pem

  chmod 640 /etc/ssl/$CERTIFICATE/$CERTIFICATE.pem
  chmod 750 /etc/ssl/$CERTIFICATE/
  chown root:haproxy /etc/ssl/$CERTIFICATE/$CERTIFICATE.pem
  chown root:haproxy /etc/ssl/$CERTIFICATE/

done
