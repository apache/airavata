added elk stack capability:
```
git clone https://github.com/satyamsah/airavata

git checkout elk_stak

cd airavata/dev-tools/elk_stack

```

- Execute the below statements
```
docker run -p 2181:2181 --name zookeeper --hostname zookeeper zookeeper
docker run --name kafka --link zookeeper:zookeeper -e KAFKA_ADVERTISED_HOST_NAME=<current IP address of the docker host> -p 9092:9092 ches/kafka

ZK_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' zookeeper)
KAFKA_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' kafka)

echo $ZK_IP, $KAFKA_IP

```

### Create Topic
- (Optional) If the topic is already created then this step can be skipped
```
docker run --rm ches/kafka kafka-topics.sh --create --topic test_all_logs --replication-factor 1 --partitions 1 --zookeeper $ZK_IP:2181

docker run --rm ches/kafka kafka-topics.sh --list --zookeeper $ZK_IP:2181
```



## Airavata Testing (Skip these steps if you are using external airavata)
### Build Start Airavata Server
- In Root Folder of airavata project
```
cd ~/airavata
mvn clean install -Dmaven.test.skip=true
```
- Extract the build
```
tar xvzf airavata/modules/distribution/target/apache-airavata-server-0.17-SNAPSHOT-bin.tar.gz
```
### Update Config
```
cd airavata/modules/distribution/target/apache-airavata-server-0.17-SNAPSHOT/bin
sudo nano  airavata-server.properties
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
##  Airavata Testing Ends

### Consume Messages(optional for testing purpose)
- Logs stored in the kafka can be view on differnt terminal by the consumer

```
docker run --rm ches/kafka kafka-console-consumer.sh --topic test_all_logs --from-beginning --zookeeper $ZK_IP:2181
```

## ELK Stack
- Logs emitted from kafka are consumed by logstash, so changing logstash conf file
```
cd airavata/dev-tools/elk_stack/elk

```
Edit the file and give the ip address of Kafka and change the ip address of KAFKA_IP_ADDRESS to kafka specific IP address:

```
sudo nano logstash-airavata.conf
```
build the image and run the elk container. If exception happens please run the below mentioned systemctl command:

```
docker build -t airavata/elk .
sysctl -w vm.max_map_count=262144
docker run -p 5601:5601 -p 9200:9200 -p 5044:5044  -it --name elk airavata/elk
```
### Parsing the logs and Filtering based on keys
```
sudo docker exec -it elk /bin/bash
/opt/logstash/bin/logstash -f /opt/logstash/airavata/logstash-airavata.conf --path.data /opt/logstash/airavata
```



## Kibana
- Kibana can be accessed at http://localhost:5601/
- Logs can be filtered at Visualize page
