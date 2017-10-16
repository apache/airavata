############################################################
# Dockerfile to build Gateway Microservice
# Based on Centos
############################################################
# Set the base image to Centos
FROM centos:latest

# File Author / Maintainer
MAINTAINER Jeffrey Ravindran <jeffravi@iu.edu>

# Install Modules
RUN yum -y update; yum clean all
#TODO: start firewalld
RUN yum install firewalld -y && \
	yum install git -y && \
	yum install maven -y && \
	yum install java-1.8.0-openjdk -y

# Setup the user
RUN useradd -ms /bin/bash airavata # check group: `cut -d: -f1 /etc/group`

#Setting WorkDirectory
ARG WORK_DIR="/home/airavata"
ENV WORK_DIR $WORK_DIR

#Checkout develop branch
WORKDIR $WORK_DIR
RUN git clone -b develop https://git-wip-us.apache.org/repos/asf/airavata.git

#Building the project using Maven
RUN cd $WORK_DIR/airavata && \
	mvn clean install -Dmaven.test.skip=true