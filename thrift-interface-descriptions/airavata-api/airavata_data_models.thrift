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

include "airavata_commons.thrift"
include "workspace_model.thrift"
include "airavata_errors.thrift"
include "messaging_events.thrift"
include "security_model.thrift"
include "experiment_model.thrift"
include "job_model.thrift"
include "task_model.thrift"
include "process_model.thrift"
include "scheduling_model.thrift"
include "status_models.thrift"
include "data_movement_models.thrift"
include "data_resource_models.thrift"

namespace java org.apache.airavata.model
namespace php Airavata.Model
namespace cpp apache.airavata.model
namespace py apache.airavata.model

/*
 * This file describes the definitions of the Airavata Execution Data Structures. Each of the
 *   language specific Airavata Client SDK's will translate this neutral data model into an
 *   appropriate form for passing to the Airavata Server Execution API Calls.
*/



