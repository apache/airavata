/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

include "../../airavata-apis/airavata_commons.thrift"
include "../app-catalog-models/parallelism_model.thrift"
include "data_movement_models.thrift"

namespace java org.apache.airavata.model.appcatalog.computeresource
namespace php Airavata.Model.AppCatalog.ComputeResource
namespace cpp apache.airavata.model.appcatalog.computeresource
namespace py airavata.model.appcatalog.computeresource


/**
 * Enumeration of local resource job manager types supported by Airavata
 *
 * FORK:
 *  Forking of commands without any job manager
 *
 * PBS:
 *  Job manager supporting the Portal Batch System (PBS) protocol. Some examples include TORQUE, PBSPro, Grid Engine.
 *
 * SLURM:
 *  The Simple Linux Utility for Resource Management is a open source workload manager.
  *
  * UGE:
  *  Univa Grid Engine, a variation of PBS implementation.
  *
  * LSF:
  *  IBM Platform Load Sharing Facility is dominantly installed on IBM clusters.
 *
*/
enum ResourceJobManagerType {
    FORK,
    PBS,
    SLURM,
    LSF,
    UGE,
    CLOUD,
    AIRAVATA_CUSTOM,
    HTCONDOR
}

/**
 * Enumeration of resource job manager commands
 *
 * SUBMISSION:
 *  Ex: qsub, sbatch
 *
 * JOBMONITORING:
 *  Ex: qstat, squeue
 *
 * DELETION:
 *  Ex: qdel, scancel
 *
 * CHECK_JOB:
 *  Detailed Status about the Job. Ex: checkjob
 *
 * SHOW_QUEUE:
 *  List of Queued Job by the schedular. Ex: showq
 *
 * SHOW_RESERVATION:
 *  List all reservations. Ex:showres, show_res
 *
 * SHOW_START:
 *  Display the start time of the specified job. Ex: showstart
 *
*/
enum JobManagerCommand {
    SUBMISSION,
    JOB_MONITORING,
    DELETION,
    CHECK_JOB,
    SHOW_QUEUE,
    SHOW_RESERVATION,
    SHOW_START
}


/**
 * Resource Job Manager Information
 *
 * resourceJobManagerType:
 *  A typical HPC cluster has a single Job Manager to manage the resources.
 *
 * pushMonitoringEndpoint:
 *  If the job manager pushes out state changes to a database or bus, specify the service endpoint.
 *   Ex: Moab Web Service, Moab MongoDB URL, AMQP (GLUE2) Broker
 *
 * jobManagerBinPath:
 *  Path to the Job Manager Installation Binary directory.
 *
 * jobManagerCommands:
 *  An enumeration of commonly used manager commands.
 *
*/
struct ResourceJobManager {
    1: required string resourceJobManagerId = airavata_commons.DEFAULT_ID,
    2: required ResourceJobManagerType resourceJobManagerType,
    3: optional string pushMonitoringEndpoint,
    4: optional string jobManagerBinPath,
    5: optional map<JobManagerCommand, string> jobManagerCommands,
    6: optional map<parallelism_model.ApplicationParallelismType, string> parallelismPrefix
}

/**
 * Enumeration of File Systems on the resource
 *
 * FORK:
 *  Forking of commands without any job manager
 *
 * PBS:
 *  Job manager supporting the Portal Batch System (PBS) protocol. Some examples include TORQUE, PBSPro, Grid Engine.
 *
 * UGE:
 *  Univa Grid Engine, a variation of PBS implementation.
 *
 * SLURM:
 *  The Simple Linux Utility for Resource Management is a open source workload manager.
 *
*/
enum FileSystems {
    HOME,
    WORK,
    LOCALTMP,
    SCRATCH,
    ARCHIVE
}

/**
 * Batch Queue Information on SuperComputers
 *
 * maxRunTime:
 *  Maximum allowed run time in hours.
*/
struct BatchQueue {
    1: required string queueName,
    2: optional string queueDescription,
    3: optional i32 maxRunTime,
    4: optional i32 maxNodes,
    5: optional i32 maxProcessors,
    6: optional i32 maxJobsInQueue,
    7: optional i32 maxMemory,
    8: optional i32 cpuPerNode,
    9: optional i32 defaultNodeCount,
    10: optional i32 defaultCPUCount,
    11: optional i32 defaultWalltime,
    12: optional string queueSpecificMacros,
    13: optional bool isDefaultQueue
}


/**
 * Enumeration of Airavata supported Job Submission Mechanisms for High Performance Computing Clusters.
 *
 * SSH:
 *  Execute remote job submission commands using via secure shell protocol.
 *
 * GRAM:
 *  Execute remote jobs via Globus GRAM service.
 *
 * UNICORE:
 *  Execute remote jobs via Unicore services
 *
*/
enum JobSubmissionProtocol {
    LOCAL,
    SSH,
    GLOBUS,
    UNICORE,
    CLOUD,
    SSH_FORK,
    LOCAL_FORK
}

/**
* Monitoring modes
*
* POLL_JOB_MANAGER:
* GFac need to pull job status changes.
*
* XSEDE_AMQP_SUBSCRIBE:
* Server will publish job status changes to amqp servert.
*
**/
enum MonitorMode {
   POLL_JOB_MANAGER,
   CLOUD_JOB_MONITOR,
   JOB_EMAIL_NOTIFICATION_MONITOR,
   XSEDE_AMQP_SUBSCRIBE,
   FORK,
   LOCAL
}

