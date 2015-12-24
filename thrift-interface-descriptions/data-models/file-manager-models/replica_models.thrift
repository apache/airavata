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

namespace java org.apache.airavata.model.file.replica
namespace php Airavata.Model.File.Replica
namespace cpp apache.airavata.model.file.replica
namespace py apache.airavata.model.file.replica

enum FileModelType{
    FILE, DIRECTORY
}

enum StorageResourceType {
    GATEWAY_DATA_STORE,
    BACKUP_GATEWAY_DATA_STORE,
    COMPUTE_RESOURCE,
    LONG_TERM_STORAGE_RESOURCE,
    OTHER
}

enum ReplicaPersistentType {
    TRANSIENT,
    PERSISTENT
}

struct FileCollectionModel{
    1: optional string collectionId,
    2: optional string gatewayId,
    3: optional string username,
    4: optional list<string> sharedUsers,
    5: optional bool sharedPublic,
    6: optional string collectionName,
    7: optional string collectionDescription,
    8: optional list<string> fileIdList
}

struct FileModel {
    1: optional string fileId,
    2: optional string gatewayId,
    3: optional string username,
    4: optional list<string> sharedUsers,
    5: optional bool sharedPublic,
    6: optional string fileName,
    7: optional string fileDescription,
    8: optional string sha256Checksum,
    9: optional FileModelType fileType,
   10: optional i32 fileSize,
   11: optional string dataType,
   12: optional i64 creationTime,
   13: optional i64 lastModifiedTime,
   14: optional list<FileReplicaModel> fileReplicas
}

struct FileReplicaModel{
    1: optional string replicaName,
    2: optional string replicaDescription,
    3: optional string storageHostname,
    4: optional string storageResourceId,
    5: optional string filePath,
    6: optional i64 creationTime,
    7: optional i64 validUntilTime,
    8: optional StorageResourceType storageResourceType,
    9: optional ReplicaPersistentType replicaPersistentType
}