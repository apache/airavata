#!/bin/sh

if test -z "${BROKER_ID}"; then
  BROKER_ID=$(ip a | grep 'eth0' | awk '/inet /{print substr($2,4)}'| sed 's/\///g' | head -n1 | tr -d .)
fi

mkdir -p /opt/kafka/etc
cat <<EOF > /opt/kafka/etc/server.properties
broker.id=${BROKER_ID}
zookeeper.connect=${ZOOKEEPER}
log.dirs=${LOG_DIRS}
num.partitions=2
default.replication.factor=1
advertised.host.name=${ADVERTISED_HOST_NAME}
EOF

exec /opt/kafka/bin/kafka-server-start.sh /opt/kafka/etc/server.properties
