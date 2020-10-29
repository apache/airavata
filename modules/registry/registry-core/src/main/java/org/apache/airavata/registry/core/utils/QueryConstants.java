/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.registry.core.utils;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.registry.core.entities.airavataworkflowcatalog.AiravataWorkflowEntity;
import org.apache.airavata.registry.core.entities.appcatalog.*;
import org.apache.airavata.registry.core.entities.expcatalog.*;
import org.apache.airavata.registry.core.entities.replicacatalog.DataProductEntity;

public interface QueryConstants {

    String FIND_USER_PROFILE_BY_USER_ID = "SELECT u FROM UserProfileEntity u " +
            "where u.userId LIKE :" + UserProfile._Fields.USER_ID.getFieldName() + " " +
            "AND u.gatewayId LIKE :"+ UserProfile._Fields.GATEWAY_ID.getFieldName() + "";

    String FIND_ALL_USER_PROFILES_BY_GATEWAY_ID = "SELECT u FROM UserProfileEntity u " +
            "where u.gatewayId LIKE :"+ UserProfile._Fields.GATEWAY_ID.getFieldName() + "";

    // Application Deployment Queries
    String FIND_APPLICATION_DEPLOYMENTS_FOR_GATEWAY_ID = "SELECT AD FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD " +
            "WHERE AD.gatewayId LIKE :" + DBConstants.ApplicationDeployment.GATEWAY_ID;
    String FIND_APPLICATION_DEPLOYMENTS_FOR_APPLICATION_MODULE_ID = "SELECT AD FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD " +
            "WHERE AD.appModuleId LIKE :" + DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID;
    String FIND_APPLICATION_DEPLOYMENTS_FOR_COMPUTE_HOST_ID = "SELECT AD FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD " +
            "WHERE AD.computeHostId LIKE :" + DBConstants.ApplicationDeployment.COMPUTE_HOST_ID;
    String GET_ALL_APPLICATION_DEPLOYMENTS = "SELECT AD FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD";
    String FIND_ACCESSIBLE_APPLICATION_DEPLOYMENTS = "SELECT AD FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD " +
            "WHERE AD.gatewayId LIKE :" + DBConstants.ApplicationDeployment.GATEWAY_ID + " AND AD.appDeploymentId IN :" +
            DBConstants.ApplicationDeployment.ACCESSIBLE_APPLICATION_DEPLOYMENT_IDS + " AND AD.computeHostId IN :" +
            DBConstants.ApplicationDeployment.ACCESSIBLE_COMPUTE_HOST_IDS;
    String FIND_ACCESSIBLE_APPLICATION_DEPLOYMENTS_FOR_APP_MODULE = "SELECT AD FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD " +
            "WHERE AD.gatewayId LIKE :" + DBConstants.ApplicationDeployment.GATEWAY_ID + " AND AD.appDeploymentId IN :" +
            DBConstants.ApplicationDeployment.ACCESSIBLE_APPLICATION_DEPLOYMENT_IDS + " AND AD.computeHostId IN :" +
            DBConstants.ApplicationDeployment.ACCESSIBLE_COMPUTE_HOST_IDS + " AND AD.appModuleId = :" +
            DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID;

    // Application Module Queries
    String FIND_APPLICATION_MODULES_FOR_GATEWAY_ID = "SELECT AM FROM " + ApplicationModuleEntity.class.getSimpleName() + " AM " +
            "WHERE AM.gatewayId LIKE :" + DBConstants.ApplicationModule.GATEWAY_ID;
    String FIND_APPLICATION_MODULES_FOR_APPLICATION_MODULE_NAME = "SELECT AM FROM " + ApplicationModuleEntity.class.getSimpleName() + " AM " +
            "WHERE AM.appModuleName LIKE :" + DBConstants.ApplicationModule.APPLICATION_MODULE_NAME;
    String FIND_ACCESSIBLE_APPLICATION_MODULES = "SELECT AM FROM " + ApplicationModuleEntity.class.getSimpleName() + " AM " +
            "WHERE AM.gatewayId LIKE :" + DBConstants.ApplicationModule.GATEWAY_ID + " AND " +
            "EXISTS (SELECT 1 FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD " +
            "WHERE AM.appModuleId = AD.appModuleId AND AD.appDeploymentId IN :" + DBConstants.ApplicationDeployment.ACCESSIBLE_APPLICATION_DEPLOYMENT_IDS +
            " AND AD.computeHostId IN :" + DBConstants.ApplicationDeployment.ACCESSIBLE_COMPUTE_HOST_IDS + ")";

