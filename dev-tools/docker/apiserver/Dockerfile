############################################################
# Dockerfile to build Gateway Microservice
# Based on Centos
############################################################
# Set the base image to Centos
FROM airavata/base:latest

#Setting WorkDirectory
ARG WORK_DIR="/home/airavata"
ENV WORK_DIR $WORK_DIR
ARG BUILD_FILE="apache-airavata-server-0.17-SNAPSHOT-bin.tar.gz"
ARG COMPONENT="apiserver"

#Starting apiserver
CMD sh $WORK_DIR/apache-airavata-server-0.17-SNAPSHOT/bin/airavata-server-start.sh $COMPONENT