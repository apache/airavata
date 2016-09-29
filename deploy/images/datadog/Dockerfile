FROM datadog/docker-dd-agent:latest

ENV JAVA_MAJOR 8
ENV JAVA_MINOR 72
ENV JAVA_BUILD 15

RUN apt-get update \
  && apt-get install -y net-tools curl ca-certificates \
  && cd /opt \
  && curl -sSLH "Cookie: oraclelicense=accept-securebackup-cookie" \
    "http://download.oracle.com/otn-pub/java/jdk/${JAVA_MAJOR}u${JAVA_MINOR}-b${JAVA_BUILD}/server-jre-${JAVA_MAJOR}u${JAVA_MINOR}-linux-x64.tar.gz" -o jdk.tar.gz \
  && tar -zxf jdk.tar.gz \
  && update-alternatives --install /usr/bin/java java /opt/jdk1.$JAVA_MAJOR.0_$JAVA_MINOR/bin/java 100 \
  && rm jdk.tar.gz \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY conf.d/ /etc/dd-agent/conf.d/