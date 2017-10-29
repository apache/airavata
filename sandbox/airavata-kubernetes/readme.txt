Create following kafka topics

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 1 --topic airavata-launch

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-scheduler

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-task-ingress-staging

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-task-egress-staging

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-task-env-setup

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-task-env-cleanup

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-task-job-submission

Create a new database with name "airavata"

When running in a local machine, add following host entries to /etc/hosts file

127.0.0.1 db.default.svc.cluster.local
127.0.0.1 kafka.default.svc.cluster.local
127.0.0.1 api-server.default.svc.cluster.local

When running as docker containers, pass following environment variables to api-server container

spring_datasource_username=<db user>
spring_datasource_password=<db password>

To build docker images for each micoservice

mvn clean install docker:build -DdockerImageTags=v1.0

docker save dimuthuupe/api-server:v1.0 | bzip2 | ssh dimuthu@192.168.1.201 'bunzip2 | docker load'
docker save dimuthuupe/task-scheduler:v1.0 | bzip2 | ssh dimuthu@192.168.1.201 'bunzip2 | docker load'
docker save dimuthuupe/workflow-generator:v1.0 | bzip2 | ssh dimuthu@192.168.1.201 'bunzip2 | docker load'
docker save dimuthuupe/event-sink:v1.0 | bzip2 | ssh dimuthu@192.168.1.201 'bunzip2 | docker load'
docker save dimuthuupe/job-submission-task:v1.0 | bzip2 | ssh dimuthu@192.168.1.201 'bunzip2 | docker load'
docker save dimuthuupe/ingress-staging-task:v1.0 | bzip2 | ssh dimuthu@192.168.1.201 'bunzip2 | docker load'
docker save dimuthuupe/env-setup-task:v1.0 | bzip2 | ssh dimuthu@192.168.1.201 'bunzip2 | docker load'
docker save dimuthuupe/env-cleanup-task:v1.0 | bzip2 | ssh dimuthu@192.168.1.201 'bunzip2 | docker load'
docker save dimuthuupe/egress-staging-task:v1.0 | bzip2 | ssh dimuthu@192.168.1.201 'bunzip2 | docker load'

save dimuthuupe/api-server:v1.0 | bzip2 | ssh dimuthu@192.168.1.202 'bunzip2 | docker load'
docker save dimuthuupe/task-scheduler:v1.0 | bzip2 | ssh dimuthu@192.168.1.202 'bunzip2 | docker load'
docker save dimuthuupe/workflow-generator:v1.0 | bzip2 | ssh dimuthu@192.168.1.202 'bunzip2 | docker load'
docker save dimuthuupe/event-sink:v1.0 | bzip2 | ssh dimuthu@192.168.1.202 'bunzip2 | docker load'
docker save dimuthuupe/job-submission-task:v1.0 | bzip2 | ssh dimuthu@192.168.1.202 'bunzip2 | docker load'
docker save dimuthuupe/ingress-staging-task:v1.0 | bzip2 | ssh dimuthu@192.168.1.202 'bunzip2 | docker load'
docker save dimuthuupe/env-setup-task:v1.0 | bzip2 | ssh dimuthu@192.168.1.202 'bunzip2 | docker load'
docker save dimuthuupe/env-cleanup-task:v1.0 | bzip2 | ssh dimuthu@192.168.1.202 'bunzip2 | docker load'
docker save dimuthuupe/egress-staging-task:v1.0 | bzip2 | ssh dimuthu@192.168.1.202 'bunzip2 | docker load'