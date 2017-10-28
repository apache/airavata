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
