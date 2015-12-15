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

namespace java org.apache.airavata.model.workspace
namespace php Airavata.Model.Workspace
namespace cpp apache.airavata.model.workspace
namespace py apache.airavata.model.workspace

/*
 * This file describes the definitions of the Airavata Workspace. The workspace is a container for all user data
 *   organized as Projects and Experiment within them.
 *
 * The Experiment data model is divided into 6 categories: experiment metadata, experiment configuration
 *   data, experiment generated data, experiment monitoring data, provenance data and error handling data.
 *
 *
*/

struct Group {
    1: required string groupName,
    2: optional string description
}

struct Project {
    1: required string projectID = airavata_commons.DEFAULT_ID,
    2: required string owner,
    3: required string name,
    4: optional string description
    5: optional i64 creationTime
    6: optional list<string> sharedUsers,
    7: optional list<string> sharedGroups
}

struct User {
    1: required string userName,
    2: optional list<Group> groupList
}

struct Gateway {
    1: required string gatewayId,
    2: optional string gatewayName,
    3: optional string domain,
    4: optional string emailAddress
}