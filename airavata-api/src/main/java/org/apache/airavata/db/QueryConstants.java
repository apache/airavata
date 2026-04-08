/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.db;

public interface QueryConstants {

    // Entity names as string literals to avoid compile-time dependency on service modules
    String APPLICATION_DEPLOYMENT_ENTITY = "ApplicationDeploymentEntity";
    String APPLICATION_MODULE_ENTITY = "ApplicationModuleEntity";
    String APPLICATION_INTERFACE_ENTITY = "ApplicationInterfaceEntity";
    String APPLICATION_INPUT_ENTITY = "ApplicationInputEntity";
    String APPLICATION_OUTPUT_ENTITY = "ApplicationOutputEntity";
    String GATEWAY_PROFILE_ENTITY = "GatewayProfileEntity";
    String COMPUTE_RESOURCE_PREFERENCE_ENTITY = "ComputeResourcePreferenceEntity";
    String COMPUTE_RESOURCE_ENTITY = "ComputeResourceEntity";
    String COMPUTE_RESOURCE_FILE_SYSTEM_ENTITY = "ComputeResourceFileSystemEntity";
    String JOB_MANAGER_COMMAND_ENTITY = "JobManagerCommandEntity";
    String PARALLELISM_COMMAND_ENTITY = "ParallelismCommandEntity";
    String GROUP_RESOURCE_PROFILE_ENTITY = "GroupResourceProfileEntity";
    String GROUP_COMPUTE_RESOURCE_PREF_ENTITY = "GroupComputeResourcePrefEntity";
    String BATCH_QUEUE_RESOURCE_POLICY_ENTITY = "BatchQueueResourcePolicyEntity";
    String COMPUTE_RESOURCE_POLICY_ENTITY = "ComputeResourcePolicyEntity";
    String USER_RESOURCE_PROFILE_ENTITY = "UserResourceProfileEntity";
    String USER_COMPUTE_RESOURCE_PREFERENCE_ENTITY = "UserComputeResourcePreferenceEntity";
    String PARSING_TEMPLATE_ENTITY = "ParsingTemplateEntity";
    String PARSER_ENTITY = "ParserEntity";

    // Orchestration entity names (string literals to avoid compile-time dependency on orchestration-service)
    String GATEWAY_ENTITY = "GatewayEntity";
    String NOTIFICATION_ENTITY = "NotificationEntity";
    String PROJECT_ENTITY = "ProjectEntity";
    String EXPERIMENT_ENTITY = "ExperimentEntity";
    String PROCESS_ENTITY = "ProcessEntity";
    String TASK_ENTITY = "TaskEntity";
    String JOB_ENTITY = "JobEntity";
    String QUEUE_STATUS_ENTITY = "QueueStatusEntity";
    String EXEC_STATUS_ENTITY = "ExecStatusEntity";
    String AIRAVATA_WORKFLOW_ENTITY = "AiravataWorkflowEntity";

    String FIND_USER_PROFILE_BY_USER_ID = "SELECT u FROM UserProfileEntity u " + "where u.userId LIKE :"
            + "userId" + " " + "AND u.gatewayId LIKE :"
            + "gatewayId" + "";

    String FIND_ALL_USER_PROFILES_BY_GATEWAY_ID =
            "SELECT u FROM UserProfileEntity u " + "where u.gatewayId LIKE :" + "gatewayId" + "";

