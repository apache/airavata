ut/*
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

namespace java org.apache.airavata.model.application.interface
namespace php Airavata.Model.Application.Interface

const string DEFAULT_ID = "DO_NOT_SET_AT_CLIENTS"


/**
 * Data Types Supported in Airavata.
 *
 *
*/
enum DataType{
	STRING,
	INTEGER,
	FLOAT,
	URI
}

/**
* Aplication Inputs
*
*/
struct InputDataObjectType {
    1: required string key,
    2: optional string value,
    3: optional DataType type,
    4: optional string metaData
    5: optional string applicationParameter,
    5: optional string applicationUIDescription
}

/**
* Aplication Outputs
*
*/
struct OutputDataObjectType {
    1: required string key,
    2: optional string value,
    3: optional DataType type,
    4: optional string metaData
}

/**
 * Application Interface Description
 *
 * resourceId:
 *
 * hostName:
 *   Fully Qualified Host Name.
 *
 * ipAddress:
 *   IP Addresse of the Hostname.
 *
 * resourceDescription:
 *  A user friendly description of the hostname.
 *
 * JobSubmissionProtocols:
 *  A computational resources may have one or more ways of submitting Jobs. This structure
 *  will hold all available mechanisms to interact with the resource.
 *
 * DataMovementProtocol:
 *  Option to specify a prefered data movement mechanism of the available options.
 *
*/
struct ApplicationInterfaceDescription {
    1: required bool isEmpty = 0,
    2: required string applicationInterfaceId = DEFAULT_ID,
    3: required string applicationName,
    7: optional list<InputDataObjectType> applicationInputs,
    8: optional list<outputDataObjectType> applicationOutputs,
}