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

  namespace java org.apache.airavata.model.file.transfer
  namespace php Airavata.Model.File.Transfer
  namespace cpp apache.airavata.model.file.transfer
  namespace py apache.airavata.model.file.transfer

  enum StorageResourceProtocol{
        SCP,SFTP,HTTP,HTTPS,GridFTP,LOCAL
  }

  enum LSEntryType{
    DIRECTORY,
    FILE
  }

  enum FileTransferMode{
      SYNC,ASYNC
  }

  enum FileTransferStatus{
      CREATED, QUEUED, RUNNING, COMPLETED, FAILED
  }

  struct FileTransferRequestModel{
      1: optional string transferId,
      2: optional string gatewayId,
      3: optional string username,
      4: optional string srcHostname,
      5: optional string srcLoginName,
      6: optional i64 srcPort,
      7: optional StorageResourceProtocol srcProtocol,
      8: optional string srcFilePath,
      9: optional string srcHostCredToken,
     10: optional string destHostname,
     11: optional string destLoginName,
     12: optional i64 destPort,
     13: optional StorageResourceProtocol destProtocol,
     14: optional string destFilePath,
     15: optional string destHostCredToken,
     16: optional FileTransferMode fileTransferMode,
     17: optional FileTransferStatus transferStatus,
     18: optional i64 fileSize,
     19: optional i64 transferTime,
     20: optional i64 createdTime,
     21: optional i64 lastModifiedType,
     22: optional list<string> callbackEmails
  }

  struct LSEntryModel {
      1: optional LSEntryType type,
      2: optional i64 size,
      3: optional string nativeType,
      4: optional string name,
      5: optional string path,
      6: optional string storageHostName,
      7: optional i64 lastModifiedType,
      8: optional i64 createdTime
  }