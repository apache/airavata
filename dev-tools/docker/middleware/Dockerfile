############################################################
# Dockerfile to build Gateway Microservice
# Based on Ubuntu
############################################################
# Set the base image to Ubuntu
FROM ubuntu:latest

# File Author / Maintainer
MAINTAINER Jeffrey Ravindran <jeffravi@iu.edu>

# Install Modules
RUN apt-get update 
RUN apt-get install -y git nodejs npm

WORKDIR /home

COPY startup.sh /home/startup.sh

RUN chmod 700 /home/startup.sh

# Checking out Code base
RUN git clone https://github.com/airavata-courses/jeffreypeter.git

# Checking dependencies
RUN cd /home/jeffreypeter/assignment/gateway && npm -g install

# Starting the Server
CMD /home/startup.sh
