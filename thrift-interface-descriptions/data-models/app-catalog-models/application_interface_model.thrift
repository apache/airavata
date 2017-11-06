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

/**
 * This file describes the definitions of the Data Structures of Application interfaces. These interfaces are mapped
 *  to application mapping on various resources.
 *
*/
include "application_io_models.thrift"
include "../../airavata-apis/airavata_commons.thrift"

namespace java org.apache.airavata.model.appcatalog.appinterface
namespace php Airavata.Model.AppCatalog.AppInterface
namespace cpp apache.airavata.model.appcatalog.appinterface
namespace py airavata.model.appcatalog.appinterface


/**
 * Application Interface Description
 *
 * applicationModules:
 *   Associate all application modules with versions which interface is applicable to.
 *
 * applicationInputs:
 *   Inputs to be passed to the application
 *
 * applicationOutputs:
 *   Outputs generated from the application
 *
*/
struct ApplicationInterfaceDescription {
//    1: required bool isEmpty = 0,
    1: required string applicationInterfaceId = airavata_commons.DEFAULT_ID,
    2: required string applicationName,
    3: optional string applicationDescription,
    4: optional list<string> applicationModules,
    5: optional list<application_io_models.InputDataObjectType> applicationInputs,
    6: optional list<application_io_models.OutputDataObjectType> applicationOutputs,
    7: optional bool archiveWorkingDirectory = 0,
    8: optional bool hasOptionalFileInputs
}