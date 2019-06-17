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

 namespace java org.apache.airavata.model.group
 namespace php Airavata.Model.Group
 namespace cpp apache.airavata.model.group
 namespace py airavata.model.group


enum ResourceType {
    PROJECT,
    EXPERIMENT,
    DATA,
    APPLICATION_DEPLOYMENT,
    GROUP_RESOURCE_PROFILE,
    CREDENTIAL_TOKEN,
    OTHER
}

enum ResourcePermissionType {
    WRITE,
    READ,
    OWNER,
    MANAGE_SHARING
}

struct GroupModel{
    1: optional string id,
    2: optional string name,
    3: optional string ownerId,
    4: optional string description,
    5: optional list<string> members,
    /**
     * Note: each admin must also be a member of the group.
     */
    6: optional list<string> admins,
}