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

6) `kubectl create -f  	rmq-rc.yml`

6) `kubectl create -f  	mq-svc.yml`

6) `kubectl create -f  	mariadb-rc.yml`

6) `kubectl create -f  	zookeeper-rc.yml`

7) `kubectl create -f airavata-base.yml`

8) `kubectl create -f airavata-all-rc.yml`

9) `kubectl create -f airavata-all-svc.yml`