    // Application Deployment Queries
    String FIND_APPLICATION_DEPLOYMENTS_FOR_GATEWAY_ID = "SELECT AD FROM " + APPLICATION_DEPLOYMENT_ENTITY + " AD "
            + "WHERE AD.gatewayId LIKE :" + DBConstants.ApplicationDeployment.GATEWAY_ID;
    String FIND_APPLICATION_DEPLOYMENTS_FOR_APPLICATION_MODULE_ID = "SELECT AD FROM " + APPLICATION_DEPLOYMENT_ENTITY
            + " AD " + "WHERE AD.appModuleId LIKE :" + DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID;
    String FIND_APPLICATION_DEPLOYMENTS_FOR_COMPUTE_HOST_ID = "SELECT AD FROM " + APPLICATION_DEPLOYMENT_ENTITY + " AD "
            + "WHERE AD.computeHostId LIKE :" + DBConstants.ApplicationDeployment.COMPUTE_HOST_ID;
    String GET_ALL_APPLICATION_DEPLOYMENTS = "SELECT AD FROM " + APPLICATION_DEPLOYMENT_ENTITY + " AD";
    String FIND_ACCESSIBLE_APPLICATION_DEPLOYMENTS =
            "SELECT AD FROM " + APPLICATION_DEPLOYMENT_ENTITY + " AD " + "WHERE AD.gatewayId LIKE :"
                    + DBConstants.ApplicationDeployment.GATEWAY_ID + " AND AD.appDeploymentId IN :"
                    + DBConstants.ApplicationDeployment.ACCESSIBLE_APPLICATION_DEPLOYMENT_IDS
                    + " AND AD.computeHostId IN :" + DBConstants.ApplicationDeployment.ACCESSIBLE_COMPUTE_HOST_IDS;
    String FIND_ACCESSIBLE_APPLICATION_DEPLOYMENTS_FOR_APP_MODULE =
            "SELECT AD FROM " + APPLICATION_DEPLOYMENT_ENTITY + " AD " + "WHERE AD.gatewayId LIKE :"
                    + DBConstants.ApplicationDeployment.GATEWAY_ID + " AND AD.appDeploymentId IN :"
                    + DBConstants.ApplicationDeployment.ACCESSIBLE_APPLICATION_DEPLOYMENT_IDS
                    + " AND AD.computeHostId IN :" + DBConstants.ApplicationDeployment.ACCESSIBLE_COMPUTE_HOST_IDS
                    + " AND AD.appModuleId = :" + DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID;

    // Application Module Queries
    String FIND_APPLICATION_MODULES_FOR_GATEWAY_ID = "SELECT AM FROM " + APPLICATION_MODULE_ENTITY + " AM "
            + "WHERE AM.gatewayId LIKE :" + DBConstants.ApplicationModule.GATEWAY_ID;
    String FIND_APPLICATION_MODULES_FOR_APPLICATION_MODULE_NAME = "SELECT AM FROM " + APPLICATION_MODULE_ENTITY + " AM "
            + "WHERE AM.appModuleName LIKE :" + DBConstants.ApplicationModule.APPLICATION_MODULE_NAME;
    String FIND_ACCESSIBLE_APPLICATION_MODULES = "SELECT AM FROM " + APPLICATION_MODULE_ENTITY
            + " AM " + "WHERE AM.gatewayId LIKE :"
            + DBConstants.ApplicationModule.GATEWAY_ID + " AND " + "EXISTS (SELECT 1 FROM "
            + APPLICATION_DEPLOYMENT_ENTITY + " AD "
            + "WHERE AM.appModuleId = AD.appModuleId AND AD.appDeploymentId IN :"
            + DBConstants.ApplicationDeployment.ACCESSIBLE_APPLICATION_DEPLOYMENT_IDS + " AND AD.computeHostId IN :"
            + DBConstants.ApplicationDeployment.ACCESSIBLE_COMPUTE_HOST_IDS + ")";

    // Application Interface Queries
    String FIND_APPLICATION_INTERFACES_FOR_GATEWAY_ID = "SELECT AI FROM " + APPLICATION_INTERFACE_ENTITY + " AI "
            + "WHERE AI.gatewayId LIKE :" + DBConstants.ApplicationInterface.GATEWAY_ID;
    String FIND_APPLICATION_INTERFACES_FOR_APPLICATION_NAME = "SELECT AI FROM " + APPLICATION_INTERFACE_ENTITY + " AI "
            + "WHERE AI.applicationName LIKE :" + DBConstants.ApplicationInterface.APPLICATION_NAME;
    String GET_ALL_APPLICATION_INTERFACES = "SELECT AI FROM " + APPLICATION_INTERFACE_ENTITY + " AI";

    // Application Inputs Queries
    String FIND_APPLICATION_INPUTS = "SELECT AI FROM " + APPLICATION_INPUT_ENTITY + " AI "
            + "WHERE AI.interfaceId LIKE :" + DBConstants.ApplicationInput.APPLICATION_INTERFACE_ID;

    // Application Outputs Queries
    String FIND_APPLICATION_OUTPUTS = "SELECT AI FROM " + APPLICATION_OUTPUT_ENTITY + " AI "
            + "WHERE AI.interfaceId LIKE :" + DBConstants.ApplicationOutput.APPLICATION_INTERFACE_ID;