    // Application Interface Queries
    String FIND_APPLICATION_INTERFACES_FOR_GATEWAY_ID = "SELECT AI FROM " + ApplicationInterfaceEntity.class.getSimpleName() + " AI " +
            "WHERE AI.gatewayId LIKE :" + DBConstants.ApplicationInterface.GATEWAY_ID;
    String FIND_APPLICATION_INTERFACES_FOR_APPLICATION_NAME = "SELECT AI FROM " + ApplicationInterfaceEntity.class.getSimpleName() + " AI " +
            "WHERE AI.applicationName LIKE :" + DBConstants.ApplicationInterface.APPLICATION_NAME;
    String GET_ALL_APPLICATION_INTERFACES = "SELECT AI FROM " + ApplicationInterfaceEntity.class.getSimpleName() + " AI";

    // Application Inputs Queries
    String FIND_APPLICATION_INPUTS = "SELECT AI FROM " + ApplicationInputEntity.class.getSimpleName() + " AI " +
            "WHERE AI.interfaceId LIKE :" + DBConstants.ApplicationInput.APPLICATION_INTERFACE_ID;

    // Application Outputs Queries
    String FIND_APPLICATION_OUTPUTS = "SELECT AI FROM " + ApplicationOutputEntity.class.getSimpleName() + " AI " +
            "WHERE AI.interfaceId LIKE :" + DBConstants.ApplicationOutput.APPLICATION_INTERFACE_ID;

    String FIND_ALL_GATEWAY_PROFILES = "SELECT G FROM " + GatewayProfileEntity.class.getSimpleName() + " G";
    String FIND_ALL_COMPUTE_RESOURCE_PREFERENCES = "SELECT DISTINCT CR FROM " + ComputeResourcePreferenceEntity.class.getSimpleName() + " CR " +
            "WHERE CR.gatewayId LIKE :" + DBConstants.ComputeResourcePreference.GATEWAY_ID;
    String FIND_ALL_STORAGE_RESOURCE_PREFERENCES = "SELECT DISTINCT S FROM " + StoragePreferenceEntity.class.getSimpleName() + " S " +
            "WHERE S.gatewayId LIKE :" + DBConstants.StorageResourcePreference.GATEWAY_ID;

    String FIND_COMPUTE_RESOURCE = "SELECT DISTINCT CR FROM " + ComputeResourceEntity.class.getSimpleName() + " CR " +
            "WHERE CR.hostName LIKE :" + DBConstants.ComputeResource.HOST_NAME;
    String FIND_ALL_COMPUTE_RESOURCES = "SELECT CR FROM " + ComputeResourceEntity.class.getSimpleName() + " CR";
    String GET_FILE_SYSTEM = "SELECT DISTINCT FS FROM " + ComputeResourceFileSystemEntity.class.getSimpleName() + " FS " +
            "WHERE FS.computeResourceId LIKE :" + DBConstants.ComputeResource.COMPUTE_RESOURCE_ID;
    String GET_JOB_MANAGER_COMMAND = "SELECT DISTINCT JM FROM " + JobManagerCommandEntity.class.getSimpleName() + " JM " +
            "WHERE JM.resourceJobManagerId LIKE :" + DBConstants.ResourceJobManager.RESOURCE_JOB_MANAGER_ID;
    String GET_PARALLELISM_PREFIX = "SELECT DISTINCT PF FROM " + ParallelismCommandEntity.class.getSimpleName() + " PF " +
            "WHERE PF.resourceJobManagerId LIKE :" + DBConstants.ResourceJobManager.RESOURCE_JOB_MANAGER_ID;

