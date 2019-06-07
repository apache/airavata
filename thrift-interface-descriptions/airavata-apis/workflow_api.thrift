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
 * Application Programming Interface definition for Apache Airavata Services.
 *   this parent thrift file is contains all service interfaces. The data models are 
 *   described in respective thrift files.
*/

include "airavata_errors.thrift"
include "airavata_data_models.thrift"
include "experiment_model.thrift"
include "workspace_model.thrift"
include "compute_resource_model.thrift"
include "application_deployment_model.thrift"
include "application_interface_model.thrift"
include "workflow_data_model.thrift"
include "../base-api/base_api.thrift"

namespace java org.apache.airavata.api.workflow
namespace php Airavata.API.Workflow
namespace cpp airavata.api.workflow
namespace perl AiravataWorkflowAPI
namespace py airavata.api.workflow
namespace js AiravataWorkflowAPI

const string WORKFLOW_API_VERSION = "0.18.0"

service Workflow extends base_api.BaseAPI {

  list<string> getAllWorkflows()
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase)
  
  workflow_data_model.Workflow getWorkflow (1: required string workflowTemplateId)
      throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase)

  void deleteWorkflow (1: required string workflowTemplateId)
      throws (1: airavata_errors.InvalidRequestException ire,
              2: airavata_errors.AiravataClientException ace,
              3: airavata_errors.AiravataSystemException ase)

  string registerWorkflow(1: required workflow_data_model.Workflow workflow)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase)

  void updateWorkflow (1: required string workflowTemplateId, 2: required workflow_data_model.Workflow workflow)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase)

  string getWorkflowTemplateId (1: required string workflowName)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase)

  bool isWorkflowExistWithName(1: required string workflowName)
        throws (1: airavata_errors.InvalidRequestException ire,
                2: airavata_errors.AiravataClientException ace,
                3: airavata_errors.AiravataSystemException ase)
 }