    String FIND_ALL_GATEWAY_PROFILES = "SELECT G FROM " + GATEWAY_PROFILE_ENTITY + " G";
    String FIND_ALL_COMPUTE_RESOURCE_PREFERENCES = "SELECT DISTINCT CR FROM " + COMPUTE_RESOURCE_PREFERENCE_ENTITY
            + " CR " + "WHERE CR.gatewayId LIKE :" + DBConstants.ComputeResourcePreference.GATEWAY_ID;
    String FIND_ALL_STORAGE_RESOURCE_PREFERENCES = "SELECT DISTINCT S FROM " + "StoragePreferenceEntity" + " S "
            + "WHERE S.gatewayId LIKE :" + DBConstants.StorageResourcePreference.GATEWAY_ID;

    String FIND_COMPUTE_RESOURCE = "SELECT DISTINCT CR FROM " + COMPUTE_RESOURCE_ENTITY + " CR "
            + "WHERE CR.hostName LIKE :" + DBConstants.ComputeResource.HOST_NAME;
    String FIND_ALL_COMPUTE_RESOURCES = "SELECT CR FROM " + COMPUTE_RESOURCE_ENTITY + " CR";
    String GET_FILE_SYSTEM = "SELECT DISTINCT FS FROM " + COMPUTE_RESOURCE_FILE_SYSTEM_ENTITY + " FS "
            + "WHERE FS.computeResourceId LIKE :" + DBConstants.ComputeResource.COMPUTE_RESOURCE_ID;
    String GET_JOB_MANAGER_COMMAND = "SELECT DISTINCT JM FROM " + JOB_MANAGER_COMMAND_ENTITY + " JM "
            + "WHERE JM.resourceJobManagerId LIKE :" + DBConstants.ResourceJobManager.RESOURCE_JOB_MANAGER_ID;
    String GET_PARALLELISM_PREFIX = "SELECT DISTINCT PF FROM " + PARALLELISM_COMMAND_ENTITY + " PF "
            + "WHERE PF.resourceJobManagerId LIKE :" + DBConstants.ResourceJobManager.RESOURCE_JOB_MANAGER_ID;

    String FIND_ACCESSIBLE_GROUP_RESOURCE_PROFILES =
            "SELECT G FROM " + GROUP_RESOURCE_PROFILE_ENTITY + " G " + "WHERE G.gatewayId LIKE :"
                    + DBConstants.GroupResourceProfile.GATEWAY_ID + " AND G.groupResourceProfileId IN :"
                    + DBConstants.GroupResourceProfile.ACCESSIBLE_GROUP_RESOURCE_IDS;
    String FIND_ALL_GROUP_COMPUTE_PREFERENCES = "SELECT GC FROM " + GROUP_COMPUTE_RESOURCE_PREF_ENTITY
            + " GC " + "WHERE GC.groupResourceProfileId LIKE :"
            + DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID;
    String FIND_ALL_GROUP_BATCH_QUEUE_RESOURCE_POLICY = "SELECT BQ FROM "
            + BATCH_QUEUE_RESOURCE_POLICY_ENTITY + " BQ " + "WHERE BQ.groupResourceProfileId LIKE :"
            + DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID;
    String FIND_ALL_GROUP_COMPUTE_RESOURCE_POLICY = "SELECT CR FROM "
            + COMPUTE_RESOURCE_POLICY_ENTITY + " CR " + "WHERE CR.groupResourceProfileId LIKE :"
            + DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID;

    String GET_ALL_USER_RESOURCE_PROFILE = "SELECT URP FROM " + USER_RESOURCE_PROFILE_ENTITY + " URP";
    String GET_ALL_GATEWAY_ID = "SELECT DISTINCT URP FROM " + USER_RESOURCE_PROFILE_ENTITY + " URP "
            + "WHERE URP.gatewayId LIKE :" + DBConstants.UserResourceProfile.GATEWAY_ID;

    String GET_ALL_GATEWAYS = "SELECT G FROM " + GATEWAY_ENTITY + " G";
    String GET_GATEWAY_FROM_GATEWAY_NAME =
            "SELECT G FROM " + GATEWAY_ENTITY + " G " + "WHERE G.gatewayName LIKE :" + DBConstants.Gateway.GATEWAY_NAME;
    String FIND_GATEWAY_BY_INTERNAL_ID = "SELECT G FROM " + GATEWAY_ENTITY + " G "
            + "WHERE G.airavataInternalGatewayId LIKE :" + DBConstants.Gateway.AIRAVATA_INTERNAL_GATEWAY_ID;
    String GET_USER_GATEWAYS = "SELECT G FROM " + GATEWAY_ENTITY + " G " + "WHERE G.requesterUsername LIKE :"
            + DBConstants.Gateway.REQUESTER_USERNAME;
    String FIND_DUPLICATE_GATEWAY = "SELECT G FROM " + GATEWAY_ENTITY + " G "
            + "WHERE G.gatewayApprovalStatus IN :" + DBConstants.Gateway.GATEWAY_APPROVAL_STATUS + " "
            + "AND (G.gatewayId LIKE :" + DBConstants.Gateway.GATEWAY_ID + " "
            + "OR G.gatewayName LIKE :" + DBConstants.Gateway.GATEWAY_NAME + " "
            + "OR G.gatewayUrl LIKE :" + DBConstants.Gateway.GATEWAY_URL + ")";

