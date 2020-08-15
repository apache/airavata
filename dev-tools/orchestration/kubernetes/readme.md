### Goal: Orchestration of Airavata Components:



#### install kubernetes management plane and make sure kubernetes is running properly

Use the link to install kubenetes using [kubeadm](https://kubernetes.io/docs/setup/independent/install-kubeadm/)

I have 3 slave nodes with 1 master

#### deploy airavata using kubenrnetes

1) login into master node

2) `git clone https://github.com/satyamsah/airavata.git`

3) `cd airavata`

4) `git checkout orch_kube`

5) `cd dev-tools/orchestration/kubernetes/airavata-setup/`

6) `kubectl create -f rmq-deploy.yml`

7) `kubectl create -f rmq-svc.yml`

9) `kubectl create -f mariadb-deploy.yml`

8) `kubectl create -f mariadb-svc.yml`

11) `kubectl create -f mariadb-deploy.yml`

12) `kubectl create -f zookeeper-svc.yml`

15) `kubectl create -f airavata-all-deploy.yml`

15) `kubectl create -f airavata-all-svc.yml`

16) The airavata-setup should be up
