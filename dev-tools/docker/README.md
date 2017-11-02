# Docker

### Build and Run all dockerfiles
```
docker-compose up
```

### Build 
Build individual dockerfile
- Docker base image is used by ApiServer, GFAC and Orch

```
docker build -t airavata/base .
docker build -t airavata/apiserver .
docker build -t airavata/rabbitmq .
docker build -t airavata/mariadb .
docker build -t airavata/zookeeper .
```
### Run
Run individual images
```
docker run --name apiserver --link rabbitmq:rabbitmq airavata/apiserver 
docker run --hostname rabbitmq --name rabbitmq -p 15672:15672 -p 5672:5672 -e RABBITMQ_DEFAULT_USER=airavata -e RABBITMQ_DEFAULT_PASS=airavata -d airavata/rabbitmq
docker run --name mariadb -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_USER=airavata -e MYSQL_PASSWORD=airavata -p 3306:3306 -d airavata/mariadb
docker run --name=zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 -p 8080:8080  -d airavata/zookeeper

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
