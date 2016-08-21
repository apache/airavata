#
# To build from the airavata root dir until jenkins script or maven build to create docker image
#
FROM scigap/java:8
ARG COMPONENT="all"

ENV COMPONENT=$COMPONENT

RUN mkdir /airavata
COPY . /airavata

WORKDIR /airavata

RUN chmod +x ./bin/airavata-server-start.sh

ENTRYPOINT ./bin/airavata-server-start.sh $COMPONENT
