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
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.execution.service.RequestContext;
import org.apache.airavata.execution.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserResourceProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserResourceProfileService.class);

    private final RegistryServerHandler registryHandler;

    public UserResourceProfileService(RegistryServerHandler registryHandler) {
        this.registryHandler = registryHandler;
    }

    public String registerUserResourceProfile(RequestContext ctx, UserResourceProfile userResourceProfile)
            throws ServiceException {
        try {
            return registryHandler.registerUserResourceProfile(userResourceProfile);
        } catch (Exception e) {
            throw new ServiceException("Error while registering user resource profile: " + e.getMessage(), e);
        }
    }

    public boolean isUserResourceProfileExists(RequestContext ctx, String userId, String gatewayId)
            throws ServiceException {
        try {
            return registryHandler.isUserResourceProfileExists(userId, gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while checking existence of user resource profile: " + e.getMessage(), e);
        }
    }

    public UserResourceProfile getUserResourceProfile(RequestContext ctx, String userId, String gatewayId)
            throws ServiceException {
        try {
            return registryHandler.getUserResourceProfile(userId, gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving user resource profile: " + e.getMessage(), e);
        }
    }

    public boolean updateUserResourceProfile(
            RequestContext ctx, String userId, String gatewayId, UserResourceProfile userResourceProfile)
            throws ServiceException {
        try {
            return registryHandler.updateUserResourceProfile(userId, gatewayId, userResourceProfile);
        } catch (Exception e) {
            throw new ServiceException("Error while updating user resource profile: " + e.getMessage(), e);
        }
    }

    public boolean deleteUserResourceProfile(RequestContext ctx, String userId, String gatewayId)
            throws ServiceException {
        try {
            return registryHandler.deleteUserResourceProfile(userId, gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting user resource profile: " + e.getMessage(), e);
        }
    }

    public boolean addUserComputeResourcePreference(
            RequestContext ctx,
            String userId,
            String gatewayId,
            String userComputeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws ServiceException {
        try {
            return registryHandler.addUserComputeResourcePreference(
                    userId, gatewayId, userComputeResourceId, userComputeResourcePreference);
        } catch (Exception e) {
            throw new ServiceException("Error while adding user compute resource preference: " + e.getMessage(), e);
        }
    }

    public UserComputeResourcePreference getUserComputeResourcePreference(
            RequestContext ctx, String userId, String gatewayId, String userComputeResourceId) throws ServiceException {
        try {
            return registryHandler.getUserComputeResourcePreference(userId, gatewayId, userComputeResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while reading user compute resource preference: " + e.getMessage(), e);
        }
    }

    public boolean updateUserComputeResourcePreference(
            RequestContext ctx,
            String userId,
            String gatewayId,
            String userComputeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws ServiceException {
        try {
            return registryHandler.updateUserComputeResourcePreference(
                    userId, gatewayId, userComputeResourceId, userComputeResourcePreference);
        } catch (Exception e) {
            throw new ServiceException("Error while updating user compute resource preference: " + e.getMessage(), e);
        }
    }

    public boolean deleteUserComputeResourcePreference(
            RequestContext ctx, String userId, String gatewayId, String userComputeResourceId) throws ServiceException {
        try {
            return registryHandler.deleteUserComputeResourcePreference(userId, gatewayId, userComputeResourceId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting user compute resource preference: " + e.getMessage(), e);
        }
    }

    public boolean addUserStoragePreference(
            RequestContext ctx,
            String userId,
            String gatewayId,
            String userStorageResourceId,
            UserStoragePreference storagePreference)
            throws ServiceException {
        try {
            return registryHandler.addUserStoragePreference(
                    userId, gatewayId, userStorageResourceId, storagePreference);
        } catch (Exception e) {
            throw new ServiceException("Error while adding user storage preference: " + e.getMessage(), e);
        }
    }

    public UserStoragePreference getUserStoragePreference(
            RequestContext ctx, String userId, String gatewayId, String userStorageId) throws ServiceException {
        try {
            return registryHandler.getUserStoragePreference(userId, gatewayId, userStorageId);
        } catch (Exception e) {
            throw new ServiceException("Error while reading user storage preference: " + e.getMessage(), e);
        }
    }

    public boolean updateUserStoragePreference(
            RequestContext ctx,
            String userId,
            String gatewayId,
            String userStorageId,
            UserStoragePreference storagePreference)
            throws ServiceException {
        try {
            return registryHandler.updateUserStoragePreference(userId, gatewayId, userStorageId, storagePreference);
        } catch (Exception e) {
            throw new ServiceException("Error while updating user storage preference: " + e.getMessage(), e);
        }
    }

    public boolean deleteUserStoragePreference(
            RequestContext ctx, String userId, String gatewayId, String userStorageId) throws ServiceException {
        try {
            return registryHandler.deleteUserStoragePreference(userId, gatewayId, userStorageId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting user storage preference: " + e.getMessage(), e);
        }
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(
            RequestContext ctx, String userId, String gatewayId) throws ServiceException {
        try {
            return registryHandler.getAllUserComputeResourcePreferences(userId, gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while reading user compute resource preferences: " + e.getMessage(), e);
        }
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(RequestContext ctx, String userId, String gatewayId)
            throws ServiceException {
        try {
            return registryHandler.getAllUserStoragePreferences(userId, gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while reading user storage preferences: " + e.getMessage(), e);
        }
    }

    public List<UserResourceProfile> getAllUserResourceProfiles(RequestContext ctx) throws ServiceException {
        try {
            return registryHandler.getAllUserResourceProfiles();
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving all user resource profiles: " + e.getMessage(), e);
        }
    }

    public List<QueueStatusModel> getLatestQueueStatuses(RequestContext ctx) throws ServiceException {
        try {
            return registryHandler.getLatestQueueStatuses();
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving queue statuses: " + e.getMessage(), e);
        }
    }
}
