# airavata-ansible

Ansible script to deploy Apache Airavata

## Support OS

- Centos 7

## Roles

- **setup** :- Create user and group, install oracle java 8
- **zookeeper** :- Download and install zookeeper.
- **rabbitmq** :- Download and install rabbitmq as service.
- **common** :- Checkout Airavata source from git and run maven build. Move keystore files.
- **gfac** :- Setup Gfac deployment and Change configurations.
- **api-orch** :- Setup Api-Orch deployment and Change configurations.
- **pga** :- Deploy Airavata PHP Gateway._(Under development)_

## Useful commands

- `ansible-playbook -i hosts site.yml`
- `ansible-playbook -i hosts site.yml -t "tags"`
- `ansible-playbook -i hosts site.yml --start-at-task="name of the ansible task"`

To deploy pga run following. see site.yml (playbook) file for other available tags.

- `ansible-playbook -i hosts site.yml -t "pga"`

## Configurations

- Set correct private key file to `ansible_ssh_private_key_file` property in group_vars/all
