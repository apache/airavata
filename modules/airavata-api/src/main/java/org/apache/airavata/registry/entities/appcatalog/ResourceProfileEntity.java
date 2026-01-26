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
package org.apache.airavata.registry.entities.appcatalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.airavata.common.model.ProfileOwnerType;
import org.apache.airavata.common.utils.AiravataUtils;

/**
 * Unified entity for resource profiles, consolidating the former GatewayProfileEntity
 * and UserResourceProfileEntity into a single table.
 *
 * <p>This entity represents profile-level metadata for both gateway and user resource profiles.
 * The actual compute and storage preferences are stored in the RESOURCE_PREFERENCE table
 * and linked via the profileId and profileType.
 *
 * <p><strong>Profile ID Convention:</strong>
 * <ul>
 *   <li>GATEWAY profiles: profileId = gatewayId</li>
 *   <li>USER profiles: profileId = "userId@gatewayId"</li>
 * </ul>
 *
 * <p>The gatewayId field is always populated and is extracted from the profileId for USER types.
 * This enables efficient queries for all profiles (gateway and user) within a specific gateway.
 *
 * @see ProfileOwnerType
 * @see ResourceProfileEntityPK
 */
@Entity
@Table(name = "RESOURCE_PROFILE")
@IdClass(ResourceProfileEntityPK.class)
public class ResourceProfileEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The profile identifier.
     * For GATEWAY type: gatewayId
     * For USER type: userId@gatewayId
     */
    @Id
    @Column(name = "PROFILE_ID", nullable = false)
    private String profileId;

    /**
     * The type of profile owner (GATEWAY or USER).
     */
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "PROFILE_TYPE", nullable = false)
    private ProfileOwnerType profileType;

    /**
     * The gateway ID. Always set for both GATEWAY and USER profiles.
     * For GATEWAY profiles, this equals profileId.
     * For USER profiles, this is extracted from profileId.
     */
    @Column(name = "GATEWAY_ID", nullable = false)
    private String gatewayId;

    /**
     * Token for accessing the credential store.
     * Used for secure storage and retrieval of credentials.
     */
    @Column(name = "CREDENTIAL_STORE_TOKEN")
    private String credentialStoreToken;

    /**
     * Identity server password credential token.
     * Used for authentication with identity providers.
     */
    @Column(name = "IDENTITY_SERVER_PWD_CRED_TOKEN")
    private String identityServerPwdCredToken;

    /**
     * Identity server tenant identifier.
     * Identifies the tenant within a multi-tenant identity server.
     */
    @Column(name = "IDENTITY_SERVER_TENANT")
    private String identityServerTenant;

    /**
     * Timestamp when this profile was created.
     */
    @Column(name = "CREATION_TIME", nullable = false, columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Timestamp creationTime;

    /**
     * Timestamp when this profile was last updated.
     */
    @Column(
            name = "UPDATE_TIME",
            nullable = false,
            columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updateTime;

    public ResourceProfileEntity() {}

    /**
     * Create a gateway resource profile.
     *
     * @param gatewayId the gateway ID
     * @return a new ResourceProfileEntity for a gateway
     */
    public static ResourceProfileEntity forGateway(String gatewayId) {
        ResourceProfileEntity entity = new ResourceProfileEntity();
        entity.setProfileId(gatewayId);
        entity.setProfileType(ProfileOwnerType.GATEWAY);
        entity.setGatewayId(gatewayId);
        return entity;
    }

    /**
     * Create a group resource profile.
     *
     * @param groupResourceProfileId the group resource profile ID
     * @param gatewayId the gateway ID
     * @return a new ResourceProfileEntity for a group
     */
    public static ResourceProfileEntity forGroup(String groupResourceProfileId, String gatewayId) {
        ResourceProfileEntity entity = new ResourceProfileEntity();
        entity.setProfileId(groupResourceProfileId);
        entity.setProfileType(ProfileOwnerType.GROUP);
        entity.setGatewayId(gatewayId);
        return entity;
    }

    /**
     * Create a user resource profile.
     *
     * @param userId the user ID
     * @param gatewayId the gateway ID
     * @return a new ResourceProfileEntity for a user
     */
    public static ResourceProfileEntity forUser(String userId, String gatewayId) {
        ResourceProfileEntity entity = new ResourceProfileEntity();
        entity.setProfileId(userId + "@" + gatewayId);
        entity.setProfileType(ProfileOwnerType.USER);
        entity.setGatewayId(gatewayId);
        return entity;
    }

    @PrePersist
    void setTimestamps() {
        Timestamp now = AiravataUtils.getCurrentTimestamp();
        if (this.creationTime == null) {
            this.creationTime = now;
        }
        this.updateTime = now;
    }

    @PreUpdate
    void updateTimestamp() {
        this.updateTime = AiravataUtils.getCurrentTimestamp();
    }

    // --- Getters and Setters ---

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public ProfileOwnerType getProfileType() {
        return profileType;
    }

    public void setProfileType(ProfileOwnerType profileType) {
        this.profileType = profileType;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getCredentialStoreToken() {
        return credentialStoreToken;
    }

    public void setCredentialStoreToken(String credentialStoreToken) {
        this.credentialStoreToken = credentialStoreToken;
    }

    public String getIdentityServerPwdCredToken() {
        return identityServerPwdCredToken;
    }

    public void setIdentityServerPwdCredToken(String identityServerPwdCredToken) {
        this.identityServerPwdCredToken = identityServerPwdCredToken;
    }

    public String getIdentityServerTenant() {
        return identityServerTenant;
    }

    public void setIdentityServerTenant(String identityServerTenant) {
        this.identityServerTenant = identityServerTenant;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    // --- Convenience methods for profile type checking ---

    /**
     * Extract the user ID from the profile ID.
     * Only valid for USER profile types.
     *
     * @return the user ID
     * @throws IllegalStateException if profile type is not USER
     */
    public String getUserId() {
        if (profileType != ProfileOwnerType.USER) {
            throw new IllegalStateException("getUserId() is only valid for USER profile type");
        }
        return profileType.extractUserId(profileId);
    }

    /**
     * Check if this is a gateway profile.
     *
     * @return true if this is a GATEWAY profile
     */
    public boolean isGatewayProfile() {
        return profileType == ProfileOwnerType.GATEWAY;
    }

    /**
     * Check if this is a group profile.
     *
     * @return true if this is a GROUP profile
     */
    public boolean isGroupProfile() {
        return profileType == ProfileOwnerType.GROUP;
    }

    /**
     * Check if this is a user profile.
     *
     * @return true if this is a USER profile
     */
    public boolean isUserProfile() {
        return profileType == ProfileOwnerType.USER;
    }

    @Override
    public String toString() {
        return "ResourceProfileEntity{"
                + "profileId='"
                + profileId
                + '\''
                + ", profileType="
                + profileType
                + ", gatewayId='"
                + gatewayId
                + '\''
                + '}';
    }
}
