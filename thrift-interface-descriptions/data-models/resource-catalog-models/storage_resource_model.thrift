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
include "data_movement_models.thrift"

namespace java org.apache.airavata.model.appcatalog.storageresource
namespace php Airavata.Model.AppCatalog.StorageResource
namespace cpp apache.airavata.model.appcatalog.storageresource
namespace py airavata.model.appcatalog.storageresource

/**
 * Storage Resource Description
 *
 * storageResourceId: Airavata Internal Unique Identifier to distinguish Compute Resource.
 *
 * hostName:
 *   Fully Qualified Host Name.
 *
 * storageResourceDescription:
 *  A user friendly description of the resource.
 *
 *
 * DataMovementProtocol:
 *  Option to specify a prefered data movement mechanism of the available options.
 *
 *
*/
struct StorageResourceDescription {
    1: required string storageResourceId = airavata_commons.DEFAULT_ID,
    2: required string hostName,
    3: optional string storageResourceDescription,
    4: optional bool enabled,
    5: optional list<data_movement_models.DataMovementInterface> dataMovementInterfaces,
    6: optional i64 creationTime,
    7: optional i64 updateTime,
}
