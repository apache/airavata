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

namespace java org.apache.airavata.model.data.movement
namespace php Airavata.Model.Data.Movement
namespace cpp apache.airavata.model.data.movement
namespace py airavata.model.data.movement

enum DMType {
    COMPUTE_RESOURCE,
    STORAGE_RESOURCE
}

/**
 * Enumeration of security sshKeyAuthentication and authorization mechanisms supported by Airavata. This enumeration just
 *  describes the supported mechanism. The corresponding security credentials are registered with Airavata Credential
 *  store.
 *
 * USERNAME_PASSWORD:
 *  A User Name.
 *
 * SSH_KEYS:
 *  SSH Keys
 *
 * FIXME: Change GSI to a more precise generic security protocol - X509
 *
*/
enum SecurityProtocol {
    USERNAME_PASSWORD,
    SSH_KEYS,
    GSI,
    KERBEROS,
    OAUTH,
    LOCAL
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
    LOCAL,
    SCP,
    SFTP,
    GridFTP,
    UNICORE_STORAGE_SERVICE
}

/**
 * Data Movement through Secured Copy
 *
 * alternativeSCPHostName:
 *  If the login to scp is different than the hostname itself, specify it here
 *
 * sshPort:
 *  If a non-default port needs to used, specify it.
*/
struct SCPDataMovement {
    1: required string dataMovementInterfaceId = airavata_commons.DEFAULT_ID,
    2: required SecurityProtocol securityProtocol,
    3: optional string alternativeSCPHostName,
    4: optional i32 sshPort = 22
}

/**
 * Data Movement through GridFTP
 *
 * alternativeSCPHostName:
 *  If the login to scp is different than the hostname itself, specify it here
 *
 * sshPort:
 *  If a non-default port needs to used, specify it.
*/
struct GridFTPDataMovement {
    1: required string dataMovementInterfaceId = airavata_commons.DEFAULT_ID,
    2: required SecurityProtocol securityProtocol,
    3: required list<string>  gridFTPEndPoints
}

/**
 * Data Movement through UnicoreStorage
 *
 * unicoreEndPointURL:
 *  unicoreGateway End Point. The provider will query this service to fetch required service end points.
*/
struct UnicoreDataMovement {
    1: required string dataMovementInterfaceId = airavata_commons.DEFAULT_ID,
    2: required SecurityProtocol securityProtocol,
    3: required string unicoreEndPointURL
}

/**
 * LOCAL
 *
 * alternativeSCPHostName:
 *  If the login to scp is different than the hostname itself, specify it here
 *
 * sshPort:
 *  If a non-defualt port needs to used, specify it.
*/
struct LOCALDataMovement {
    1: required string dataMovementInterfaceId = airavata_commons.DEFAULT_ID,
}

/**
 * Data Movement Interfaces
 *
 * dataMovementInterfaceId: The Data Movement Interface has to be previously registered and referenced here.
 *
 * priorityOrder:
 *  For resources with multiple interfaces, the priority order should be selected.
 *   Lower the numerical number, higher the priority
 *
*/
struct DataMovementInterface {
    1: required string dataMovementInterfaceId,
    2: required DataMovementProtocol dataMovementProtocol,
    3: required i32 priorityOrder = 0,
    4: optional i64 creationTime,
    5: optional i64 updateTime,
    6: optional string storageResourceId
}



