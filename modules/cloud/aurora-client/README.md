# Aurora Thrift Client
This project implements a Thrift based Java client for Apache Aurora scheduler, running on a Mesos cluster. This client has been developed with a focus of being integrated with Apache Airavata for job submission & monitoring

## Requirements

In order to use this client, you must have the following:

* A Mesos cluster - running on any public cloud (eg: AWS)
  We have written ansible scripts to deploy a Mesos-Marathon cluster on any given cloud infrastructure.
  The scripts can be found in the ```modules/cloud/ansible-playbooks``` directory, with necessary steps.
	
* Aurora scheduler installed on this Mesos cluster. Follow instructions at: ```http://aurora.apache.org/documentation/latest/operations/installation/``` to install Aurora components in the cluster - including the scheduler, and worker components.

## Configuration

Please update the __aurora-scheduler.properties__ file in __src/main/resources__ directory with the following properties:

	```
	# aurora scheduler host-name
	aurora.scheduler.host=mesos-master-1
	
	# aurora scheduler port (http)
	aurora.scheduler.port=8081
	
	# aurora executor name
	aurora.executor.name=AuroraExecutor
	
	# mesos cluster name
	mesos.cluster.name=example
	```
	
Default values for some of the parameters are added to the properties file.

## Operations supported

Currently the following operations are supported, but more will be added progressively.

* Submit a job to the Aurora scheduler
  Job can be AdHoc(one time) or a Service (long running)

* Get job details

* Get pending jobs & reason for why it is pending

* Kill tasks in a Job, or kill all tasks in a Job

## Sample Code

The __AuroraClientSample.java__ contains sample code on how to use the Client.