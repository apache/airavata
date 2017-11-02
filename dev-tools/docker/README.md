# Docker
Build individual docker
Docker base image is used by ApiServer, GFAC and Orch

```
docker build -t airavata/base .
docker build -t airavata/apiserver .
docker build -t airavata/rabbitmq .
docker build -t airavata/mariadb .
docker build -t airavata/zookeeper .
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
