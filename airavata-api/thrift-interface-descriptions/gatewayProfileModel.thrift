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

namespace java org.apache.airavata.model.appcatalog
namespace php Airavata.Model.AppCatalog

const string DEFAULT_ID = "DO_NOT_SET_AT_CLIENTS"

/**
 * Enumeration of local resource job managers supported by Airavata
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
enum ResourceJobManager {
    FORK,
    PBS,
    UGE,
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
    GSISSH,
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



struct SCPDataMovement {
    1: required string dataMovementDataID = DEFAULT_ID,
    2: required SecurityProtocol securityProtocol,
    3: optional i32 sshPort = 22
}

struct GridFTPDataMovement {
    1: required string dataMovementDataID = DEFAULT_ID,
    2: required SecurityProtocol securityProtocol,
    3: required list<string>  gridFTPEndPoint
}

struct SSHJobSubmission {
    1: required string jobSubmissionDataID = DEFAULT_ID,
    2: required ResourceJobManager resourceJobManager,
    3: optional i32 sshPort = 22
}

struct GlobusJobSubmission {
    1: required string jobSubmissionDataID = DEFAULT_ID,
    2: required SecurityProtocol securityProtocol,
    3: required ResourceJobManager resourceJobManager,
    4: optional list<string> globusGateKeeperEndPoint
}

struct GSISSHJobSubmission {
    1: required string jobSubmissionDataID = DEFAULT_ID,
    2: required ResourceJobManager resourceJobManager,
    3: optional i32 sshPort = 22,
    4: optional set<string> exports,
    5: optional list<string> preJobCommands,
    6: optional list<string> postJobCommands,
    7: optional string installedPath,
    8: optional string monitorMode
}

/**
 * Gateway Profile
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
 * JobSubmissionProtocols:
 *  A computational resources may have one or more ways of submitting Jobs. This structure
 *  will hold all available mechanisms to interact with the resource.
 *
 * DataMovementProtocol:
 *  Option to specify a prefered data movement mechanism of the available options.
 *
*/


struct GatewayProfile {
    1: required string gatewayID = DEFAULT_ID,
    2: required string gatewayName,
    3: optional string gatewayDescription,
    4: optional string preferedResource
}
