# Set the base image to Airavtabase

FROM centos:latest

# Install Modules
RUN yum -y update; yum clean all
#TODO: start firewalld
RUN yum install firewalld -y && \
	yum install git -y && \
	yum install maven -y && \
	yum install java-1.8.0-openjdk -y

# Setup the user
RUN useradd -ms /bin/bash airavata # check group: `cut -d: -f1 /etc/group`

RUN sleep 10

#Setting WorkDirectory
ARG WORK_DIR="/home/airavata"
ENV WORK_DIR $WORK_DIR
ARG BUILD_FILE="apache-airavata-server-0.17-SNAPSHOT-bin.tar.gz"

#Checkout develop branch
WORKDIR $WORK_DIR
RUN git clone -b develop https://github.com/apache/airavata.git

#Building the project using Maven
RUN cd $WORK_DIR/airavata && \
	mvn clean install -Dmaven.test.skip=true

#Extracting the Build
RUN cp $WORK_DIR/airavata/modules/distribution/target/$BUILD_FILE $WORK_DIR && \
	tar xvzf $BUILD_FILE

#Setting WorkDirectory
ARG COMPONENT="apiserver"
ENV COMPONENT $COMPONENT

#Setting Property file
COPY airavata-server.properties $WORK_DIR/apache-airavata-server-0.17-SNAPSHOT/bin/

#Starting apiserver
CMD sh $WORK_DIR/apache-airavata-server-0.17-SNAPSHOT/bin/airavata-server-start.sh $COMPONENT