    String GET_ALL_GATEWAY_NOTIFICATIONS = "SELECT N FROM " + NOTIFICATION_ENTITY + " N " + "WHERE N.gatewayId LIKE :"
            + DBConstants.Notification.GATEWAY_ID;

    String GET_ALL_PROJECTS_FOR_OWNER =
            "SELECT P FROM " + PROJECT_ENTITY + " P " + "WHERE P.owner LIKE :" + DBConstants.Project.OWNER;

    String GET_EXPERIMENTS_FOR_USER = "SELECT E FROM " + EXPERIMENT_ENTITY + " E " + "WHERE E.userName LIKE :"
            + DBConstants.Experiment.USER_NAME + " AND E.gatewayId = :"
            + DBConstants.Experiment.GATEWAY_ID;
    String GET_EXPERIMENTS_FOR_PROJECT_ID = "SELECT E FROM " + EXPERIMENT_ENTITY + " E " + "WHERE E.projectId LIKE :"
            + DBConstants.Experiment.PROJECT_ID + " AND E.gatewayId = :"
            + DBConstants.Experiment.GATEWAY_ID;
    String GET_EXPERIMENTS_FOR_GATEWAY_ID = "SELECT E FROM " + EXPERIMENT_ENTITY + " E " + "WHERE E.gatewayId LIKE :"
            + DBConstants.Experiment.GATEWAY_ID;

    String GET_PROCESS_FOR_EXPERIMENT_ID =
            "SELECT P FROM " + PROCESS_ENTITY + " P " + "WHERE P.experimentId = :" + DBConstants.Process.EXPERIMENT_ID;

    String GET_TASK_FOR_PARENT_PROCESS_ID = "SELECT T FROM " + TASK_ENTITY + " T " + "WHERE T.parentProcessId LIKE :"
            + DBConstants.Task.PARENT_PROCESS_ID;

    String GET_JOB_FOR_PROCESS_ID =
            "SELECT J FROM " + JOB_ENTITY + " J " + "WHERE J.processId LIKE :" + DBConstants.Job.PROCESS_ID;

    String GET_JOB_FOR_TASK_ID =
            "SELECT J FROM " + JOB_ENTITY + " J " + "WHERE J.taskId LIKE :" + DBConstants.Job.TASK_ID;

    String GET_JOB_FOR_JOB_ID = "SELECT J FROM " + JOB_ENTITY + " J " + "WHERE J.jobId LIKE :" + DBConstants.Job.JOB_ID;

    String GET_ALL_QUEUE_STATUS_MODELS = "SELECT QSM FROM " + QUEUE_STATUS_ENTITY + " QSM";

    String GET_ALL_USER_COMPUTE_RESOURCE_PREFERENCE = "SELECT UCRP FROM "
            + USER_COMPUTE_RESOURCE_PREFERENCE_ENTITY + " UCRP " + "WHERE UCRP.userId LIKE :"
            + DBConstants.UserComputeResourcePreference.USER_ID + " AND UCRP.gatewayId LIKE :"
            + DBConstants.UserComputeResourcePreference.GATEWAY_ID;

    String GET_ALL_GATEWAY_STORAGE_PREFERENCES = "SELECT SP FROM " + "StoragePreferenceEntity" + " SP "
            + "WHERE SP.gatewayId LIKE :" + DBConstants.StorageResourcePreference.GATEWAY_ID;

    String GET_ALL_USER_STORAGE_PREFERENCE =
            "SELECT USP FROM " + "UserStoragePreferenceEntity" + " USP " + "WHERE USP.userId LIKE :"
                    + DBConstants.UserStoragePreference.USER_ID + " AND USP.gatewayId LIKE :"
                    + DBConstants.UserStoragePreference.GATEWAY_ID;

    String FIND_ALL_CHILD_DATA_PRODUCTS = "SELECT DP FROM " + "DataProductEntity" + " DP "
            + "WHERE DP.parentProductUri LIKE :" + DBConstants.DataProduct.PARENT_PRODUCT_URI;

