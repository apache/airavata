added elk stack capability:
```
git clone https://github.com/satyamsah/airavata/tree/elk_stack

git checkout elk_stak

cd airavta/dev-tools/elk_stack
```
## Setup
- Build dockerfile
```
docker build -t airavata/kafka .
```
- Execute the below statements
```
docker run -d -p 2181:2181 --name zookeeper --hostaname zookeeper
docker run -d --name kafka -p 9092:9092 --link zookeeper airavata/kafka
ZK_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' zookeeper)
KAFKA_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' kafka)
echo $ZK_IP, $KAFKA_IP

```

### Create Topic
- (Optional) If the topic is already created then this step can be skipped
```
docker run --rm airavata/kafka kafka-topics.sh --create --topic test_all_logs --replication-factor 1 --partitions 1 --zookeeper $ZK_IP:2181

docker run --rm airavata/kafka kafka-topics.sh --list --zookeeper $ZK_IP:2181
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
- Changes are required in airavata-server.properties under "Kafka Logging related configuration". Change it to actual ZK_IP
```
isRunningOnAws=false
kafka.broker.list=<ZK_IP>:9092 # change it to actual  ZK_IP
kafka.topic.prefix=test
enable.kafka.logging=true
```
- Since kafka depends on zookeeper, if we use custom zookeeper, then we would have to disable the zookeeper in airavata under "Zookeeper" section . Change it to actual  KAFKA_IP
```
embedded.zk=false
zookeeper.server.connection=<KAFKA_IP>:2181 # change it to actual  KAFKA_IP
zookeeper.timeout=30000
```
### Run Airavata server
```
sh airavata-server-start.sh all
```

### Consume Messages
- This step is optional if testing kibana
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
### Kibana
- Kibana can be accessed at http://localhost:5601/
- Logs can be filtered at Visualize page
