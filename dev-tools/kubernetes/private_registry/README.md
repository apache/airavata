### Docker Private Registry Creation

Docker registry is just a docker container that can be used to pull and push images to or from it. It requires a password authentication to log into it. Hence first step is to setup a htpasswd file. Follow these steps..

`mkdir /opt/registry/auth`

Next step, start the docker registry container

`docker run --entrypoint htpasswd registry:2 -Bbn admin <insert-password> >> /opt/registry/auth/htpasswd`

Next step is to configure SSL Certificates for the registry. Certificates can be stored in 

'mkdir /opt/registry/cert && cd /opt/registry/cert'

'cat my-cert.crt DigiCertCA.crt > registry-cert.crt'

Create a directory to store the image data

`mkdir /opt/registry/data`

