# mesos-deployment
This project setup Apache Mesos master and slave clusters on Cloud Infrastructures using Anisble

If you are working with new set of hosts which you haven't ssh login to those host from the machine you are suppose to run your Ansible play. It will prompt you to get permission to add to known_hosts list, if you wan't remove this prompt and you know the consequences of disabling this feature and you want tor remove this prompt behavior run this on your terminal  `export ANSIBLE_HOST_KEY_CHECKING=False`

## Configurations

To run this ansible script you must have at least 4 instances. 3 instances to setup Mesos master , marathon and zookeeper clusters. Along with Mesos master we install marathon and zookeeper. Another one or more instance/s to setup mesos agent/s. You can use __ec2__ ansible role provided with this playbook to spin up aws instances OR use the __openstack__ ansible role to spin up OpenStack instances. For either of these roles to work, you need to set valid credentials for AWS or OpenStack.

### AWS Configuration & Provisioning

1. Install the __boto__ python package using the following command:
  `pip install boto`

2. set valid aws credentials in `roles/ec2/vars/aws-credential.yml`. You need to set the following parameters:

  `aws_access_key: <your_valid_access_key>`

  `aws_secret_key: <your_valid_secret_key>`

3. Set ec2 instance names under ec2 task `with_items:` configurations

After you set valid aws credentials and instance names run following ansible playbook command to spin up require aws ec2 instances.

  `ansible-playbook -i hosts site.yml -t "ec2"`


### OpenStack Configuration & Provisioning

1. Install the __shade__ python package using the following command:
  `pip install shade`

2. set valid openstack credentials in `roles/openstack/vars/openstack-credential.yml`. You need to set the following parameters:

  `os_username: <your_valid_openstack_username>`

  `os_password: <your_valid_openstack_password>`

  `os_project_name: <your_valid_openstack_project_name>`

  `os_auth_url: <your_valid_keystone_auth_url>`

  `os_region_name: <your_valid_openstack_region>`
  

3. Set OpenStack instance names under openstack task `with_items:` configurations


After you set valid OpenStack credentials and instance names, run the following ansible playbook command to spin up require OpenStack instances.

  `ansible-playbook -i hosts site.yml -t “openstack”`


## Installation

1.  You need to know public ips of all the nodes and private ips of all mesos-master nodes.  List all mesos master public ips under `[mesos-master]` section in `hosts` file. List all mesos-slave *(mesos-agent)* public ips under `[mesos-slave]` section.  You need to set `my_id` hosts variable along with mesos-master ip addresses. This will use to set zookeeper id.

 > [mesos-master]

 > *1.2.3.4 my_id=1*

 > *11.22.33.44 my_id=2*

 > ...

 > [mesos-slave]

 > *123.123.123.123*

 > ...

2. Use mesos-master private ip addresses to set zookeeper servers properties in `roles/zookeeper/vars/main.yml`

  > zookeeper_servers:

  >  \- {id: "1", ip: "172.31.24.149"}

  >  \- {id: "2", ip: "172.31.19.49"}

  >  \- {id: "3", ip: "172.31.25.80"}

3. Set Mesos-master zookeeper quorum value in `group_vars/all.yml` file

  > zk_quorum: 2

4. Now we are set to deploy Apache Mesos cluster (Mesos-master, marthon, zookeeper, mesos-slave). Following ansible playbook command respectively setup mesos-master cluster , Mesos-slaves, and both mesos-master cluster and mesos-slave

  `ansible-playbook -i hosts site.yml  -t "mesos-master"`

  `ansible-playbook -i hosts site.yml  -t "mesos-slave"`

  `ansible-playbook -i hosts site.yml  -t "mesos"`

If everything works without any error, now you have running mesos cluster on aws/openstack instances.


## Verifying installation

1.  If your plays works without any error. Now you can access Mesos master console using `http://<master-ip>:5050` run following in one of Mesos master node to find which instance is the leader of cluster.

  ``mesos-resolve `cat /etc/mesos/zk` `` this will return master-ip. Then use it to access console

2. To access marathon console use `http://<master-ip>:8080`

3. If you want run simple task on mesos. run following command on one of master node.

  ``MASTER=$(mesos-resolve `cat /etc/mesos/zk`)``

  `mesos-execute --master=$MASTER --name="cluster-test" --command="sleep 5"`



## Ansible roles
- java - install oracle java 8
- zookeeper - install zookeeper
- mesos-master - install mesos mastera and marathon
- mesos-slave  - install mesos slave
- ec2 - provision instances on aws
- openstack - provision instances on openstack
