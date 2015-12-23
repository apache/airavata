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

  namespace java org.apache.airavata.model.file
  namespace php Airavata.Model.File
  namespace cpp apache.airavata.model.file
  namespace py apache.airavata.model.file

  enum StorageResourceProtocol{
        SCP,SFTP,HTTP,HTTPS,GridFTP,LOCAL
  }

  enum FileNodeTypes{
    DIRECTORY,
    FILE
  }

  enum FileTransferMode{
      SYNC,ASYNC
  }

  enum FileTransferStatus{
      CREATED, QUEUED, RUNNING, COMPLETED, FAILED
  }

  struct FileTransferRequest{
      1: optional string transferId,
      2: optional string srcHostname,
      3: optional string srcLoginName,
      4: optional i64 srcPort,
      5: optional StorageResourceProtocol srcProtocol,
      6: optional string srcFilePath,
      7: optional string srcHostCredToken,
      8: optional string destHostname,
      9: optional string destLoginName,
     10: optional i64 destPort,
     11: optional StorageResourceProtocol destProtocol,
     12: optional string destFilePath,
     13: optional string destHostCredToken,
     14: optional FileTransferMode fileTransferMode,
     15: optional FileTransferStatus transferStatus,
     16: optional i64 fileSize,
     17: optional i64 transferTime,
     18: optional i64 createdTime,
     19: optional i64 lastModifiedType
  }

  struct FileNode {
      1: optional FileNodeTypes type,
      2: optional i64 size,
      3: optional string nativeType,
      4: optional string name,
      5: optional string path,
      6: optional string storageHostName,
      7: optional i64 lastModifiedType,
      8: optional i64 createdTime
  }