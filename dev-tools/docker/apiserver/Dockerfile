############################################################
# Dockerfile to build Gateway Microservice
############################################################

# Set the base image to Airavtabase
FROM airavata/base:latest

#Setting WorkDirectory
ARG WORK_DIR="/home/airavata"
ENV WORK_DIR $WORK_DIR
ARG BUILD_FILE="apache-airavata-server-0.17-SNAPSHOT-bin.tar.gz"

#Checkout develop branch
WORKDIR $WORK_DIR
RUN git clone -b develop https://git-wip-us.apache.org/repos/asf/airavata.git

#Building the project using Maven
RUN cd $WORK_DIR/airavata && \
	mvn clean install -Dmaven.test.skip=true

#Extracting the Build
RUN cp $WORK_DIR/airavata/modules/distribution/target/$BUILD_FILE $WORK_DIR && \
	tar xvzf $BUILD_FILE

#Setting WorkDirectory
ARG COMPONENT="apiserver"
ENV COMPONENT $COMPONENT

#Starting apiserver
CMD sh $WORK_DIR/apache-airavata-server-0.17-SNAPSHOT/bin/airavata-server-start.sh $COMPONENT