## Prerequisites

* Docker installed with 'docker-compose' utility

* InteliJ IDEA with Java 8 installed

* Git

## Steps

### Setting up the development environment

* Clone Airavata repository to a local directory
git clone https://github.com/apache/airavata

* Checkout develop branch
git checkout develop

* Open the project using InteliJ IDEA

* Browse to modules -> ide-integration module

### Starting backend components

* Add a host entry to /etc/hosts file in local machine

    127.0.0.1 airavata.host

* Go to src/main/resources directory and run docker-compose up

* Wait until all the services come up. This will initialize all utilities required to start Airavata server

* Go to org.apache.airavata.ide.integration.APIServerStarter class and right click on the editor and click Run option. This will start Airavata server

### Starting PGA

* Go to src/main/resources/pga directory and run 

    docker-compose up -d

* Run following command to get the ip address of host machine

 This command is for docker containers deployed on Mac OSX  

    docker-compose exec pga getent hosts docker.for.mac.host.internal | awk '{ print $1 }'

* Update the host entries of pga container with above ip address

    docker-compose exec pga /bin/sh -c "echo '<host-machine ip> airavata.host' >> /etc/hosts"

* Now PGA should be accessible through http://airavata.host:8008

### Stop all components

* For each composer file, run 

   docker-compose down
 
   docker-compose rm