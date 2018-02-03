package org.apache.airavata.registry.core.utils;

import org.apache.airavata.model.user.UserProfile;

import org.apache.airavata.registry.core.entities.appcatalog.*;

public interface QueryConstants {

    String FIND_USER_PROFILE_BY_USER_ID = "SELECT u FROM UserProfileEntity u " +
            "where u.userId LIKE :" + UserProfile._Fields.USER_ID.getFieldName() + " " +
            "AND u.gatewayId LIKE :"+ UserProfile._Fields.GATEWAY_ID.getFieldName() + "";

    String FIND_ALL_USER_PROFILES_BY_GATEWAY_ID = "SELECT u FROM UserProfileEntity u " +
            "where u.gatewayId LIKE :"+ UserProfile._Fields.GATEWAY_ID.getFieldName() + "";

    // Application Deployment Queries
    String FIND_APPLICATION_DEPLOYMENTS_FOR_GATEWAY_ID = "SELECT AD FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD " +
            "WHERE AD.gatewayId LIKE : " + DBConstants.ApplicationDeployment.GATEWAY_ID;
    String FIND_APPLICATION_DEPLOYMENTS_FOR_APPLICATION_MODULE_ID = "SELECT AD FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD " +
            "WHERE AD.appModuleId LIKE : " + DBConstants.ApplicationDeployment.APPLICATION_MODULE_ID;
    String FIND_APPLICATION_DEPLOYMENTS_FOR_COMPUTE_HOST_ID = "SELECT AD FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD " +
            "WHERE AD.computeHostId LIKE : " + DBConstants.ApplicationDeployment.COMPUTE_HOST_ID;
    String FIND_APPLICATION_DEPLOYMENT = "SELECT AD FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD " +
            "WHERE AD.appDeploymentId LIKE : " + DBConstants.ApplicationDeployment.APPLICATION_DEPLOYMENT_ID;
    String GET_ALL_APPLICATION_DEPLOYMENTS = "SELECT AD FROM " + ApplicationDeploymentEntity.class.getSimpleName() + " AD";

    // Application Module Queries
    String FIND_APPLICATION_MODULES_FOR_GATEWAY_ID = "SELECT AM FROM " + ApplicationModuleEntity.class.getSimpleName() + " AM " +
            "WHERE AM.gatewayId LIKE : " + DBConstants.ApplicationModule.GATEWAY_ID;
    String FIND_APPLICATION_MODULES_FOR_APPLICATION_MODULE_NAME = "SELECT AM FROM " + ApplicationModuleEntity.class.getSimpleName() + " AM " +
            "WHERE AM.appModuleName LIKE : " + DBConstants.ApplicationModule.APPLICATION_MODULE_NAME;
    String FIND_APPLICATION_MODULE = "SELECT AM FROM " + ApplicationModuleEntity.class.getSimpleName() + " AM " +
            "WHERE AM.appModuleId LIKE : " + DBConstants.ApplicationModule.APPLICATION_MODULE_ID;

    // Application Interface Queries
    String FIND_APPLICATION_INTERFACES_FOR_GATEWAY_ID = "SELECT AI FROM " + ApplicationInterfaceEntity.class.getSimpleName() + " AI " +
            "WHERE AI.gatewayId LIKE : " + DBConstants.ApplicationInterface.GATEWAY_ID;
    String FIND_APPLICATION_INTERFACES_FOR_APPLICATION_NAME = "SELECT AI FROM " + ApplicationInterfaceEntity.class.getSimpleName() + " AI " +
            "WHERE AI.applicationName LIKE : " + DBConstants.ApplicationInterface.APPLICATION_NAME;
    String FIND_APPLICATION_INTERFACE = "SELECT AI FROM " + ApplicationInterfaceEntity.class.getSimpleName() + " AI " +
            "WHERE AI.applicationInterfaceId LIKE : " + DBConstants.ApplicationInterface.APPLICATION_INTERFACE_ID;
    String GET_ALL_APPLICATION_INTERFACES = "SELECT AI FROM " + ApplicationInterfaceEntity.class.getSimpleName() + " AI";

