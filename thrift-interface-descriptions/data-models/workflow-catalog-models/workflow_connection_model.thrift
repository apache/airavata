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

include "../../airavata-apis/airavata_commons.thrift"
include "core-models/component_type.thrift"
include "core-models/data_block_model.thrift"

namespace java org.apache.airavata.model.workflow
namespace php Airavata.Model.Workflow
namespace cpp apache.airavata.model.workflow
namespace py airavata.model.workflow

struct WorkflowConnection {
    1:  required string id = airavata_commons.DEFAULT_ID,
    2:  required bool belongsToMainWorkflow,
    3:  required component_type.ComponentType fromType
    4:  required string fromId,
    5:  required string fromOutputName,
    6:  required component_type.ComponentType toType,
    7:  required string toId,
    8:  required string toInputName
    9:  optional i64 createdAt,
    10: optional i64 updatedAt
}
