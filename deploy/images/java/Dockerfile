FROM debian:stable

ENV JAVA_MAJOR 8
ENV JAVA_MINOR 102
ENV JAVA_BUILD 14

ENV JAVA_HOME /opt/jdk
ENV PATH ${PATH}:${JAVA_HOME}/bin

RUN apt-get update \
  && apt-get install --assume-yes curl ca-certificates \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/* /var/log/apt/*

RUN curl -jksSLH "Cookie: oraclelicense=accept-securebackup-cookie" \
  http://download.oracle.com/otn-pub/java/jdk/${JAVA_MAJOR}u${JAVA_MINOR}-b${JAVA_BUILD}/server-jre-${JAVA_MAJOR}u${JAVA_MINOR}-linux-x64.tar.gz \
  | tar -zxvf - -C /opt && \
  ln -s /opt/jdk1.${JAVA_MAJOR}.0_${JAVA_MINOR} /opt/jdk && \
  rm -rf /opt/jdk/man/* \
         /opt/jdk/jre/bin/jjs \
         /opt/jdk/jre/bin/keytool \
         /opt/jdk/jre/bin/orbd \
         /opt/jdk/jre/bin/pack200 \
         /opt/jdk/jre/bin/policytool \
         /opt/jdk/jre/bin/rmid \
         /opt/jdk/jre/bin/rmiregistry \
         /opt/jdk/jre/bin/servertool \
         /opt/jdk/jre/bin/tnameserv \
         /opt/jdk/jre/bin/unpack200 \
         /opt/jdk/jre/lib/ext/nashorn.jar \
         /opt/jdk/jre/lib/oblique-fonts

