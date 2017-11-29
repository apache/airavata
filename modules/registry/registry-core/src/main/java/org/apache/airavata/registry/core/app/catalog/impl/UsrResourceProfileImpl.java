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
package org.apache.airavata.registry.core.app.catalog.impl;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.registry.core.app.catalog.resources.*;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogThriftConversion;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.UsrResourceProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UsrResourceProfileImpl implements UsrResourceProfile {
    private final static Logger logger = LoggerFactory.getLogger(UsrResourceProfileImpl.class);

    @Override
    public String addUserResourceProfile(org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile userResourceProfile) throws AppCatalogException {
        try {
            UserResourceProfileResource profileResource = new UserResourceProfileResource();
            if (!userResourceProfile.getUserId().equals("")){
                profileResource.setUserId(userResourceProfile.getUserId());
            }
            if (!userResourceProfile.getGatewayID().equals("")){
                profileResource.setGatewayID(userResourceProfile.getGatewayID());
            }
            if (userResourceProfile.getCredentialStoreToken()!= null){
                profileResource.setCredentialStoreToken(userResourceProfile.getCredentialStoreToken());
            }
            if (userResourceProfile.getIdentityServerTenant() != null){
                profileResource.setIdentityServerTenant(userResourceProfile.getIdentityServerTenant());
            }
            if (userResourceProfile.getIdentityServerPwdCredToken() != null){
                profileResource.setIdentityServerPwdCredToken(userResourceProfile.getIdentityServerPwdCredToken());
            }
            profileResource.setUserId(userResourceProfile.getUserId());
            profileResource.setGatewayID(userResourceProfile.getGatewayID());
            profileResource.save();
            List<UserComputeResourcePreference> userComputeResourcePreferences = userResourceProfile.getUserComputeResourcePreferences();
            if (userComputeResourcePreferences != null && !userComputeResourcePreferences.isEmpty()){
                for (UserComputeResourcePreference preference : userComputeResourcePreferences ){
                    UserComputeHostPreferenceResource resource = new UserComputeHostPreferenceResource();
                    resource.setUserResourceProfileResource(profileResource);
                    resource.setResourceId(preference.getComputeResourceId());
                    ComputeResourceResource computeHostResource = new ComputeResourceResource();
                    resource.setComputeHostResource((ComputeResourceResource)computeHostResource.get(preference.getComputeResourceId()));
                    resource.setGatewayId(profileResource.getGatewayID());
                    resource.setUserId(profileResource.getUserId());
                    resource.setLoginUserName(preference.getLoginUserName());
                    resource.setResourceCSToken(preference.getResourceSpecificCredentialStoreToken());
                    resource.setBatchQueue(preference.getPreferredBatchQueue());
                    resource.setProjectNumber(preference.getAllocationProjectNumber());
                    resource.setScratchLocation(preference.getScratchLocation());
                    resource.setQualityOfService(preference.getQualityOfService());
                    resource.setReservation(preference.getReservation());
                    if(preference.getReservationStartTime() > 0){
                        resource.setReservationStartTime(AiravataUtils.getTime(preference.getReservationStartTime()));
                    }

                    if (preference.getReservationEndTime() > 0) {
                        resource.setReservationEndTime(AiravataUtils.getTime(preference.getReservationEndTime()));
                    }
                    resource.setValidated(preference.isValidated());
                    resource.save();
                }
            }
            List<UserStoragePreference> dataStoragePreferences = userResourceProfile.getUserStoragePreferences();
            if (dataStoragePreferences != null && !dataStoragePreferences.isEmpty()){
                for (UserStoragePreference storagePreference : dataStoragePreferences){
                    UserStoragePreferenceResource resource = new UserStoragePreferenceResource();
                    resource.setStorageResourceId(storagePreference.getStorageResourceId());
                    resource.setGatewayId(profileResource.getGatewayID());
                    resource.setUserId(profileResource.getUserId());
                    resource.setFsRootLocation(storagePreference.getFileSystemRootLocation());
                    resource.setLoginUserName(storagePreference.getLoginUserName());
                    resource.setResourceCSToken(storagePreference.getResourceSpecificCredentialStoreToken());
                    resource.setUserResourceProfileResource(profileResource);
                    resource.save();
                }
            }
            return profileResource.getGatewayID();
        }catch (Exception e) {
            logger.error("Error while saving gateway profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void updateUserResourceProfile(String userId, String gatewayId, org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile updatedProfile) throws AppCatalogException {
        try {
            UserResourceProfileResource profileResource = new UserResourceProfileResource();
            CompositeIdentifier ids = new CompositeIdentifier(userId,gatewayId);
            UserResourceProfileResource existingUP = (UserResourceProfileResource)profileResource.get(ids);
            existingUP.setCredentialStoreToken(updatedProfile.getCredentialStoreToken());
            existingUP.setIdentityServerTenant(updatedProfile.getIdentityServerTenant());
            existingUP.setIdentityServerPwdCredToken(updatedProfile.getIdentityServerPwdCredToken());
            existingUP.save();

            List<UserComputeResourcePreference> userComputeResourcePreferences = updatedProfile.getUserComputeResourcePreferences();
            if (userComputeResourcePreferences != null && !userComputeResourcePreferences.isEmpty()){
                for (UserComputeResourcePreference preference : userComputeResourcePreferences ){
                    UserComputeHostPreferenceResource resource = new UserComputeHostPreferenceResource();
                    resource.setUserResourceProfileResource(existingUP);
                    resource.setResourceId(preference.getComputeResourceId());
                    ComputeResourceResource computeHostResource = new ComputeResourceResource();
                    resource.setComputeHostResource((ComputeResourceResource)computeHostResource.get(preference.getComputeResourceId()));
                    resource.setUserId(userId);
                    resource.setGatewayId(gatewayId);
                    resource.setLoginUserName(preference.getLoginUserName());
                    resource.setBatchQueue(preference.getPreferredBatchQueue());
                    resource.setProjectNumber(preference.getAllocationProjectNumber());
                    resource.setScratchLocation(preference.getScratchLocation());
                    resource.setResourceCSToken(preference.getResourceSpecificCredentialStoreToken());
                    resource.setQualityOfService(preference.getQualityOfService());
                    resource.setReservation(preference.getReservation());
                    if(preference.getReservationStartTime() > 0){
                        resource.setReservationStartTime(AiravataUtils.getTime(preference.getReservationStartTime()));
                    }

                    if (preference.getReservationEndTime() > 0) {
                        resource.setReservationEndTime(AiravataUtils.getTime(preference.getReservationEndTime()));
                    }
                    resource.setValidated(preference.isValidated());
                    resource.save();
                }
            }
            List<UserStoragePreference> dataStoragePreferences = updatedProfile.getUserStoragePreferences();
            if (dataStoragePreferences != null && !dataStoragePreferences.isEmpty()){
                for (UserStoragePreference storagePreference : dataStoragePreferences){
                    UserStoragePreferenceResource resource = new UserStoragePreferenceResource();
                    resource.setStorageResourceId(storagePreference.getStorageResourceId());
                    resource.setGatewayId(existingUP.getGatewayID());
                    resource.setUserId(existingUP.getUserId());
                    resource.setFsRootLocation(storagePreference.getFileSystemRootLocation());
                    resource.setLoginUserName(storagePreference.getLoginUserName());
                    resource.setResourceCSToken(storagePreference.getResourceSpecificCredentialStoreToken());
                    resource.setUserResourceProfileResource(existingUP);
                    resource.save();
                }
            }
        }catch (Exception e) {
            logger.error("Error while updating User Resource profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws AppCatalogException {
        try {
            UserResourceProfileResource resource = new UserResourceProfileResource();
            CompositeIdentifier ids = new CompositeIdentifier(userId,gatewayId);
            UserResourceProfileResource uResource = (UserResourceProfileResource)resource.get(ids);
            UserComputeHostPreferenceResource prefResource = new UserComputeHostPreferenceResource();
            List<AppCatalogResource> usercomputePrefList = prefResource.get(AppCatAbstractResource.UserComputeResourcePreferenceConstants.USER_ID, ids);
            List<UserComputeResourcePreference> userComputeResourcePreferences = AppCatalogThriftConversion.getUserComputeResourcePreferences(usercomputePrefList);
            List<UserStoragePreference> dataStoragePreferences = getAllUserStoragePreferences(userId,gatewayId);
            if(uResource != null){
                return AppCatalogThriftConversion.getUserResourceProfile(uResource, userComputeResourcePreferences, dataStoragePreferences);
            }else{
                return AppCatalogThriftConversion.createNullUserResourceProfile(userId, gatewayId);
            }
        }catch (Exception e) {
            logger.error("Error while retrieving user resource profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public boolean removeUserResourceProfile(String userId, String gatewayId) throws AppCatalogException {
       try {
           UserResourceProfileResource resource = new UserResourceProfileResource();
           CompositeIdentifier ids = new CompositeIdentifier(userId,gatewayId);
           resource.remove(ids);
           return true;
       }catch (Exception e) {
           logger.error("Error while deleting user resource profile...", e);
           throw new AppCatalogException(e);
       }
    }

    @Override
    public boolean removeUserComputeResourcePreferenceFromGateway(String userId, String gatewayId, String preferenceId) throws AppCatalogException {
        try {
            UserComputeHostPreferenceResource resource = new UserComputeHostPreferenceResource();
            CompositeIdentifier ids = new CompositeIdentifier(preferenceId, userId, gatewayId);
            resource.remove(ids);
            return true;
        }catch (Exception e) {
            logger.error("Error while deleting user resource profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public boolean removeUserDataStoragePreferenceFromGateway(String userId, String gatewayId, String preferenceId) throws AppCatalogException {
        try {
            UserStoragePreferenceResource resource = new UserStoragePreferenceResource();
            CompositeIdentifier ids = new CompositeIdentifier(preferenceId,userId, gatewayId);
            resource.remove(ids);
            return true;
        }catch (Exception e) {
            logger.error("Error while deleting user resource profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws AppCatalogException {
        try {
            UserResourceProfileResource resource = new UserResourceProfileResource();
            CompositeIdentifier ids = new CompositeIdentifier(userId, gatewayId);
            return resource.isExists(ids);
        }catch (Exception e) {
            logger.error("Error while retrieving user resource profile...", e);
            throw new AppCatalogException(e);
        }
    }

    /**
     * @param gatewayId
     * @param hostId
     * @return ComputeResourcePreference
     */
    @Override
    public UserComputeResourcePreference getUserComputeResourcePreference(String userId, String gatewayId, String hostId) throws AppCatalogException {
        try {
            UserComputeHostPreferenceResource prefResource = new UserComputeHostPreferenceResource();
            CompositeIdentifier ids = new CompositeIdentifier(userId, gatewayId);
            List<AppCatalogResource> computePrefList = prefResource.get(AppCatAbstractResource.UserComputeResourcePreferenceConstants.USER_ID, ids);
            for (AppCatalogResource resource : computePrefList){
                UserComputeHostPreferenceResource cmP = (UserComputeHostPreferenceResource) resource;
                if (cmP.getResourceId() != null && !cmP.getResourceId().equals("")){
                    if (cmP.getResourceId().equals(hostId)){
                        return AppCatalogThriftConversion.getUserComputeResourcePreference(cmP);
                    }
                }
            }
        }catch (Exception e) {
            logger.error("Error while retrieving user compute resource preference...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    @Override
    public UserStoragePreference getUserStoragePreference(String userId, String gatewayId, String storageId) throws AppCatalogException {
        try {
            UserStoragePreferenceResource prefResource = new UserStoragePreferenceResource();
            CompositeIdentifier ids = new CompositeIdentifier(userId, gatewayId);
            List<AppCatalogResource> computePrefList = prefResource.get(AppCatAbstractResource.UserComputeResourcePreferenceConstants.USER_ID, ids);
            for (AppCatalogResource resource : computePrefList){
                UserStoragePreferenceResource dsP = (UserStoragePreferenceResource) resource;
                if (dsP.getStorageResourceId() != null && !dsP.getStorageResourceId().equals("")){
                    if (dsP.getStorageResourceId().equals(storageId)){
                        return AppCatalogThriftConversion.getUserDataStoragePreference(dsP);
                    }
                }
            }
        }catch (Exception e) {
            logger.error("Error while retrieving user data storage preference...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    /**
     * @param userId
     * @return
     */
    @Override
    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID) throws AppCatalogException {
        try {
            UserComputeHostPreferenceResource prefResource = new UserComputeHostPreferenceResource();
            CompositeIdentifier ids = new CompositeIdentifier(userId, gatewayID);
            List<AppCatalogResource> computePrefList = prefResource.get(AppCatAbstractResource.UserComputeResourcePreferenceConstants.USER_ID, ids);
            return AppCatalogThriftConversion.getUserComputeResourcePreferences(computePrefList);
        }catch (Exception e) {
            logger.error("Error while retrieving compute resource preference...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID) throws AppCatalogException {
        try {
            UserStoragePreferenceResource prefResource = new UserStoragePreferenceResource();
            CompositeIdentifier ids = new CompositeIdentifier(userId, gatewayID);
            List<AppCatalogResource> dataStoragePrefList = prefResource.get(AppCatAbstractResource.UserStoragePreferenceConstants.USER_ID, ids);
            return AppCatalogThriftConversion.getUserDataStoragePreferences(dataStoragePrefList);
        }catch (Exception e) {
            logger.error("Error while retrieving data storage preference...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<String> getGatewayProfileIds(String gatewayName) throws AppCatalogException {
        try {
            UserResourceProfileResource profileResource = new UserResourceProfileResource();
            List<AppCatalogResource> resourceList = profileResource.get(AppCatAbstractResource.UserResourceProfileConstants.GATEWAY_ID, gatewayName);
            List<String> gatewayIds = new ArrayList<String>();
            if (resourceList != null && !resourceList.isEmpty()){
                for (AppCatalogResource resource : resourceList){
                    gatewayIds.add(((UserResourceProfileResource)resource).getGatewayID());
                }
            }
            return gatewayIds;
        }catch (Exception e) {
            logger.error("Error while retrieving gateway ids...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public String getUserNamefromID(String userId, String gatewayID) throws AppCatalogException {
//        Implementation not need, username and ID are some.
        return null;
    }


    @Override
    public List<UserResourceProfile> getAllUserResourceProfiles() throws AppCatalogException {
        try {
            List<UserResourceProfile> gatewayResourceProfileList = new ArrayList<UserResourceProfile>();
            UserResourceProfileResource profileResource = new UserResourceProfileResource();
            List<AppCatalogResource> resourceList = profileResource.getAll();
            if (resourceList != null && !resourceList.isEmpty()){
                for (AppCatalogResource resource : resourceList){
                    UserResourceProfileResource userProfileResource = (UserResourceProfileResource)resource;
                    List<UserComputeResourcePreference> computeResourcePreferences = getAllUserComputeResourcePreferences(userProfileResource.getUserId(),userProfileResource.getGatewayID());
                    List<UserStoragePreference> dataStoragePreferences = getAllUserStoragePreferences(userProfileResource.getUserId(), userProfileResource.getGatewayID());
                    UserResourceProfile gatewayResourceProfile = AppCatalogThriftConversion.getUserResourceProfile(userProfileResource, computeResourcePreferences, dataStoragePreferences);
                    gatewayResourceProfileList.add(gatewayResourceProfile);
                }
            }
            return gatewayResourceProfileList;
        }catch (Exception e) {
            logger.error("Error while retrieving user resource profiles...", e);
            throw new AppCatalogException(e);
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
