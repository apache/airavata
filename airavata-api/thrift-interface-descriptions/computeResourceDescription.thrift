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

namespace java org.apache.airavata.model.appcatalog.computeresource

/**
 * Enumeration of local resource job managers supported by Airavata
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
*/
enum ResourceJobManager {
    FORK,
    PBS,
    SLURM
}

/**
 * Enumeration of Airavata supported Job Submission Mechanisms for High Perforamance Computing Clusters.
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
    SSH,
    GRAM,
    UNICORE
}

/**
 * Enumeration of data movement supported by Airavata
 *
 * SCP:
 *  Job manager supporting the Portal Batch System (PBS) protocol. Some examples include TORQUE, PBSPro, Grid Engine.
 *
 * SFTP:
 *  The Simple Linux Utility for Resource Management is a open source workload manager.
 *
 * GridFTP:
 *  Globus File Transfer Protocol
 *
 * UNICORE_STORAGE_SERVICE:
 *  Storage Service Provided by Unicore
 *
*/
enum DataMovementProtocol {
    SCP,
    SFTP,
    GridFTP,
    UNICORE_STORAGE_SERVICE
}

/**
 * Enumeration of security authentication and authorization mechanisms supported by Airavata. This enumeration just
 *  describes the supported mechanism. The corresponding security credentials are registered with Airavata Credential
 *  store.
 *
 * USERNAME_PASSWORD:
 *  A User Name.
 *
 * SSH_KEYS:
 *  SSH Keys
 *
*/
enum SecurityProtocol {
    USERNAME_PASSWORD,
    SSH_KEYS,
    GSI,
    KERBEROS,
    OAUTH
}

struct SSHJobSubmission {
    1: required SecurityProtocol securityProtocol,
    2: required ResourceJobManager resourceJobManager,
    3: optional i32 sshPort = 22,

    //6: optional list<string> preJobCommands,
    //7: optional list<string> postJobCommands,
}

struct SCPDataMovement {
    1: required SecurityProtocol securityProtocol,
    2: optional i32 sshPort = 22,
}


struct ec2HostType
{
	1 : required list<string> imageID,
	2 : required list<string> instanceID,
}

struct globusHostType
{
	1 : required list<string> gridFTPEndPoint,
	2 : required list<string> globusGateKeeperEndPoint,
}

struct UnicoreHostType
{
	1 : required list<string> unicoreBESEndPoint,
}

/**
 * Computational Resource Description
 *
 * resourceId:
 *
 * hostName:
 *   Fully Qualified Host Name.
 *
 * ipAddress:
 *   IP Addresse of the Hostname.
 *
 * resourceDescription:
 *  A user friendly description of the hostname.
 *
 * preferedJobSubmissionProtocol:
 *  HPC resources may have multiple options to interact with the resource. This flag identified a prefered mechanism.
 *
 * preferedDataMovementProtocol:
 *  Option to specify a prefered data movement mechanism of the available options.
 *
*/

struct computeResourceDescription {
    1: required string resourceId,
    2: required string hostName,
    3: optional string ipAddress,
    4: optional string resourceDescription,
    5: optional JobSubmissionProtocol preferedJobSubmissionProtocol,
    6: optional DataMovementProtocol preferedDataMovementProtocol,
    7: optional SSHJobSubmission SSHJobSubmission,
}