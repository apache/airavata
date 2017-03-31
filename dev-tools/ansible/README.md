# airavata-ansible

Ansible script to deploy Apache Airavata and PGA. 
There are ansible roles to install Airavata pre-requisites (RabbitMQ, Zookeeper, MariaDB).

## Supported OS with versions.

- Centos 7
- The PGA should also work on Ubuntu 16

## Roles

- **env_setup** :- Create user and group, install oracle java 8, open firewall ports.
- **zookeeper** :- Download and install zookeeper.
- **rabbitmq** :- Download and install rabbitmq as service.
- **database** :- Download and install mysql(mariadb) as a service.
- **common** :- Checkout Airavata source from git and run maven build. Move keystore files.
- **gfac** :- Setup and deploy Gfac component.
- **registry** Setup and deploy registry component.
- **api-orch** :- Setup and deploy Api-Orch components.
- **pga** :- Setup and deploy Airavata PHP Gateway.

## Useful commands

- `ansible-playbook -i inventories/develop site.yml`
- `ansible-playbook -i inventories/develop site.yml -t "tags"`
- `ansible-playbook -i inventories/develop site.yml --start-at-task="name of the ansible task"`

To deploy pga run following. see site.yml (playbook) file for other available tags.

- `ansible-playbook -i inventories/develop site.yml -t "pga"`

## Local Deployment in VirtualBox

Build three CentOS 7 VMs locally, on a host-only network (See README.md in inventories/local_vbox).

- `ansible-playbook -i inventories/local-vbox local_deploy.yml`

## Jetstream-based deployment

Deploying on Jetstream will require a few extra steps:

1. You will need API access to Jetstream, which must be requested 
as described here:
https://iujetstream.atlassian.net/wiki/display/JWT/Using+the+Jetstream+API

2. The 'shade' library will have to be added to your python environment. 
For example, `sudo apt-get install python-shade` or `pip install shade`

3. You will need to create a clouds.yaml file, which contains the 
 following (similar to the openrc file described in the Jetstream wiki):

```
clouds:
 tacc:
  auth: 
   username: value
   auth_url: value
   project_name: value
   password: value 
  user_domain_name: value
  project_domain_name: value
  identity_api_version: 3
```

4. Changes to ansible.cfg, ssh.cfg and inventories/jetstream/groupvars/all.yml :

ansible.cfg:
```
[ssh_connection]
ssh_args = -F ./ssh.cfg 
```

ssh.cfg:
```
Host 149.165.*.*
 User centos
 BatchMode yes
 StrictHostKeyChecking no
 UserKnownHostsFile=/dev/null
 IdentityFile $ssh_key_location
```

inventories/jetstream/groupvars/all.yml :
```
js_public_key: /path/to/public/ssh/key
```


Once those pieces are in place, you should be able to deploy vm's via

- `ansible-playbook -i inventories/jetstream all_vms_js.yml`

And deploy software them via:

- `ansible-playbook -i inventories/jetstream all_deploy_js.yml`

The same flags as above (-t, --start-at-task) are *quite* useful. 

## Configurations

- Set correct private key file to `ansible_ssh_private_key_file` property in group_vars/all
