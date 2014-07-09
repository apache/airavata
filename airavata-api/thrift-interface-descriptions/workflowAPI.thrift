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

include "airavataErrors.thrift"
include "airavataDataModel.thrift"
include "experimentModel.thrift"
include "workspaceModel.thrift"
include "computeResourceModel.thrift"
include "applicationDeploymentModel.thrift"
include "applicationInterfaceModel.thrift"
include "workflowDataModel.thrift"

namespace java org.apache.airavata.api.workflow
namespace php Airavata.API.Workflow
namespace cpp airavata.api.workflow
namespace perl AiravataWorkflowAPI
namespace py airavata.api.workflow
namespace js AiravataWorkflowAPI

const string AIRAVATA_API_VERSION = "0.13.0"

service Workflow {

  list<string> getAllWorkflows()
        throws (1: airavataErrors.InvalidRequestException ire,
                2: airavataErrors.AiravataClientException ace,
                3: airavataErrors.AiravataSystemException ase)
  
  workflowDataModel.Workflow getWorkflow (1: required string workflowTemplateId)
      throws (1: airavataErrors.InvalidRequestException ire,
              2: airavataErrors.AiravataClientException ace,
              3: airavataErrors.AiravataSystemException ase)

  void deleteWorkflow (1: required string workflowTemplateId)
      throws (1: airavataErrors.InvalidRequestException ire,
              2: airavataErrors.AiravataClientException ace,
              3: airavataErrors.AiravataSystemException ase)

  string registerWorkflow(1: required string workflowTemplateId, 2: required workflowDataModel.Workflow workflow)
        throws (1: airavataErrors.InvalidRequestException ire,
                2: airavataErrors.AiravataClientException ace,
                3: airavataErrors.AiravataSystemException ase)

  void updateWorkflow (1: required string workflowTemplateId, 2: required workflowDataModel.Workflow workflow)
        throws (1: airavataErrors.InvalidRequestException ire,
                2: airavataErrors.AiravataClientException ace,
                3: airavataErrors.AiravataSystemException ase)

  string getWorkflowTemplateId (1: required string workflowName)
        throws (1: airavataErrors.InvalidRequestException ire,
                2: airavataErrors.AiravataClientException ace,
                3: airavataErrors.AiravataSystemException ase)

  bool isWorkflowExistWithName(1: required string workflowName)
        throws (1: airavataErrors.InvalidRequestException ire,
                2: airavataErrors.AiravataClientException ace,
                3: airavataErrors.AiravataSystemException ase)
 }