    String FIND_DATA_PRODUCT_BY_NAME = "SELECT DP FROM " + "DataProductEntity" + " DP "
            + "WHERE DP.gatewayId LIKE :"
            + DBConstants.DataProduct.GATEWAY_ID + " AND DP.ownerName LIKE :" + DBConstants.DataProduct.OWNER_NAME
            + " AND DP.productName LIKE :" + DBConstants.DataProduct.PRODUCT_NAME;

    String GET_WORKFLOW_FOR_EXPERIMENT_ID = "SELECT W FROM " + AIRAVATA_WORKFLOW_ENTITY + " W "
            + "WHERE W.experimentId LIKE :" + DBConstants.Workflow.EXPERIMENT_ID;

    String FIND_STORAGE_RESOURCE = "SELECT DISTINCT SR FROM " + "StorageResourceEntity" + " SR "
            + "WHERE SR.hostName LIKE :" + DBConstants.StorageResource.HOST_NAME;

    String FIND_ALL_STORAGE_RESOURCES = "SELECT SR FROM " + "StorageResourceEntity" + " SR";

    String FIND_ALL_AVAILABLE_STORAGE_RESOURCES =
            "SELECT SR FROM " + "StorageResourceEntity" + " SR " + "WHERE SR.enabled = TRUE";

    String FIND_PARSING_TEMPLATES_FOR_APPLICATION_INTERFACE_ID = "SELECT PT FROM " + PARSING_TEMPLATE_ENTITY + " PT "
            + "WHERE PT.applicationInterface = :" + DBConstants.ParsingTemplate.APPLICATION_INTERFACE_ID;

    String FIND_ALL_PARSING_TEMPLATES_FOR_GATEWAY_ID = "SELECT PT FROM " + PARSING_TEMPLATE_ENTITY + " PT "
            + "WHERE PT.gatewayId = :" + DBConstants.ParsingTemplate.GATEWAY_ID;

    String FIND_ALL_PARSERS_FOR_GATEWAY_ID =
            "SELECT P FROM " + PARSER_ENTITY + " P " + "WHERE P.gatewayId = :" + DBConstants.Parser.GATEWAY_ID;

    String FIND_QUEUE_STATUS = "SELECT  L  FROM " + QUEUE_STATUS_ENTITY
            + " L WHERE L.hostName LIKE :" + DBConstants.QueueStatus.HOST_NAME + " AND L.queueName LIKE :"
            + DBConstants.QueueStatus.QUEUE_NAME + " ORDER BY L.time DESC";

    String FIND_PROCESS_WITH_STATUS = "SELECT P FROM " + EXEC_STATUS_ENTITY + " P " + " where P.state = :"
            + DBConstants.ProcessStatus.STATE + " and P.entityType = 'PROCESS'";

    String GET_ALL_PROCESSES = "SELECT P FROM " + PROCESS_ENTITY + " P ";

    String DELETE_JOB_NATIVE_QUERY = "DELETE FROM JOB WHERE JOB_ID = ?1 AND TASK_ID = ?2";

    String FIND_JOB_COUNT_NATIVE_QUERY =
            "SELECT DISTINCT JS.ENTITY_ID FROM EXEC_STATUS JS WHERE JS.ENTITY_TYPE = 'JOB' AND JS.ENTITY_ID IN "
                    + "(SELECT J.JOB_ID FROM JOB J where J.PROCESS_ID IN "
                    + "(SELECT P.PROCESS_ID FROM PROCESS P  where P.EXPERIMENT_ID IN "
                    + "(SELECT E.EXPERIMENT_ID FROM EXPERIMENT E where E.GATEWAY_ID= ?1))) "
                    + "AND JS.STATE = ?2 and JS.TIME_OF_STATE_CHANGE > now() - interval ?3 minute";

