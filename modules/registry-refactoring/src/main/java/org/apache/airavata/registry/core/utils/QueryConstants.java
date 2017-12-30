package org.apache.airavata.registry.core.utils;

import org.apache.airavata.model.user.UserProfile;

import org.apache.airavata.registry.core.entities.appcatalog.*;

public interface QueryConstants {

    String FIND_USER_PROFILE_BY_USER_ID = "SELECT u FROM UserProfileEntity u " +
            "where u.userId LIKE :" + UserProfile._Fields.USER_ID.getFieldName() + " " +
            "AND u.gatewayId LIKE :"+ UserProfile._Fields.GATEWAY_ID.getFieldName() + "";

    String FIND_ALL_USER_PROFILES_BY_GATEWAY_ID = "SELECT u FROM UserProfileEntity u " +
            "where u.gatewayId LIKE :"+ UserProfile._Fields.GATEWAY_ID.getFieldName() + "";

    String FIND_ALL_GATEWAY_PROFILES = "SELECT G FROM " + GatewayProfileEntity.class.getSimpleName() + " G";
    String FIND_ALL_COMPUTE_RESOURCE_PREFERENCES = "SELECT DISTINCT CR FROM " + ComputeResourcePreferenceEntity.class.getSimpleName() + " CR " +
            "WHERE CR.id.gatewayId LIKE : " + DBConstants.ComputeResourcePreference.GATEWAY_ID;
    String FIND_ALL_STORAGE_RESOURCE_PREFERENCES = "SELECT DISTINCT S FROM " + StoragePreferenceEntity.class.getSimpleName() + " S " +
            "WHERE S.id.gatewayId LIKE : " + DBConstants.StorageResourcePreference.GATEWAY_ID;

    String FIND_COMPUTE_RESOURCE = "SELECT DISTINCT CR FROM " + ComputeResourceEntity.class.getSimpleName() + " CR " +
            "WHERE CR.hostName LIKE : " + DBConstants.ComputeResource.HOST_NAME;
    String FIND_ALL_COMPUTE_RESOURCES = "SELECT CR FROM " + ComputeResourceEntity.class.getSimpleName() + " CR";
    String GET_JOB_SUBMISSION_INTERFACE = "SELECT DISTINCT J FROM " + JobSubmissionInterfaceEntity.class.getSimpleName() + " J " +
            "WHERE J.id.computeResourceId LIKE: " + DBConstants.ComputeResource.COMPUTE_RESOURCE_ID;
    String GET_FILE_SYSTEM = "SELECT DISTINCT FS FROM " + ComputeResourceFileSystemEntity.class.getSimpleName() + " FS " +
            "WHERE FS.id.computeResourceId LIKE: " + DBConstants.ComputeResource.COMPUTE_RESOURCE_ID;
    String GET_BATCH_QUEUES = "SELECT DISTINCT BQ FROM "+ BatchQueueEntity.class.getSimpleName() + " BQ " +
            "WHERE BQ.id.computeResourceId LIKE: " + DBConstants.ComputeResource.COMPUTE_RESOURCE_ID;
    String GET_DATA_MOVEMENT_INTERFACES = "SELECT DISTINCT DM FROM " + DataMovementInterfaceEntity.class.getSimpleName() + " DM "+
            "WHERE DM.id.computeResourceId LIKE: " + DBConstants.ComputeResource.COMPUTE_RESOURCE_ID;
    String GET_HOST_ALIASES = "SELECT DISTINCT HA.id.alias FROM "+ HostAliasEntity.class.getSimpleName() + " HA "+
            "WHERE HA.id.computeResourceId LIKE: " + DBConstants.ComputeResource.COMPUTE_RESOURCE_ID;
    String GET_IP_ADDRESS = "SELECT DISTINCT IP.id.ipAddress FROM "+ HostIpaddressEntity.class.getSimpleName() + " IP "+
            "WHERE IP.id.computeResourceId LIKE: " + DBConstants.ComputeResource.COMPUTE_RESOURCE_ID;
}
