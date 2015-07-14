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


namespace java org.apache.airavata.model
namespace php Airavata.Model
namespace py apache.airavata.model.workflow

include "application_io_models.thrift"
include "airavata_commons.thrift"

/*
 * This file describes the definitions of the Airavata Execution Data Structures. Each of the
 *   language specific Airavata Client SDK's will translate this neutral data model into an
 *   appropriate form for passing to the Airavata Server Execution API Calls.
*/


struct Workflow {
    1: required string templateId = airavata_commons.DEFAULT_ID,
    2: required string name,
    3: optional string graph,
    4: optional binary image,
    5: optional list<application_io_models.InputDataObjectType> workflowInputs,
    6: optional list<application_io_models.OutputDataObjectType> workflowOutputs
}