    String FIND_ACCESSIBLE_GROUP_RESOURCE_PROFILES = "SELECT G FROM " + GroupResourceProfileEntity.class.getSimpleName() + " G " +
            "WHERE G.gatewayId LIKE :" + DBConstants.GroupResourceProfile.GATEWAY_ID + " AND G.groupResourceProfileId IN :"
            + DBConstants.GroupResourceProfile.ACCESSIBLE_GROUP_RESOURCE_IDS;
    String FIND_ALL_GROUP_COMPUTE_PREFERENCES = "SELECT GC FROM "+ GroupComputeResourcePrefEntity.class.getSimpleName() + " GC " +
            "WHERE GC.groupResourceProfileId LIKE :" + DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID;
    String FIND_ALL_GROUP_BATCH_QUEUE_RESOURCE_POLICY = "SELECT BQ FROM "+ BatchQueueResourcePolicyEntity.class.getSimpleName() + " BQ " +
            "WHERE BQ.groupResourceProfileId LIKE :" + DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID;
    String FIND_ALL_GROUP_COMPUTE_RESOURCE_POLICY = "SELECT CR FROM "+ ComputeResourcePolicyEntity.class.getSimpleName() + " CR " +
            "WHERE CR.groupResourceProfileId LIKE :" + DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID;

    String GET_ALL_USER_RESOURCE_PROFILE = "SELECT URP FROM " + UserResourceProfileEntity.class.getSimpleName() + " URP";
    String GET_ALL_GATEWAY_ID = "SELECT DISTINCT URP FROM " + UserResourceProfileEntity.class.getSimpleName() + " URP " +
            "WHERE URP.gatewayId LIKE :" + DBConstants.UserResourceProfile.GATEWAY_ID;

    String GET_ALL_GATEWAYS = "SELECT G FROM " + GatewayEntity.class.getSimpleName() + " G";
    String GET_GATEWAY_FROM_GATEWAY_NAME = "SELECT G FROM " + GatewayEntity.class.getSimpleName() + " G " +
            "WHERE G.gatewayName LIKE :" + DBConstants.Gateway.GATEWAY_NAME;

    String GET_ALL_GATEWAY_NOTIFICATIONS = "SELECT N FROM " + NotificationEntity.class.getSimpleName() + " N " +
            "WHERE N.gatewayId LIKE :" + DBConstants.Notification.GATEWAY_ID;

    String GET_ALL_GATEWAY_USERS = "SELECT U FROM " + UserEntity.class.getSimpleName() + " U " +
            "WHERE U.gatewayId LIKE :" + DBConstants.User.GATEWAY_ID;

    String GET_ALL_PROJECTS_FOR_OWNER = "SELECT P FROM " + ProjectEntity.class.getSimpleName() + " P " +
            "WHERE P.owner LIKE :" + DBConstants.Project.OWNER;

    String GET_EXPERIMENTS_FOR_USER = "SELECT E FROM " + ExperimentEntity.class.getSimpleName() + " E " +
            "WHERE E.userName LIKE :" + DBConstants.Experiment.USER_NAME + 
            " AND E.gatewayId = :" + DBConstants.Experiment.GATEWAY_ID;
    String GET_EXPERIMENTS_FOR_PROJECT_ID = "SELECT E FROM " + ExperimentEntity.class.getSimpleName() + " E " +
            "WHERE E.projectId LIKE :" + DBConstants.Experiment.PROJECT_ID +
            " AND E.gatewayId = :" + DBConstants.Experiment.GATEWAY_ID;
    String GET_EXPERIMENTS_FOR_GATEWAY_ID = "SELECT E FROM " + ExperimentEntity.class.getSimpleName() + " E " +
            "WHERE E.gatewayId LIKE :" + DBConstants.Experiment.GATEWAY_ID;

    String GET_PROCESS_FOR_EXPERIMENT_ID = "SELECT P FROM " + ProcessEntity.class.getSimpleName() + " P " +
            "WHERE P.experimentId = :" + DBConstants.Process.EXPERIMENT_ID;

    String GET_TASK_FOR_PARENT_PROCESS_ID = "SELECT T FROM " + TaskEntity.class.getSimpleName() + " T " +
            "WHERE T.parentProcessId LIKE :" + DBConstants.Task.PARENT_PROCESS_ID;

    String GET_JOB_FOR_PROCESS_ID = "SELECT J FROM " + JobEntity.class.getSimpleName() + " J " +
            "WHERE J.processId LIKE :" + DBConstants.Job.PROCESS_ID;
    String GET_JOB_FOR_TASK_ID = "SELECT J FROM " + JobEntity.class.getSimpleName() + " J " +
            "WHERE J.taskId LIKE :" + DBConstants.Job.TASK_ID;
    String GET_JOB_FOR_JOB_ID = "SELECT J FROM " + JobEntity.class.getSimpleName() + " J " +
            "WHERE J.jobId LIKE :" + DBConstants.Job.JOB_ID;

