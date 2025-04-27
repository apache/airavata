## This directory Contains required files and instructions to replicate the Process of creating a Kubernetes Pod that runs Airavata PGA Containers

The test cluster has two Nodes in it and by default one will be the Master and the other will be the Slave. There are many different ways to start a container in a pod. Some of them are pulling the image from docker official repository or having a private registry for your cluster and pulling it.

For now, i have not used both of the options but i have built the image induvidually in both the nodes. The reccomended way is to use a private registry and pull from it.

For building images, the airavata-pga Dockerfile is already available from my previous assignment and build instructions were also available. I have used them to build the image in all the nodes.


Now, to start a pod in the Kubernetes, a yaml file needs to be written which will have instructions like which image to use when starting a container, how many containers need to be started etc. It also will take all the arguments that are generally passed to docker run command.

To start the pga-container, it needs some configurations and these configurations are mounted as a mount point. To achieve that, configmaps need to be created for the Kubernetes cluster. These are used in the Pod yaml file

PGA Pod was started in the Container and now im trying to start the airavata backend services. There is a Dockerfile that exists and that needs to be included in the yaml file which is the next step
