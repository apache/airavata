FROM avtbase

ARG COMPONENT="all"
ENV COMPONENT $COMPONENT
RUN git clone https://github.com/apache/airavata.git -b develop

WORKDIR "/airavata"

RUN mvn clean install -Dmaven.test.skip=true

WORKDIR "/"
RUN cp /airavata/distribution/target/apache-airavata-server-0.17-SNAPSHOT-bin.tar.gz ./

RUN tar xvzf apache-airavata-server-0.17-SNAPSHOT-bin.tar.gz

ADD airavata-server.properties /apache-airavata-server-0.17-SNAPSHOT/bin/
ADD mysql-connector-java-5.1.44-bin.jar /apache-airavata-server-0.17-SNAPSHOT/lib/

ADD airavata_sym.jks /airavata_sym.jks

CMD sh apache-airavata-server-0.17-SNAPSHOT/bin/airavata-server-start.sh $COMPONENT


