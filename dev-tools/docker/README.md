# Docker

## Manual Setup

### 1. Build 
Build all the required modules 
```
cd dev-tools/docker/rabbitmq
docker build -t airavata/rabbitmq .

cd dev-tools/docker/mariadb
docker build -t airavata/mariadb .

cd dev-tools/docker/zookeeper
docker build -t airavata/zookeeper .
```
### 2. Update Config File (optional)
- As the airavata-server.properties is going to be moved in to the build, update them if required
- TODO: Script to move the properties files on demand and restart the server
```
cd dev-tools/docker/middleware
vim airavata-server.properties
```

### 3. Build Base image
Base Airavata Image
```
cd dev-tools/docker 
docker build -t airavata/base .
```

### 4. Build individual dockerfile
- Docker base image is used by ApiServer, GFAC, credentialstore and Orch.
```
cd dev-tools/docker/middleware
docker build --build-arg COMPONENT=credentialstore -t airavata/credentialstore  .
docker build --build-arg COMPONENT=orchestrator -t airavata/orchestrator  .
docker build --build-arg COMPONENT=gfac -t airavata/gfac  .
docker build --build-arg COMPONENT=apiserver -t airavata/apiserver . 
```
- (Optional) Building all middleware components(apiserver, GFAC & orch) in the same build
```
cd dev-tools/docker/middleware
docker build --build-arg COMPONENT=all -t airavata/all .
```
### Run
- Run individual images
- TODO: Understand the order in which the images are to be brought up.
```
docker run --hostname rabbitmq --name rabbitmq -p 15672:15672 -p 5672:5672 -e RABBITMQ_DEFAULT_USER=airavata -e RABBITMQ_DEFAULT_PASS=airavata -d airavata/rabbitmq
docker run --name mariadb -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_USER=airavata -e MYSQL_PASSWORD=airavata -p 3306:3306 -d airavata/mariadb
docker run --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 -p 8080:8080  -d airavata/zookeeper

docker run --name all -it --link rabbitmq:rabbitmq airavata/all
```

## Quick Shortcuts
Remove all local dockers
```
docker rm -f $(docker ps -a -q)
```
Remove all images
```
docker rmi -f $(docker images -q)
```
