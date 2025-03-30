# Testing Kubernetes with the Container ecosystem of Airavata

Airavata repo already has many Dockerfiles to run services individually. There were also many cases where different orchestration platforms were tested with airavata.But, Kubernetes was not really tested with airavata. This is one POC on Kubernetes airavata.

# Status of the POC

## Exploring Kubernetes

Kubernetes, as told above, is an Orchestration Layer for orchestrating containers. There are different open source APIs for Containers creations and most popular of them is Docker.There are already some docker files which package airavata into docker containers.

Airavata PGA Dockerfile: https://github.com/apache/airavata/pull/122

Airavata Backend Components: https://github.com/apache/airavata/pull/67


### Thought Process and Approach

Initial Step is to understand the different components involved in Kubernetes. In that process, it is important to setup a Kubernetes Cluster and experiment with things.

Kubernetes cluster was setup on Jetstream. There is master node running on 149.165.169.147 and a slave node for Kubernetes is running on 149.165.169.156.

Kubernetes mainly has three components in them. One is kubernetes orchestration engine. One is etcd which is a distributed key-value store. Keys can be stored in this keystore and can be reliably accessed in a distributed eco-system. Kubernetes recommends this keystore to be used to store network configurations of the cluster.

Kubernetes needs a subnet to be given as input through etcd keystore. Whenever a new Pod is created under the master, IP Address is allocated from that subnet. This clears the problems when a pod is down. As a range is allocated, we can directly hit the gateway address and through another service called Flanneld, the request is routed.

![](https://github.com/hiteshkumardasika/airavata/blob/master/test.png)

Kubernetes has the concept called Pods. Pods are an abstraction over a group of containers. Each pod will have one service running. That service can be run in one containers or many depending on the request load. This detail is taken care by Kubernetes.

To manage and lease subnets for pods, Flanneld is used. It is an application which takes the subnets that are allocated to Kubernetes from the etcd Keystore and leases subnets to the pods in Kubernetes.

In jetstream, there are options to create your own Private networks and also subnets inside those networks. This feature was leveraged.

## Status right now

Configuring private networks and allocating subnets was a trouble initially. Theoretical concepts were familiar, but when it came to implementation, it took a bit more time to understand and figure out who all the puzzles where put in place. It was also essential to get an understanding of each service like etcd, flanneld and kubectl and also understand the need of them. Hence, right now i was able to configure kubernetes and setup the cluster. I am in process of starting a pod and understanding how to start a container through a Dockerfile which i already have.

Kubectl is a kubernetes control utility that is similar to the docker cli utility and given an image starting a pod is not a big task. Hence continuing my existing task by starting all the Airavata components in different pods and seeing how everything works.


## Kubernetes with Airavata - Will it Fit the Architecture?

Airavata already has a very good distibuted architecture and also different applications are clearly separated.(Micro Service Architecture). Hence it would be a good fit for orchestration in general. Specifically the advantage we get with Kubernetes is that this orchestration framework at a pretty low level.By this I mean at the request-response level, irrespective of the Softwares or frameworks which are at the top.

For example:
Kubernetes can be configured to be run along with Apache Mesos and Mesos can take care of the resource management at a level above Kubernetes. But, kubernetes is configured to load balance Mesos itslef and hence it is a better distributed architecture. This approach can be debated but for now it seems logical.


## Final Update -- Full Installation Steps

# Airavata-PGA on Kubernetes

First thing is to setup a cluster to deploy Kubernetes. Cluster, in this case, is nothing but two instances which fall on the same Subnet.

There are many ways in which Kubernetes can be set up.But I tried setting up it in terms of a private cluster starting from scratch and given a bare metal instance what are the different installations that are required to setup a cluster to run Kubernetes with Airavata Components.

## Using Baremetal Cluster

* Initially launch two Jetstream instances on a private network
* Create a subnet that can be used by the services to allocate themselves an IP Address. Kubernetes allocates an IP address from the subnet lease that was provided to it
* There are some installations that need to be made in each instance. This task can be automated.But, for now they are manual steps.

### Installations on each instance

As all of them are CENTOS based instances, we can create a repo in all of those instances and yum install through that repo and also other installations can be done easily

`vim /etc/yum.repos.d/virt7-docker-common-release.repo`

`[virt7-docker-common-release]
name=virt7-docker-common-release
baseurl=http://cbs.centos.org/repos/virt7-docker-common-release/x86_64/os/
gpgcheck=0`

Next, run the following command on each of the instances to be launched

`yum -y install --enablerepo=virt7-docker-common-release kubernetes etcd flannel`

As you can clearly observe, these are steps which can be automated through ansible scripts. Hence they shouldn't be a hindrance to the use.

## Kubernetes Configuration files

After successful Kubernetes installation, there will be a config file in 
`/etc/kubernetes/config` which needs to be changed which indicate the master node's ip adress etc..File comments can tell the details of each line in the file.

### ETCD Configuration on Master

ETCD is a key-value pair storage service for the complete cluster. It stores the details of the Cluster allocated subnet and the ip addresses that are allocated to the subnet. To add the subnet that is leased, execute the following command on master

`etcdctl mkdir /kube-centos/network`

`etcdctl mk /kube-centos/network/config "{ \"Network\": \"<subnet-address>\", \"SubnetLen\": 24, \"Backend\": { \"Type\": \"vxlan\" } }"`

### Kubelet Configurations on the Master

kubelet is the binary which does the kubernetes work. Hence on each instance, the kubelet can be configured as per the needs. Only main config that needs to be changed would be to include the host IP address if you donot want te hostname of the instance to be used.

### Configuring flanneld

Flanneld is a service which controls the subnet. When a new instance is added, flanneld allocates an Ip from the subnet that is reserved. It refers to etcd to get the allocated subnet and leases out an IP address from it.

Now, configuration file of flanneld contains a line which tells the flanneld service to look for etcd subnet details. 

`FLANNEL_ETCD_PREFIX="/kube-centos/network"`

Enter the prefix that was created above in etcd case.


Kubernetes cluster is ready to be used.


## Docker Private Registry Setup

To facilitate the image storage, I have tried using a private registry instead of using the Public Docker registry as my aim was to create a complete private cluster.

Docker registry is just a docker container that can be used to pull and push images to or from it. It requires a password authentication to log into it. Hence first step is to setup a htpasswd file. Follow these steps..

`mkdir /opt/registry/auth`

Next step, start the docker registry container

`docker run --entrypoint htpasswd registry:2 -Bbn admin <insert-password> >> /opt/registry/auth/htpasswd`

Next step is to configure SSL Certificates for the registry. Certificates can be stored in 

'mkdir /opt/registry/cert && cd /opt/registry/cert'

'cat my-cert.crt DigiCertCA.crt > registry-cert.crt'

Create a directory to store the image data

`mkdir /opt/registry/data`

Next use the docker-compose yaml to start the registry




## Starting all the Components

To start deployments of each service, proceed to each directory and run the following command

`kubectl create -f <####-deploy.yaml>`

Every directory has this file to start the deployment of the Services.


Next step is to make the services out of deployments

`kubectl create -f <###-service>.yaml`



This should setup airavata full application






