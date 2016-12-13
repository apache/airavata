# airavata-ansible

Ansible script to deploy Apache Airavata and PGA. 
There are ansible roles to install Airavata pre-requisites (RabbitMQ, Zookeeper, MariaDB).

## Supported OS with versions.

- Centos 7

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

- `ansible-playbook -i hosts site.yml`
- `ansible-playbook -i hosts site.yml -t "tags"`
- `ansible-playbook -i hosts site.yml --start-at-task="name of the ansible task"`

To deploy pga run following. see site.yml (playbook) file for other available tags.

- `ansible-playbook -i hosts site.yml -t "pga"`

## Configurations

- Set correct private key file to `ansible_ssh_private_key_file` property in group_vars/all
