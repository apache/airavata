### Goal: Orchestration of Airavata Components:

Use the link to install kubenetes using [kubeadm](https://kubernetes.io/docs/setup/independent/install-kubeadm/)

#### install kubernetes management plane and make sure kubernetes is running properly


#### deploy airavata using kubenrnetes

1) `git clone https://github.com/satyamsah/airavata.git`

2) `cd airavata`

2) `git checkout orchestration`

3) `cd dev-tools/orchestration/kubernetes/airavata-setup/`

4) `kubectl create -f airavata-mq.yml`

5) `kubectl create -f airavata-base.yml`

5) `kubectl create -f airavata-all-rc.yml`

5) `kubectl create -f airavata-all-svc.yml`