    // Application Inputs Queries
    String FIND_APPLICATION_INPUTS = "SELECT AI FROM " + ApplicationInputEntity.class.getSimpleName() + " AI " +
            "WHERE AI.interfaceId LIKE : " + DBConstants.ApplicationInputs.APPLICATION_INTERFACE_ID;

    // Application Outputs Queries
    String FIND_APPLICATION_OUTPUTS = "SELECT AI FROM " + ApplicationOutputEntity.class.getSimpleName() + " AI " +
            "WHERE AI.interfaceId LIKE : " + DBConstants.ApplicationOutputs.APPLICATION_INTERFACE_ID;

    // App Module Mapping Queries


    String FIND_ALL_GATEWAY_PROFILES = "SELECT G FROM " + GatewayProfileEntity.class.getSimpleName() + " G";
    String FIND_ALL_COMPUTE_RESOURCE_PREFERENCES = "SELECT DISTINCT CR FROM " + ComputeResourcePreferenceEntity.class.getSimpleName() + " CR " +
            "WHERE CR.gatewayId LIKE : " + DBConstants.ComputeResourcePreference.GATEWAY_ID;
    String FIND_ALL_STORAGE_RESOURCE_PREFERENCES = "SELECT DISTINCT S FROM " + StoragePreferenceEntity.class.getSimpleName() + " S " +
            "WHERE S.gatewayId LIKE : " + DBConstants.StorageResourcePreference.GATEWAY_ID;

    String FIND_COMPUTE_RESOURCE = "SELECT DISTINCT CR FROM " + ComputeResourceEntity.class.getSimpleName() + " CR " +
            "WHERE CR.hostName LIKE : " + DBConstants.ComputeResource.HOST_NAME;
    String FIND_ALL_COMPUTE_RESOURCES = "SELECT CR FROM " + ComputeResourceEntity.class.getSimpleName() + " CR";
    String GET_FILE_SYSTEM = "SELECT DISTINCT FS FROM " + ComputeResourceFileSystemEntity.class.getSimpleName() + " FS " +
            "WHERE FS.computeResourceId LIKE: " + DBConstants.ComputeResource.COMPUTE_RESOURCE_ID;
    String GET_JOB_MANAGER_COMMAND = "SELECT DISTINCT JM FROM " + JobManagerCommandEntity.class.getSimpleName() + " JM " +
            "WHERE JM.id.resourceJobManagerId LIKE: " + DBConstants.ResourceJobManager.RESOURCE_JOB_MANAGER_ID;
    String GET_PARALLELISM_PREFIX = "SELECT DISTINCT PF FROM " + ParallelismCommandEntity.class.getSimpleName() + " PF " +
            "WHERE PF.id.resourceJobManagerId LIKE: " + DBConstants.ResourceJobManager.RESOURCE_JOB_MANAGER_ID;

    String FIND_ALL_GROUP_RESOURCE_PROFILES = "SELECT G FROM " + GroupResourceProfileEntity.class.getSimpleName() + " G " +
            "WHERE G.gatewayId LIKE : " + DBConstants.GroupResourceProfile.GATEWAY_ID;
    String FIND_ALL_GROUP_COMPUTE_PREFERENCES = "SELECT GC FROM "+ GroupComputeResourcePrefEntity.class.getSimpleName() + " GC " +
            "WHERE GC.groupResourceProfileId LIKE : " + DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID;
    String FIND_ALL_GROUP_BATCH_QUEUE_RESOURCE_POLICY = "SELECT BQ FROM "+ BatchQueueResourcePolicyEntity.class.getSimpleName() + " BQ " +
            "WHERE BQ.groupResourceProfileId LIKE : " + DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID;
    String FIND_ALL_GROUP_COMPUTE_RESOURCE_POLICY = "SELECT CR FROM "+ ComputeResourcePolicyEntity.class.getSimpleName() + " CR " +
            "WHERE CR.groupResourceProfileId LIKE : " + DBConstants.GroupResourceProfile.GROUP_RESOURCE_PROFILE_ID;
}
