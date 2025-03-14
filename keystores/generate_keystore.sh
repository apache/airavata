#!/bin/bash

# Remove existing key stores
rm -f airavata.jks client_truststore.jks

# Generate a PKCS12 keystore with a self-signed certificate
keytool -genkey -keyalg RSA -alias selfsigned -keystore airavata.jks -storetype pkcs12 -storepass airavata -validity 360 -keysize 2048 \
  -dname "CN=airavata.host, OU=airavata.host, O=airavata.host, L=airavata.host, ST=airavata.host, C=airavata.host" \
  -ext san=dns:airavata.host

# Also generate a JKS keystore with that certificate (for backward-compatibility)
keytool -importkeystore -noprompt \
  -srckeystore airavata.jks -srcstoretype pkcs12 -srcstorepass airavata \
  -destkeystore client_truststore.jks -deststoretype jks -deststorepass airavata
