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

### Create Topic
- (Optional) If the topic is already created then this step can be skipped
```
docker run --rm airavata/kafka \
> kafka-topics.sh --create --topic test_all_logs --replication-factor 1 --partitions 1 --zookeeper $ZK_IP:2181
```

## Testing 
### Build Start Airavata Server
- In Root Folder of airavata project
```
mvn clean install -Dmaven.test.skip=true
```
- Extract the build
```
tar xvzf airavata/modules/distribution/target/apache-airavata-server-0.17-SNAPSHOT-bin.tar.gz
```
### Update Config
```
cd airavata/modules/distribution/target/apache-airavata-server-0.17-SNAPSHOT/bin
vim airavata-server.properties
```
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
### Run Airavata server
```
sh airavata-server-start.sh all
```

### Consume Messages
- Logs stored in the kafka can be view on terminal
```
docker run --rm airavata/kafka kafka-console-consumer.sh \
> --topic test_all_logs --from-beginning --zookeeper $ZK_IP:2181
```
### ELK Stack
- Logs emitted from kafka are consumed by logstash
```
cd /airavata/dev-tools/docker/elk/
docker build -t airavata/elk .
docker run -p 5601:5601 -p 9200:9200 -p 5044:5044  -it --name elk airavata/elk
(In new terminal)
sudo docker exec -it elk /bin/bash
/opt/logstash/bin/logstash -f /opt/logstash/airavata/logstash-airavata.conf --path.data /opt/logstash/airavata
```
