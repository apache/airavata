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

  namespace java org.apache.airavata.model.data.metadata
  namespace php Airavata.Model.data.metadata
  namespace cpp apache.airavata.model.data.metadata
  namespace py apache.airavata.model.data.metadata

  enum MetadataType{
    FILE, COLLECTION
  }

  struct MetadataModel{
    1: optional string metadataId,
    2: optional string gatewayId,
    3: optional string username,
    4: optional list<string> sharedUsers,
    5: optional bool sharedPublic,
    6: optional string userFriendlyName,
    7: optional string userFriendlyDescription,
    8: optional MetadataType metadataType,
    9: optional string associatedEntityId,
   10: optional map<string,string> customInformation
  }