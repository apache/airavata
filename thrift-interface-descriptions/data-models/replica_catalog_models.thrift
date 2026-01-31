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
  namespace java org.apache.airavata.thriftapi.model
  namespace php Airavata.Model.Data.Replica
  namespace cpp apache.airavata.model.data.replica
  namespace py airavata.model.data.replica

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

enum DataProductType {
    FILE,
    COLLECTION,
}

// THRIFT-3783 Put DataReplicaLocationModel before DataProductModel since DataProductModel references it
struct DataReplicaLocationModel {
    1: optional string replicaId,
    2: optional string productUri,
    3: optional string replicaName,
    4: optional string replicaDescription,
    5: optional i64 creationTime,
    6: optional i64 lastModifiedTime,
    7: optional i64 validUntilTime,
    8: optional ReplicaLocationCategory replicaLocationCategory,
    9: optional ReplicaPersistentType replicaPersistentType,
    10: optional string storageResourceId,
    11: optional string filePath,
    12: optional map<string, string> replicaMetadata
}

struct DataProductTag {
    1: optional string id,
    2: optional string name,
    3: optional string color
}

struct DataProductModel {
    1: optional string productUri,
    2: optional string gatewayId,
    3: optional string parentProductUri,
    4: optional string productName,
    5: optional string productDescription,
    6: optional string ownerName,
    7: optional DataProductType dataProductType,
    8: optional i32 productSize,
    9: optional i64 creationTime,
    10: optional i64 lastModifiedTime,
    11: optional map<string, string> productMetadata,
    12: optional list<DataReplicaLocationModel> replicaLocations,
    13: optional string primaryStorageResourceId,
    14: optional string primaryFilePath,
    15: optional string status,
    16: optional string privacy,
    17: optional string scope,
    18: optional string ownerId,
    19: optional list<string> authors,
    20: optional list<DataProductTag> tags,
    21: optional string format,
    22: optional string headerImage,
    23: optional i64 updatedAt,
}
