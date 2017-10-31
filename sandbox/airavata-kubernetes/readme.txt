This project contains the implementation of the the components that compose the microservice based task execution workflow framework for Airavata

Design document and the user guide to install in a development environment can be found in the project root directory.

Implementation of microservices can be found in modules -> mircoservices directory

Implementation of the Web Console can be found in web-console directory

To build docker images for each micoservice, goto the module and run
mvn clean install docker:build -DdockerImageTags=v1.0

If you are running this for the first time, run mvn clean install at the root of the project

When running in a local machine, add following host entries to /etc/hosts file
127.0.0.1 db.default.svc.cluster.local
127.0.0.1 kafka.default.svc.cluster.local
127.0.0.1 api-server.default.svc.cluster.local

When running as docker containers, pass following environment variables to api-server container
spring_datasource_username=<db user>
spring_datasource_password=<db password>