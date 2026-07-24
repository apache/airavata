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
package org.apache.airavata.compute.service;

import java.util.List;
import org.apache.airavata.common.RequestContext;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserStoragePreference;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserResourceProfileService {

    // TODO: This is a useless service class that just delegates to the
    // ResourceProfileRegistryService.
    // We should remove it and use the ResourceProfileRegistryService directly in
    // the future.
    private static final Logger logger = LoggerFactory.getLogger(UserResourceProfileService.class);

    private final ResourceProfileRegistryService resourceProfileRegistryService;
    private final QueueStatusRegistryService queueStatusRegistryService;

    public UserResourceProfileService(ResourceProfileRegistryService resourceProfileRegistryService,
            QueueStatusRegistryService queueStatusRegistryService) {
        this.resourceProfileRegistryService = resourceProfileRegistryService;
        this.queueStatusRegistryService = queueStatusRegistryService;
    }

    public String registerUserResourceProfile(RequestContext ctx, UserResourceProfile userResourceProfile)
            throws Exception {
        try {
            return resourceProfileRegistryService.registerUserResourceProfile(userResourceProfile);
        } catch (Exception e) {
            logger.error("Error registering user resource profile: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean isUserResourceProfileExists(RequestContext ctx, String userId, String gatewayId)
            throws Exception {
        try {
            return resourceProfileRegistryService.isUserResourceProfileExists(userId, gatewayId);
        } catch (Exception e) {
            logger.error("Error checking user resource profile existence for user {} gateway {}: {}", userId, gatewayId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public UserResourceProfile getUserResourceProfile(RequestContext ctx, String userId, String gatewayId)
            throws Exception {
        try {
            return resourceProfileRegistryService.getUserResourceProfile(userId, gatewayId);
        } catch (Exception e) {
            logger.error("Error retrieving user resource profile for user {} gateway {}: {}", userId, gatewayId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateUserResourceProfile(
            RequestContext ctx, String userId, String gatewayId, UserResourceProfile userResourceProfile)
            throws Exception {
        try {
            return resourceProfileRegistryService.updateUserResourceProfile(userId, gatewayId, userResourceProfile);
        } catch (Exception e) {
            logger.error("Error updating user resource profile for user {} gateway {}: {}", userId, gatewayId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteUserResourceProfile(RequestContext ctx, String userId, String gatewayId)
            throws Exception {
        try {
            return resourceProfileRegistryService.deleteUserResourceProfile(userId, gatewayId);
        } catch (Exception e) {
            logger.error("Error deleting user resource profile for user {} gateway {}: {}", userId, gatewayId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public boolean addUserComputeResourcePreference(
            RequestContext ctx,
            String userId,
            String gatewayId,
            String userComputeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws Exception {
        try {
            return resourceProfileRegistryService.addUserComputeResourcePreference(
                    userId, gatewayId, userComputeResourceId, userComputeResourcePreference);
        } catch (Exception e) {
            logger.error("Error adding user compute resource preference for user {} gateway {} resource {}: {}", userId,
                    gatewayId, userComputeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public UserComputeResourcePreference getUserComputeResourcePreference(
            RequestContext ctx, String userId, String gatewayId, String userComputeResourceId) throws Exception {
        try {
            return resourceProfileRegistryService.getUserComputeResourcePreference(userId, gatewayId,
                    userComputeResourceId);
        } catch (Exception e) {
            logger.error("Error reading user compute resource preference for user {} gateway {} resource {}: {}",
                    userId, gatewayId, userComputeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateUserComputeResourcePreference(
            RequestContext ctx,
            String userId,
            String gatewayId,
            String userComputeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws Exception {
        try {
            return resourceProfileRegistryService.updateUserComputeResourcePreference(
                    userId, gatewayId, userComputeResourceId, userComputeResourcePreference);
        } catch (Exception e) {
            logger.error("Error updating user compute resource preference for user {} gateway {} resource {}: {}",
                    userId, gatewayId, userComputeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteUserComputeResourcePreference(
            RequestContext ctx, String userId, String gatewayId, String userComputeResourceId) throws Exception {
        try {
            return resourceProfileRegistryService.deleteUserComputeResourcePreference(userId, gatewayId,
                    userComputeResourceId);
        } catch (Exception e) {
            logger.error("Error deleting user compute resource preference for user {} gateway {} resource {}: {}",
                    userId, gatewayId, userComputeResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean addUserStoragePreference(
            RequestContext ctx,
            String userId,
            String gatewayId,
            String userStorageResourceId,
            UserStoragePreference storagePreference)
            throws Exception {
        try {
            return resourceProfileRegistryService.addUserStoragePreference(
                    userId, gatewayId, userStorageResourceId, storagePreference);
        } catch (Exception e) {
            logger.error("Error adding user storage preference for user {} gateway {} storage {}: {}", userId,
                    gatewayId, userStorageResourceId, e.getMessage(), e);
            throw e;
        }
    }

    public UserStoragePreference getUserStoragePreference(
            RequestContext ctx, String userId, String gatewayId, String userStorageId) throws Exception {
        try {
            return resourceProfileRegistryService.getUserStoragePreference(userId, gatewayId, userStorageId);
        } catch (Exception e) {
            logger.error("Error reading user storage preference for user {} gateway {} storage {}: {}", userId,
                    gatewayId, userStorageId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean updateUserStoragePreference(
            RequestContext ctx,
            String userId,
            String gatewayId,
            String userStorageId,
            UserStoragePreference storagePreference)
            throws Exception {
        try {
            return resourceProfileRegistryService.updateUserStoragePreference(userId, gatewayId, userStorageId,
                    storagePreference);
        } catch (Exception e) {
            logger.error("Error updating user storage preference for user {} gateway {} storage {}: {}", userId,
                    gatewayId, userStorageId, e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteUserStoragePreference(
            RequestContext ctx, String userId, String gatewayId, String userStorageId) throws Exception {
        try {
            return resourceProfileRegistryService.deleteUserStoragePreference(userId, gatewayId, userStorageId);
        } catch (Exception e) {
            logger.error("Error deleting user storage preference for user {} gateway {} storage {}: {}", userId,
                    gatewayId, userStorageId, e.getMessage(), e);
            throw e;
        }
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(
            RequestContext ctx, String userId, String gatewayId) throws Exception {
        try {
            return resourceProfileRegistryService.getAllUserComputeResourcePreferences(userId, gatewayId);
        } catch (Exception e) {
            logger.error("Error reading user compute resource preferences for user {} gateway {}: {}", userId,
                    gatewayId, e.getMessage(), e);
            throw e;
        }
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(RequestContext ctx, String userId, String gatewayId)
            throws Exception {
        try {
            return resourceProfileRegistryService.getAllUserStoragePreferences(userId, gatewayId);
        } catch (Exception e) {
            logger.error("Error reading user storage preferences for user {} gateway {}: {}", userId, gatewayId,
                    e.getMessage(), e);
            throw e;
        }
    }

    public List<UserResourceProfile> getAllUserResourceProfiles(RequestContext ctx) throws Exception {
        try {
            return resourceProfileRegistryService.getAllUserResourceProfiles();
        } catch (Exception e) {
            logger.error("Error retrieving all user resource profiles: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<QueueStatusModel> getLatestQueueStatuses(RequestContext ctx) throws Exception {
        try {
            return queueStatusRegistryService.getLatestQueueStatuses();
        } catch (Exception e) {
            logger.error("Error retrieving queue statuses: {}", e.getMessage(), e);
            throw e;
        }
    }
}
