#!/bin/sh

if test -z "${BROKER_ID}"; then
  BROKER_ID=$(ifconfig eth0 | awk '/inet addr/{print substr($2,6)}' | head -n1  | tr -d '.')
fi

mkdir -p /opt/kafka/etc
cat <<EOF > /opt/kafka/etc/server.properties
broker.id=${BROKER_ID}
zookeeper.connect=${ZOOKEEPER}
log.dirs=${LOG_DIRS}
num.partitions=2
default.replication.factor=2
advertised.host.name=${ADVERTISED_HOST_NAME}
EOF

exec /opt/kafka/bin/kafka-server-start.sh /opt/kafka/etc/server.properties
