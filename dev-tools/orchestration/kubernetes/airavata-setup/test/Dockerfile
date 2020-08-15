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