enum DMType {
    COMPUTE_RESOURCE,
    STORAGE_RESOURCE
}

/**
 * Locally Fork Jobs as OS processes
 *
 * alternativeSSHHostName:
 *  If the login to ssh is different than the hostname itself, specify it here
 *
 * sshPort:
 *  If a non-default port needs to used, specify it.
*/
struct LOCALSubmission {
    1: required string jobSubmissionInterfaceId = airavata_commons.DEFAULT_ID,
    2: required ResourceJobManager resourceJobManager,
    3: required data_movement_models.SecurityProtocol securityProtocol
}

/**
 * Authenticate using Secured Shell
 *
 * alternativeSSHHostName:
 *  If the login to ssh is different than the hostname itself, specify it here
 *
 * sshPort:
 *  If a non-default port needs to used, specify it.
 *
 * batchQueueEmailSenders:
 *  If a resource always sends the monitoring from a specific address, specify the
 *   full email address. If a resource sends emails from multiple addresses (
 *    example: based on the submitted login node) then use the wildchar * to indicate
 *    the same. Example: *@*.example.com or *@example.com
 *
*/
struct SSHJobSubmission {
    1: required string jobSubmissionInterfaceId = airavata_commons.DEFAULT_ID,
    2: required data_movement_models.SecurityProtocol securityProtocol,
    3: required ResourceJobManager resourceJobManager,
    4: optional string alternativeSSHHostName,
    5: optional i32 sshPort = 22,
    6: optional MonitorMode monitorMode,
    7: optional list<string> batchQueueEmailSenders
}

struct GlobusJobSubmission {
    1: required string jobSubmissionInterfaceId = airavata_commons.DEFAULT_ID,
    2: required data_movement_models.SecurityProtocol securityProtocol,
    3: optional list<string> globusGateKeeperEndPoint
}

/**
 * Unicore Job Submission
 *
 * unicoreEndPointURL:
 *  unicoreGateway End Point. The provider will query this service to fetch required service end points.
 * authenticationMode
 *  The authenticationMode defines the way certificate is fetched. 
*/
struct UnicoreJobSubmission {
    1: required string jobSubmissionInterfaceId = airavata_commons.DEFAULT_ID,
    2: required data_movement_models.SecurityProtocol securityProtocol,
    3: required string unicoreEndPointURL,
}

/**
* Provider name
**/
enum ProviderName {
    EC2,
    AWSEC2,
    RACKSPACE
}

/**
 * Cloud Job Submission
 *
 *
*/
struct CloudJobSubmission {
    1: required string jobSubmissionInterfaceId = airavata_commons.DEFAULT_ID,
    2: required data_movement_models.SecurityProtocol securityProtocol,
    3: required string nodeId,
    4: required string executableType,
    5: required ProviderName providerName,
    6: required string userAccountName
}

/**
 * Job Submission Interfaces
 *
 * jobSubmissionInterfaceId: The Job Submission Interface has to be previously registered and referenced here.
 *
 * priorityOrder:
 *  For resources with multiple interfaces, the priority order should be selected.
 *   Lower the numerical number, higher the priority
 *
*/
struct JobSubmissionInterface {
    1: required string jobSubmissionInterfaceId,
    2: required JobSubmissionProtocol jobSubmissionProtocol
    3: required i32 priorityOrder = 0,
}



/**
 * Computational Resource Description
 *
 * computeResourceId: Airavata Internal Unique Identifier to distinguish Compute Resource.
 *
 * hostName:
 *   Fully Qualified Host Name.
 *
 * hostAliases:
 *   Aliases if any.
 *
 * ipAddress:
 *   IP Addresses of the Resource.
 *
 * resourceDescription:
 *  A user friendly description of the resource.
 *
 * JobSubmissionProtocols:
 *  A computational resources may have one or more ways of submitting Jobs. This structure
 *    will hold all available mechanisms to interact with the resource.
 *  The key is the priority
 *
 * DataMovementProtocol:
 *  Option to specify a prefered data movement mechanism of the available options.
 *
 * fileSystems:
 *  Map of file systems type and the path.
 *
*/
struct ComputeResourceDescription {
//    1: required bool isEmpty = 0,
    1: required string computeResourceId = airavata_commons.DEFAULT_ID,
    2: required string hostName,
    3: optional list<string> hostAliases,
    4: optional list<string> ipAddresses,
    5: optional string resourceDescription,
    6: optional bool enabled,
    7: optional list<BatchQueue> batchQueues,
    8: optional map<FileSystems, string> fileSystems,
    9: optional list<JobSubmissionInterface> jobSubmissionInterfaces,
    10: optional list<data_movement_models.DataMovementInterface> dataMovementInterfaces,
    11: optional i32 maxMemoryPerNode,
    12: optional bool gatewayUsageReporting,
    13: optional string gatewayUsageModuleLoadCommand,
    14: optional string gatewayUsageExecutable,
    15: optional i32 cpusPerNode,
    16: optional i32 defaultNodeCount,
    17: optional i32 defaultCPUCount,
    18: optional i32 defaultWalltime,
}
