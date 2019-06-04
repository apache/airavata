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
 include "../app-catalog-models/application_io_models.thrift"
 include "scheduling_model.thrift"
 include "status_models.thrift"
 include "process_model.thrift"
 include "airavata_workflow_model.thrift"

 namespace java org.apache.airavata.model.experiment
 namespace php Airavata.Model.Experiment
 namespace cpp apache.airavata.model.experiment
 namespace py airavata.model.experiment

enum ExperimentType {
    SINGLE_APPLICATION,
    WORKFLOW
}

enum ExperimentSearchFields {
    EXPERIMENT_NAME,
    EXPERIMENT_DESC,
    APPLICATION_ID,
    FROM_DATE,
    TO_DATE,
    STATUS,
    PROJECT_ID,
    USER_NAME,
    JOB_ID,
}

enum ProjectSearchFields {
    PROJECT_NAME,
    PROJECT_DESCRIPTION
}

/**
 * A structure holding the experiment configuration.
 *
 *
*/
struct UserConfigurationDataModel {
    1: required bool airavataAutoSchedule = 0,
    2: required bool overrideManualScheduledParams = 0,
    3: optional bool shareExperimentPublicly = 0,
    4: optional scheduling_model.ComputationalResourceSchedulingModel computationalResourceScheduling,
    5: optional bool throttleResources = 0,
    6: optional string userDN,
    7: optional bool generateCert = 0,
    8: optional string storageId;
    9: optional string experimentDataDir;
    10: optional bool useUserCRPref;
    11: optional string groupResourceProfileId
}

/**
 * A structure holding the experiment metadata and its child models.
 *
 * userName:
 *   The user name of the targeted gateway end user on whose behalf the experiment is being created.
 *     the associated gateway identity can only be inferred from the security hand-shake so as to avoid
 *     authorized Airavata Clients mimicking an unauthorized request. If a gateway is not registered with
 *     Airavata, an authorization exception is thrown.
 *
 * experimentName:
 *   The name of the experiment as defined by the user. The name need not be unique as uniqueness is enforced
 *      by the generated experiment id.
 *
 * experimentDescription:
 *    The verbose description of the experiment. This is an optional parameter.
*/

struct ExperimentModel {
    1: required string experimentId = airavata_commons.DEFAULT_ID,
    2: required string projectId,
    3: required string gatewayId,
    4: required ExperimentType experimentType = ExperimentType.SINGLE_APPLICATION,
    5: required string userName,
    6: required string experimentName,
    7: optional i64 creationTime,
    8: optional string description,
    9: optional string executionId,
    10: optional string gatewayExecutionId,
    11: optional string gatewayInstanceId,
    12: optional bool enableEmailNotification,
    13: optional list<string> emailAddresses,
    14: optional UserConfigurationDataModel userConfigurationData,
    15: optional list<application_io_models.InputDataObjectType> experimentInputs,
    16: optional list<application_io_models.OutputDataObjectType> experimentOutputs,
    17: optional list<status_models.ExperimentStatus> experimentStatus,
    18: optional list<airavata_commons.ErrorModel> errors,
    19: optional list<process_model.ProcessModel> processes,
    20: optional airavata_workflow_model.AiravataWorkflow workflow
    }

struct ExperimentSummaryModel {
    1: required string experimentId,
    2: required string projectId,
    3: required string gatewayId,
    4: optional i64 creationTime,
    5: required string userName,
    6: required string name,
    7: optional string description,
    8: optional string executionId,
    9: optional string resourceHostId,
   10: optional string experimentStatus,
   12: optional i64 statusUpdateTime
}

struct ExperimentStatistics {
    1: required i32 allExperimentCount,
    2: required i32 completedExperimentCount,
    3: optional i32 cancelledExperimentCount,
    4: required i32 failedExperimentCount,
    5: required i32 createdExperimentCount,
    6: required i32 runningExperimentCount,
    7: required list<ExperimentSummaryModel> allExperiments,
    8: optional list<ExperimentSummaryModel> completedExperiments,
    9: optional list<ExperimentSummaryModel> failedExperiments,
    10: optional list<ExperimentSummaryModel> cancelledExperiments,
    11: optional list<ExperimentSummaryModel> createdExperiments,
    12: optional list<ExperimentSummaryModel> runningExperiments,
}
