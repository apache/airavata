# airavata-ansible

Ansible script to deploy Apache Airavata and PGA. 
There are ansible roles to install Airavata pre-requisites (RabbitMQ, Zookeeper, MariaDB).

## Ansible installation

Note: the following assumes a Bash shell.

1. Download and install the latest version of Python 3.6. See
   https://www.python.org/downloads/ or use your system's package manager.
2. Create a virtual environment in this directory

        cd airavata/dev-tools/ansible
        python3.6 -m venv ENV

3. Source the environment (you'll need to do this each time before using ansible commands)

        source ENV/bin/activate

4. Install ansible and any other dependencies.

        pip install -r requirements.txt

Now you should be ready to run `ansible-playbook` and other ansible commands.

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

## Configurations

- Set correct private key file to `ansible_ssh_private_key_file` property in group_vars/all
