# Set the base image to Centos
FROM centos:latest

# Install Modules
RUN yum -y update; yum clean all
#TODO: start firewalld
RUN yum install firewalld -y && \
	yum install git -y && \
	yum install maven -y && \
	yum install java-1.8.0-openjdk -y
