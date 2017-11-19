### Goal: Orchestration of Airavata Components:



#### install kubernetes management plane and make sure kubernetes is running properly

Use the link to install kubenetes using [kubeadm](https://kubernetes.io/docs/setup/independent/install-kubeadm/)

I have 3 slave nodes with 1 master

#### deploy airavata using kubenrnetes

1) login into master node

2) `git clone https://github.com/satyamsah/airavata.git`

3) `cd airavata`

4) `git checkout orchestration`

5) `cd dev-tools/orchestration/kubernetes/airavata-setup/`

6) `kubectl create -f  	rmq-deploy.yml`

7) `kubectl create -f  	rmq-svc.yml`

8) `s=$(kubectl describe svc | grep :5672);rabbitmqip="$( cut -d ':' -f 2 <<< "$s" )"; echo "$rabbitmqip"`

9) `kubectl create -f  	mariadb-deploy.yml`

10) `s=$(kubectl describe deploy | grep :3306);mariadbip="$( cut -d ':' -f 2 <<< "$s" )"; echo "$mariadbip"`

11) `kubectl create -f zookeeper-deploy.yml`

12) `s=$(kubectl describe deploy | grep :3888);zookeeperip="$( cut -d ':' -f 2 <<< "$s" )"; echo "$zookeeperip"`

13)  `cd ../dockermodule/middleware/`

14) edit the file to change the ip addresses/hostname with respective placeholder ip adresses of rmq, mariadb and zookeepeer and save the file.

15) `kubectl create -f airavata-all-rc.yml`

16) The airavata-setup should be up
