############################################################
# Dockerfile to build Gateway Microservice
# Based on Centos
############################################################
# Set the base image to Centos
FROM centos:latest

# File Author / Maintainer
MAINTAINER Jeffrey Ravindran <jeffravi@iu.edu>

# Setup the user
RUN useradd -ms /bin/bash airavata # check group: `cut -d: -f1 /etc/group`

#Work directory - TODO:: Move to config
#WORKDIR /usr/local/src

# Install Modules
RUN yum -y update; yum clean all
#TODO: start firewalld
RUN yum install firewalld -y && \
	yum install git -y && \
	yum install maven -y && \
	yum install java-1.8.0-openjdk -y
	
#Checkout master branch
RUN git clone -b master https://git-wip-us.apache.org/repos/asf/airavata.git

#Building the project using Maven
RUN cd /airavata && \
	mvn clean install -Dmaven.test.skip=true