#!/usr/bin/env bash

rm -rf base-airavata/apache-airavata-server-0.17-SNAPSHOT
cp ../../../../distribution/target/apache-airavata-server-0.17-SNAPSHOT-bin.tar.gz base-airavata/
tar -xvf base-airavata/apache-airavata-server-0.17-SNAPSHOT-bin.tar.gz -C base-airavata/
cp mysql-connector-java-5.1.39-bin.jar base-airavata/apache-airavata-server-0.17-SNAPSHOT/lib/
cp airavata-server.properties base-airavata/apache-airavata-server-0.17-SNAPSHOT/bin/
base-airavata/apache-airavata-server-0.17-SNAPSHOT/bin/airavata-server-start.sh all -d
