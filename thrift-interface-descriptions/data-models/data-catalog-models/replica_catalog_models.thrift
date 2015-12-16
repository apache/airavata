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

 include "../resource-catalog-models/data_movement_models.thrift"

  namespace java org.apache.airavata.model.data.resource
  namespace php Airavata.Model.Data.Resource
  namespace cpp apache.airavata.model.data.resource
  namespace py apache.airavata.model.data.resource

enum ReplicaLocationCategory {
    GATEWAY_DATA_STORE,
    COMPUTE_RESOURCE,
    LONG_TERM_STORAGE_RESOURCE,
    OTHER
}

enum ReplicaPersistentType {
    TRANSIENT,
    PERSISTENT
}

enum DataResourceType {
    COLLECTION,
    FILE
}

struct DataResourceModel {
    1: optional string resourceId,
    2: optional string gatewayId,
    3: optional string parentResourceId,
    4: optional string resourceName,
    5: optional string resourceDescription,
    6: optional string ownerName,
    7: optional DataResourceType dataResourceType,
    8: optional i32 resourceSize,
    9: optional i64 creationTime,
    10: optional i64 lastModifiedTime,
    11: optional map<string, string> resourceMetadata,
    12: optional list<DataReplicaLocationModel> replicaLocations,
    13: optional list<DataResourceModel> childResources
}

struct DataReplicaLocationModel {
    1: optional string replicaId,
    2: optional string resourceId,
    3: optional string replicaName,
    4: optional string replicaDescription,
    5: optional i64 creationTime,
    6: optional i64 lastModifiedTime,
    7: optional ReplicaLocationCategory replicaLocationCategory,
    8: optional ReplicaPersistentType replicaPersistentType,
    9: optional string storageResourceId,
    10: optional string storageResourceHostName,
    11: optional data_movement_models.DataMovementProtocol dataMovementProtocol,
    12: optional i64 hostPort,
    13: optional string filePath,
    14: optional string replicaUrl,
    15: optional map<string, string> replicaMetadata
}
