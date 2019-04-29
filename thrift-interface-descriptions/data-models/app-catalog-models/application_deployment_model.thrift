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
 * This file describes the definitions of the Data Structures related to deployment of Application on
 *  computational resources.
 *
*/

include "../../airavata-apis/airavata_commons.thrift"
include "parallelism_model.thrift"

namespace java org.apache.airavata.model.appcatalog.appdeployment
namespace php Airavata.Model.AppCatalog.AppDeployment
namespace cpp apache.airavata.model.appcatalog.appdeployment
namespace py airavata.model.appcatalog.appdeployment

/**
 * Key Value pairs to be used to set environments
 *
 * name:
 *   Name of the environment variable such as PATH, LD_LIBRARY_PATH, NETCDF_HOME.
 *
 * value:
 *   Value of the environment variable to set
 *
 * envPathOrder:
 *   The order of the setting of the env variables when there are multiple env variables
*/
struct SetEnvPaths {
    1: required string name,
    2: required string value,
    3: optional i32 envPathOrder
}

/**
 * Job commands to be used in Pre Job, Post Job and Module Load Commands
 *
 * command:
 *   The actual command in string format
 *
 * commandOrder:
 *   Order of the command in the multiple command situation
*/
struct CommandObject {
    1: required string command,
    2: optional i32 commandOrder
}

/**
 * Application Module Information. A module has to be registered before registering a deployment.
 *
 * appModuleId: Airavata Internal Unique Job ID. This is set by the registry.
 *
 * appModuleName:
 *   Name of the application module.
 *
 * appModuleVersion:
 *   Version of the application.
 *
 * appModuleDescription:
 *    Descriprion of the Module
 *
*/
struct ApplicationModule {
    1: required string appModuleId = airavata_commons.DEFAULT_ID,
    2: required string appModuleName,
    3: optional string appModuleVersion,
    4: optional string appModuleDescription
}

/**
 * Application Deployment Description
 *
 * appDeploymentId: Airavata Internal Unique Job ID. This is set by the registry.
 *
 * appModuleName:
 *   Application Module Name. This has to be precise describing the binary.
 *
 * computeHostId:
 *   This ID maps application deployment to a particular resource previously described within Airavata.
 *   Example: Stampede is first registered and refered when registering WRF.
 *
 * moduleLoadCmd:
 *  Command string to load modules. This will be placed in the job submisison
 *  Ex: module load amber
 *
 * libPrependPaths:
 *  prepend to a path variable the value
 *
 * libAppendPaths:
 *  append to a path variable the value
 *
 * setEnvironment:
 *  assigns to the environment variable "NAME" the value
 *
*/
struct ApplicationDeploymentDescription {
//    1: required bool isEmpty = 0,
    1: required string appDeploymentId = airavata_commons.DEFAULT_ID,
    2: required string appModuleId,
    3: required string computeHostId,
    4: required string executablePath,
    5: required parallelism_model.ApplicationParallelismType parallelism = parallelism_model.ApplicationParallelismType.SERIAL,
	6: optional string appDeploymentDescription,
	7: optional list<CommandObject> moduleLoadCmds,
	8: optional list<SetEnvPaths> libPrependPaths,
	9: optional list<SetEnvPaths> libAppendPaths,
	10: optional list<SetEnvPaths> setEnvironment,
	11: optional list<CommandObject> preJobCommands,
	12: optional list<CommandObject> postJobCommands,
	13: optional string defaultQueueName,
	14: optional i32 defaultNodeCount,
	15: optional i32 defaultCPUCount,
	16: optional i32 defaultWalltime,
	17: optional bool editableByUser,
}
