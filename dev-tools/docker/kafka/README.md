# Kafka
Used [ches/kafka](https://github.com/ches/docker-kafka) the base.

## Setup
- Build dockerfile
```
docker build -t airavata/kafka .
```
- Execute the below statements
```
docker run -d -p 2181:2181 --name zookeeper jplock/zookeeper
docker run -d --name kafka --link zookeeper:zookeeper airavata/kafka
ZK_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' zookeeper)
KAFKA_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' kafka)
echo $ZK_IP, $KAFKA_IP

```

### Config
- Changes are required in airavata-server.properties under "Kafka Logging related configuration"
```
isRunningOnAws=false
kafka.broker.list=172.17.0.3:9092
kafka.topic.prefix=test_all_logs
enable.kafka.logging=true
```
- Since kafka depends on zookeeper, if we use custom zookeeper, then we would have to disable the zookeeper in airavata under "Zookeeper" section 
```
embedded.zk=false
zookeeper.server.connection=172.17.0.2:2181
zookeeper.timeout=30000
```

### Create Topic
- (Optional) If the topic is already created then this step can be skipped
```
docker run --rm airavata/kafka \
> kafka-topics.sh --create --topic test_all_logs --replication-factor 1 --partitions 1 --zookeeper $ZK_IP:2181
```

### Consume Messages
```
docker run --rm airavata/kafka kafka-console-consumer.sh \
> --topic test_all_logs --from-beginning --zookeeper $ZK_IP:2181
```
