#!/bin/bash

# generate AES-256 key for credential encryption
keytool -genseckey -alias airavata -keyalg AES -keysize 256 -keystore aes.p12 -storepass airavata

# generate self-signed key-cert pair for SSL termination
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout server.key -out server.crt \
  -subj "/CN=airavata.host/OU=airavata.host/O=airavata.host/L=airavata.host/ST=airavata.host/C=airavata.host" \
  -addext "subjectAltName=DNS:airavata.host"

# add server.crt to java truststore
sudo keytool -cacerts -storepass changeit -delete -alias airavata
sudo keytool -cacerts -storepass changeit -importcert -alias airavata -file server.crt -trustcacerts -noprompt

# generate airavata.p12
rm -rf airavata.p12
# if self-signed {server.crt, server.key}
openssl pkcs12 -export -name tls -out airavata.p12 -passout pass:airavata -in server.crt -inkey server.key
# if letsencrypt {fullchain.pem, privkey.pem}
openssl pkcs12 -export -name tls -out airavata.p12 -passout pass:airavata -in fullchain.pem -inkey privkey.pem
# add AES key to store
keytool -importkeystore -srckeystore aes.p12 -destkeystore airavata.p12 -srcstorepass airavata -deststorepass airavata
