## Purpose of Vault Keys

* `airavata_sym.jks` - symmetric key used to encrypt all secrets in the credential store.
* `airavata.jks` - 
* `client_truststore.jks` - TLS certificate used to validate incoming https connections. generated from certbot fullchain.pem.

## Run on Build Server

```bash

# build from source
mvn clean install -DskipTests

# define hostname and basepath
HOSTNAME=exouser@api.dev.cybershuttle.org
BASEPATH="~/airavata-deployment/airavata-services-v2"

# copy the shell scripts
scp dev-tools/deployment-scripts/*.sh $HOSTNAME:$BASEPATH/

# copy the vault
scp dev-tools/deployment-scripts/vault/* $HOSTNAME:$BASEPATH/vault/

scp -r distribution $HOSTNAME:$BASEPATH/

```

## Run on Deployment Server
```bash

BASEPATH=$HOME/airavata-deployment/airavata-services-v2
cd $BASEPATH

./services_down.sh
./distribution_update.sh
./services_up.sh

multitail apache-airavata-*/logs/*.log

```