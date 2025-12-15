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
package org.apache.airavata.thriftapi.handler;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.profile.exception.IamAdminServicesException;
import org.apache.airavata.profile.exception.UserProfileServiceException;
import org.apache.airavata.security.interceptor.SecurityCheck;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Component
public class UserProfileServiceHandler implements org.apache.airavata.thriftapi.profile.model.UserProfileService.Iface {

    private final org.apache.airavata.service.profile.UserProfileService userProfileService;

    public UserProfileServiceHandler(org.apache.airavata.service.profile.UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Override
    public String getAPIVersion() throws org.apache.airavata.thriftapi.exception.AiravataSystemException, TException {
        return org.apache.airavata.thriftapi.profile.model.profile_user_cpiConstants.USER_PROFILE_CPI_VERSION;
    }

    @Override
    @SecurityCheck
    public String initializeUserProfile(org.apache.airavata.thriftapi.security.model.AuthzToken authzToken)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = convertToDomainAuthzToken(authzToken);
            return userProfileService.initializeUserProfile(domainAuthzToken);
        } catch (org.apache.airavata.profile.exception.UserProfileServiceException e) {
            throw convertToThriftUserProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException(
                            "Error initializing user profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public String addUserProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.UserProfile userProfile)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = convertToDomainAuthzToken(authzToken);
            org.apache.airavata.common.model.UserProfile domainProfile = convertToDomainUserProfile(userProfile);
            return userProfileService.addUserProfile(domainAuthzToken, domainProfile);
        } catch (IamAdminServicesException e) {
            org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException(
                            "Error adding user profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.UserProfileServiceException e) {
            throw convertToThriftUserProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException(
                            "Error adding user profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUserProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken,
            org.apache.airavata.thriftapi.model.UserProfile userProfile)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = convertToDomainAuthzToken(authzToken);
            org.apache.airavata.common.model.UserProfile domainProfile = convertToDomainUserProfile(userProfile);
            return userProfileService.updateUserProfile(domainAuthzToken, domainProfile);
        } catch (IamAdminServicesException e) {
            org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException(
                            "Error updating user profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        } catch (org.apache.airavata.profile.exception.UserProfileServiceException e) {
            throw convertToThriftUserProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException(
                            "Error updating user profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public org.apache.airavata.thriftapi.model.UserProfile getUserProfileById(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userId, String gatewayId)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = convertToDomainAuthzToken(authzToken);
            org.apache.airavata.common.model.UserProfile domainProfile =
                    userProfileService.getUserProfileById(domainAuthzToken, userId, gatewayId);
            return convertToThriftUserProfile(domainProfile);
        } catch (org.apache.airavata.profile.exception.UserProfileServiceException e) {
            throw convertToThriftUserProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException(
                            "Error getting user profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteUserProfile(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userId, String gatewayId)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = convertToDomainAuthzToken(authzToken);
            return userProfileService.deleteUserProfile(domainAuthzToken, userId, gatewayId);
        } catch (org.apache.airavata.profile.exception.UserProfileServiceException e) {
            throw convertToThriftUserProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException(
                            "Error deleting user profile: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    @SecurityCheck
    public List<org.apache.airavata.thriftapi.model.UserProfile> getAllUserProfilesInGateway(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String gatewayId, int offset, int limit)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = convertToDomainAuthzToken(authzToken);
            List<org.apache.airavata.common.model.UserProfile> domainProfiles =
                    userProfileService.getAllUserProfilesInGateway(domainAuthzToken, gatewayId, offset, limit);
            return domainProfiles.stream().map(this::convertToThriftUserProfile).collect(Collectors.toList());
        } catch (org.apache.airavata.profile.exception.UserProfileServiceException e) {
            throw convertToThriftUserProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException(
                            "Error getting all user profiles: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public boolean doesUserExist(
            org.apache.airavata.thriftapi.security.model.AuthzToken authzToken, String userId, String gatewayId)
            throws org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException,
            org.apache.airavata.thriftapi.exception.AuthorizationException, TException {
        try {
            org.apache.airavata.security.model.AuthzToken domainAuthzToken = convertToDomainAuthzToken(authzToken);
            return userProfileService.doesUserExist(domainAuthzToken, userId, gatewayId);
        } catch (org.apache.airavata.profile.exception.UserProfileServiceException e) {
            throw convertToThriftUserProfileServiceException(e);
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            throw convertToThriftAuthorizationException(e);
        } catch (Exception e) {
            org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException ex =
                    new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException(
                            "Error checking if user exists: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    // Helper methods for conversion
    private org.apache.airavata.security.model.AuthzToken convertToDomainAuthzToken(
            org.apache.airavata.thriftapi.security.model.AuthzToken thrift) {
        org.apache.airavata.security.model.AuthzToken domain = new org.apache.airavata.security.model.AuthzToken();
        domain.setAccessToken(thrift.getAccessToken());
        domain.setClaimsMap(thrift.getClaimsMap());
        return domain;
    }

    private org.apache.airavata.common.model.UserProfile convertToDomainUserProfile(
            org.apache.airavata.thriftapi.model.UserProfile thrift) {
        // Simple conversion - in production, use a mapper
        org.apache.airavata.common.model.UserProfile domain = new org.apache.airavata.common.model.UserProfile();
        domain.setUserId(thrift.getUserId());
        domain.setGatewayId(thrift.getGatewayId());
        if (thrift.isSetFirstName()) {
            domain.setFirstName(thrift.getFirstName());
        }
        if (thrift.isSetLastName()) {
            domain.setLastName(thrift.getLastName());
        }
        if (thrift.isSetEmail()) {
            domain.setEmail(thrift.getEmail());
        }
        // Add other fields as needed
        return domain;
    }

    private org.apache.airavata.thriftapi.model.UserProfile convertToThriftUserProfile(
            org.apache.airavata.common.model.UserProfile domain) {
        org.apache.airavata.thriftapi.model.UserProfile thrift =
                new org.apache.airavata.thriftapi.model.UserProfile();
        thrift.setUserId(domain.getUserId());
        thrift.setGatewayId(domain.getGatewayId());
        if (domain.getFirstName() != null) {
            thrift.setFirstName(domain.getFirstName());
        }
        if (domain.getLastName() != null) {
            thrift.setLastName(domain.getLastName());
        }
        if (domain.getEmail() != null) {
            thrift.setEmail(domain.getEmail());
        }
        // Add other fields as needed
        return thrift;
    }

    private org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException convertToThriftUserProfileServiceException(
            org.apache.airavata.profile.exception.UserProfileServiceException e) {
        org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException thriftException =
                new org.apache.airavata.thriftapi.profile.exception.UserProfileServiceException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }

    private org.apache.airavata.thriftapi.exception.AuthorizationException convertToThriftAuthorizationException(
            org.apache.airavata.common.exception.AuthorizationException e) {
        org.apache.airavata.thriftapi.exception.AuthorizationException thriftException =
                new org.apache.airavata.thriftapi.exception.AuthorizationException();
        thriftException.setMessage(e.getMessage());
        thriftException.initCause(e);
        return thriftException;
    }
}