    String FIND_AVG_TIME_UPTO_METASCHEDULER_NATIVE_QUERY =
            "SELECT AVG(difference) FROM(select es.TIME_OF_STATE_CHANGE AS esTime1, ps.TIME_OF_STATE_CHANGE as psTime1, "
                    + " TIMESTAMPDIFF(MICROSECOND, es.TIME_OF_STATE_CHANGE, ps.TIME_OF_STATE_CHANGE) AS difference FROM EXPERIMENT_STATUS es, "
                    + " EXPERIMENT_STATUS ps WHERE es.EXPERIMENT_ID IN (select EXPERIMENT_ID FROM EXPERIMENT WHERE GATEWAY_ID= ?1) "
                    + " AND ps.EXPERIMENT_ID=es.EXPERIMENT_ID AND es.STATE='CREATED' AND (ps.STATE='SCHEDULED' OR (ps.STATE='LAUNCHED ' "
                    + " AND ps.EXPERIMENT_ID NOT IN(select ps1.EXPERIMENT_ID FROM EXPERIMENT_STATUS ps1 WHERE ps1.STATE='SCHEDULED'))"
                    + " AND ps.TIME_OF_STATE_CHANGE <= ALL(select ps1.TIME_OF_STATE_CHANGE FROM EXPERIMENT_STATUS ps1 WHERE "
                    + " ps1.EXPERIMENT_ID=ps.EXPERIMENT_ID AND ps1.STATE='SCHEDULED'))  "
                    + " AND es.TIME_OF_STATE_CHANGE > now()-interval ?2 minute) abstract_t";

    String FIND_AVG_TIME_QUEUED_NATIVE_QUERY =
            "SELECT AVG(difference) FROM (SELECT es.TIME_OF_STATE_CHANGE AS esTime1, ps.TIME_OF_STATE_CHANGE as psTime1, "
                    + " TIMESTAMPDIFF(MICROSECOND, es.TIME_OF_STATE_CHANGE, ps.TIME_OF_STATE_CHANGE) AS difference FROM EXPERIMENT_STATUS es,"
                    + "  EXPERIMENT_STATUS ps WHERE es.EXPERIMENT_ID IN (select EXPERIMENT_ID FROM EXPERIMENT WHERE GATEWAY_ID=?1) "
                    + "  AND ps.EXPERIMENT_ID=es.EXPERIMENT_ID AND es.STATE='SCHEDULED' AND ps.STATE='LAUNCHED' "
                    + "  AND ps.TIME_OF_STATE_CHANGE >= ALL(SELECT ps1.TIME_OF_STATE_CHANGE FROM EXPERIMENT_STATUS ps1 "
                    + "  WHERE ps1.EXPERIMENT_ID=ps.EXPERIMENT_ID AND ps1.STATE='LAUNCHED') AND "
                    + "  es.TIME_OF_STATE_CHANGE <= ALL(SELECT ps1.TIME_OF_STATE_CHANGE FROM EXPERIMENT_STATUS ps1 "
                    + "  WHERE ps1.EXPERIMENT_ID=es.EXPERIMENT_ID AND ps1.STATE='SCHEDULED') AND es.TIME_OF_STATE_CHANGE > now()-interval ?2 minute)abstract_t";

    String FIND_AVG_TIME_HELIX_NATIVE_QUERY =
            "SELECT AVG(difference) FROM(SELECT es.TIME_OF_STATE_CHANGE AS esTime1, ps.TIME_OF_STATE_CHANGE as psTime1, "
                    + " TIMESTAMPDIFF(MICROSECOND, es.TIME_OF_STATE_CHANGE, ps.TIME_OF_STATE_CHANGE) AS difference from EXPERIMENT_STATUS es, "
                    + " EXEC_STATUS ps where ps.ENTITY_TYPE='JOB' AND es.EXPERIMENT_ID IN (SELECT EXPERIMENT_ID FROM EXPERIMENT WHERE GATEWAY_ID=?1) "
                    + " AND ps.ENTITY_ID IN(SELECT j.JOB_ID FROM JOB j where j.PROCESS_ID IN(SELECT DISTINCT p.PROCESS_ID  FROM PROCESS p "
                    + " WHERE p.EXPERIMENT_ID=es.EXPERIMENT_ID)) AND es.STATE='LAUNCHED' AND ps.STATE='SUBMITTED' "
                    + " AND ps.TIME_OF_STATE_CHANGE >= ALL(SELECT ps1.TIME_OF_STATE_CHANGE FROM EXEC_STATUS ps1 WHERE ps1.ENTITY_TYPE='JOB' AND ps1.ENTITY_ID=ps.ENTITY_ID "
                    + " AND ps1.STATE='SUBMITTED') AND es.TIME_OF_STATE_CHANGE >= ALL(SELECT es1.TIME_OF_STATE_CHANGE FROM EXPERIMENT_STATUS es1 "
                    + " WHERE es1.EXPERIMENT_ID=es.EXPERIMENT_ID AND es1.STATE='LAUNCHED') AND  es.TIME_OF_STATE_CHANGE > now()-interval ?2 minute) abstract_t";
}
