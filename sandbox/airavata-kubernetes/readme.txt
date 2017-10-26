Create two kafka topics

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 1 --topic airavata-launch

bin/kafka-topics.sh --create --zookeeper localhost:2199 --replication-factor 1 --partitions 100 --topic airavata-gfac
