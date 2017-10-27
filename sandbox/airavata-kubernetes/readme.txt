Create following kafka topics

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 1 --topic airavata-launch

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-scheduler

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-task-ingress-staging

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-task-egress-staging

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-task-env-setup

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-task-env-cleanup

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-task-job-submission