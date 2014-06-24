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

include "applicationCatalogDataModel.thrift"

namespace java org.apache.airavata.gfac.workspace.experiment
namespace php Airavata.Model.Workspace.Experiment

/*
 * This file describes the definitions of the Gfac Framework level Experiment Data Structures. Each of the
 *   language specific Airavata Client SDK's will translate this neutral data model into an
 *   appropriate form for passing to the Airavata Server Execution API Calls.
 *
 *   This data-model will not be visible to the outside users but it will be used inside GFAc to recover
 *   the failed jobs or hanged jobs.
 *
*/

const string DEFAULT_ID = "DO_NOT_SET_AT_CLIENTS"
const string DEFAULT_PROJECT_NAME = "DEFAULT"
const string SINGLE_APP_NODE_NAME = "SINGLE_APP_NODE"

enum GfacExperimentState {
    INHANDLERSINVOKING,
    INHANDLERSINVOKED,
    PROVIDERINVOKING,
    PROVIDERINVOKED,
    OUTHANDLERSINVOKING,
    OUTHANDLERSINVOKED,
    COMPLETED,
    FAILED,
    UNKNOWN
}

struct GfacExperimentStatus {
    1: required GfacExperimentState gfacExperimentState,
    2: optional i64 timeOfStateChange
}