    String GET_ALL_QUEUE_STATUS_MODELS = "SELECT QSM FROM " + QueueStatusEntity.class.getSimpleName() + " QSM";

    String GET_ALL_USER_COMPUTE_RESOURCE_PREFERENCE = "SELECT UCRP FROM " + UserComputeResourcePreferenceEntity.class.getSimpleName() + " UCRP " +
            "WHERE UCRP.userId LIKE :" + DBConstants.UserComputeResourcePreference.USER_ID + " AND UCRP.gatewayId LIKE :" +
            DBConstants.UserComputeResourcePreference.GATEWAY_ID;

    String GET_ALL_USER_STORAGE_PREFERENCE = "SELECT USP FROM " + UserStoragePreferenceEntity.class.getSimpleName() + " USP " +
            "WHERE USP.userId LIKE :" + DBConstants.UserStoragePreference.USER_ID + " AND USP.gatewayId LIKE :" +
            DBConstants.UserStoragePreference.GATEWAY_ID;

    String FIND_ALL_CHILD_DATA_PRODUCTS = "SELECT DP FROM " + DataProductEntity.class.getSimpleName() + " DP " +
            "WHERE DP.parentProductUri LIKE :" + DBConstants.DataProduct.PARENT_PRODUCT_URI;
    String FIND_DATA_PRODUCT_BY_NAME = "SELECT DP FROM " + DataProductEntity.class.getSimpleName() + " DP " +
            "WHERE DP.gatewayId LIKE :" + DBConstants.DataProduct.GATEWAY_ID + " AND DP.ownerName LIKE :" +
            DBConstants.DataProduct.OWNER_NAME + " AND dp.productName LIKE :" + DBConstants.DataProduct.PRODUCT_NAME;

    String GET_WORKFLOW_FOR_EXPERIMENT_ID = "SELECT W FROM " + AiravataWorkflowEntity.class.getSimpleName() + " W " +
            "WHERE W.experimentId LIKE :" + DBConstants.Workflow.EXPERIMENT_ID;

    String FIND_STORAGE_RESOURCE = "SELECT DISTINCT SR FROM " + StorageResourceEntity.class.getSimpleName() + " SR " +
            "WHERE SR.hostName LIKE :" + DBConstants.StorageResource.HOST_NAME;
    String FIND_ALL_STORAGE_RESOURCES = "SELECT SR FROM " + StorageResourceEntity.class.getSimpleName() + " SR";
    String FIND_ALL_AVAILABLE_STORAGE_RESOURCES = "SELECT SR FROM " + StorageResourceEntity.class.getSimpleName() + " SR " +
            "WHERE SR.enabled = TRUE";

    String FIND_ALL_GRID_FTP_ENDPOINTS_BY_DATA_MOVEMENT = "SELECT GFE FROM " + GridftpEndpointEntity.class.getSimpleName() +
            " GFE WHERE GFE.gridftpDataMovement.dataMovementInterfaceId LIKE :" + DBConstants.DataMovement.GRID_FTP_DATA_MOVEMENT_ID;

    String FIND_PARSING_TEMPLATES_FOR_APPLICATION_INTERFACE_ID = "SELECT PT FROM " + ParsingTemplateEntity.class.getSimpleName() + " PT " +
            "WHERE PT.applicationInterface = :" + DBConstants.ParsingTemplate.APPLICATION_INTERFACE_ID;
    String FIND_ALL_PARSING_TEMPLATES_FOR_GATEWAY_ID = "SELECT PT FROM " + ParsingTemplateEntity.class.getSimpleName() + " PT " +
            "WHERE PT.gatewayId = :" + DBConstants.ParsingTemplate.GATEWAY_ID;

    String FIND_ALL_PARSERS_FOR_GATEWAY_ID = "SELECT P FROM " + ParserEntity.class.getSimpleName() + " P " +
            "WHERE P.gatewayId = :" + DBConstants.Parser.GATEWAY_ID;
}
