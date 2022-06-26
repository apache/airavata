## Introduction 

Using this module, you can setup a full Airavata installation inside Intelij IDEA for development purposes

## Prerequisites

* Docker installed with 'docker-compose' utility
  https://docs.docker.com/compose/

* InteliJ IDEA with Java 8 installed
  https://www.jetbrains.com/idea/download/#section=mac

* Maven

* Git

* python3

* npm (install or update to latest version)
  https://www.npmjs.com/get-npm

## Steps

### Setting up the development environment

* Clone Airavata repository to a local directory

  ```
  git clone https://github.com/apache/airavata
  ```

* Checkout develop branch

  ```
  git checkout develop
  ```
* Build the develop branch using Maven

  ```
  mvn clean install -DskipTests
  ```
* Open the project using InteliJ IDEA

* Browse to modules -> ide-integration module

### Starting backend components (Database, Keycloak, Kafka, RabbitMQ, SSHD Server)

* Add a host entry to /etc/hosts file in local machine

  ```
  127.0.0.1 airavata.host
  ```

* Go to src/main/containers directory and run 

  ```
  docker-compose up
  ```

* Apply any database migrations. Go to src/main/containers directory and run

  ```
  cat ./database_scripts/init/*-migrations.sql | docker exec -i resources_db_1 mysql -p123456
  ```

* Wait until all the services come up. This will initialize all utilities required to start Airavata server

### Starting API Server

* Go to org.apache.airavata.ide.integration.APIServerStarter class and right click on the editor and click Run option. This will start Airavata server

### Starting Job Execution Engine

* Go to org.apache.airavata.ide.integration.JobEngineStarter class and right click on the editor and click Run option. 
This will start all components of Job Execution Engine including Helix Controller, Helix Participant, Pre Workflow Manager and 
Post Workflow Manager

### Starting Job Monitoring components

* This will start the Email Based Job Monitoring agent. Before starting this, you have to create a new gmail account by going to 
https://accounts.google.com/signup

* Once the account is created, turn on 2-Step Verification and create an App Password (Use the type "Other" from the App type selection and give the name as "Airavata")
https://myaccount.google.com/security

* Update the email address and App Password in src/main/resources/airavata-server.properties file

  email.based.monitor.address=CHANGEME
  email.based.monitor.password=CHANGEME
  
* Go to org.apache.airavata.ide.integration.JobMonitorStarter class and right click on the editor and click Run option.

### Starting User Portal (Django Portal)

* You can create and launch experiments and manage credentials using this portal

* This is a separate project so you need to clone this in to a new directory outside the Airavata code base
  
  ```
  git clone https://github.com/apache/airavata-django-portal
  ```
  
* Go to airavata-django-portal directory and run 

  ```
  python3 -m venv venv
  source venv/bin/activate
  pip install -r requirements.txt
  ```
* Create a local settings file. Copy
      `django_airavata/settings_local.py.ide` to
      `django_airavata/settings_local.py` 

* Run Django migrations

    ```
    python3 manage.py migrate
    ```

*  Build the JavaScript sources. There are a few JavaScript packages in the source tree, colocated with the Django apps in which they are used. The `build_js.sh` script will build them all.

    ```
    ./build_js.sh
    ```

*  Load the default Wagtail CMS pages.

    ```
    python3 manage.py load_default_gateway
    ```

*  Run the server

    ```
    python3 manage.py runserver  
    ```
    
* Point your browser to http://localhost:8000/auth/login. Use user name : default-admin and password : 123456 

### Optional: Starting Super Admin Portal (PGA)

* This portal is required when you are going to register new compute resources or storage resources into the gateway

* Go to src/main/containers/pga directory and run 

  ```
  docker-compose up -d
  ```

* Run following command to get the ip address of host machine

  For Mac OSX  

  ```
  docker-compose exec pga getent hosts docker.for.mac.host.internal | awk '{ print $1 }'
  ```
  
  For Windows
  
  ```
  docker-compose exec pga getent hosts host.docker.internal
  ```

* Update the host entries of pga container with above ip address

  ```
  docker-compose exec pga /bin/sh -c "echo '<host-machine ip> airavata.host' >> /etc/hosts"
  ```

* Now PGA should be accessible through http://airavata.host:8008

* Use the username : default-admin and password : 123456 to login to the portal

### Stop all components

* For each composer file, run following commands to cleanup docker spawned components

  ```
  docker-compose down
  ```
 
  ```
  docker-compose rm
  ```
  
### NOTE: (Optional) Creating certificates if expired 
  
  * This is required only when the self signed certificate for keycloak is expired
  * Go to src/main/resources/keystores
  * Provide password as airavata for all key stores

  ```  
  rm airavata.jks
  
  rm client_truststore.jks
  
  keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass airavata -validity 360 -keysize 2048
  What is your first and last name?
    [Unknown]:  airavata.host
  What is the name of your organizational unit?
    [Unknown]:  airavata.host
  What is the name of your organization?
    [Unknown]:  airavata.host
  What is the name of your City or Locality?
    [Unknown]:  airavata.host
  What is the name of your State or Province?
    [Unknown]:  airavata.host
  What is the two-letter country code for this unit?
    [Unknown]:  airavata.host
  Is CN=airavata.host, OU=airavata.host, O=airavata.host, L=airavata.host, ST=airavata.host, C=airavata.host correct?
    [no]:  yes


  keytool -importkeystore -srckeystore keystore.jks -destkeystore airavata.jks -deststoretype pkcs12

  rm keystore.jks

  keytool  -export -alias selfsigned -file root.cer -keystore airavata.jks -storepass airavata

  keytool -import -alias mykey -file root.cer -keystore client_truststore.jks -storepass airavata

  rm root.cer

```

