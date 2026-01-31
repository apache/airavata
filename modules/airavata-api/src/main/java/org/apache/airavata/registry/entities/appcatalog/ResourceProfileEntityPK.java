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

import java.io.Serializable;
import java.util.Objects;
import org.apache.airavata.common.model.ProfileOwnerType;

/**
 * Composite primary key for {@link ResourceProfileEntity}.
 *
 * <p>The key consists of:
 * <ul>
 *   <li>{@code profileId} - The profile identifier (gatewayId for GATEWAY, userId@gatewayId for USER, groupResourceProfileId for GROUP)</li>
 *   <li>{@code profileType} - The type of profile owner (GATEWAY, USER, or GROUP)</li>
 * </ul>
 */
public class ResourceProfileEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String profileId;
    private ProfileOwnerType profileType;

    public ResourceProfileEntityPK() {}

    public ResourceProfileEntityPK(String profileId, ProfileOwnerType profileType) {
        this.profileId = profileId;
        this.profileType = profileType;
    }

    /**
     * Create a primary key for a gateway profile.
     *
     * @param gatewayId the gateway ID
     * @return the composite primary key
     */
    public static ResourceProfileEntityPK forGateway(String gatewayId) {
        return new ResourceProfileEntityPK(gatewayId, ProfileOwnerType.GATEWAY);
    }

    /**
     * Create a primary key for a user profile.
     *
     * @param userId the user ID
     * @param gatewayId the gateway ID
     * @return the composite primary key
     */
    public static ResourceProfileEntityPK forUser(String userId, String gatewayId) {
        return new ResourceProfileEntityPK(userId + "@" + gatewayId, ProfileOwnerType.USER);
    }

    /**
     * Create a primary key for a group profile.
     *
     * @param groupResourceProfileId the group resource profile ID
     * @return the composite primary key
     */
    public static ResourceProfileEntityPK forGroup(String groupResourceProfileId) {
        return new ResourceProfileEntityPK(groupResourceProfileId, ProfileOwnerType.GROUP);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceProfileEntityPK that = (ResourceProfileEntityPK) o;
        return Objects.equals(profileId, that.profileId) && profileType == that.profileType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, profileType);
    }

    @Override
    public String toString() {
        return "ResourceProfileEntityPK{" + "profileId='" + profileId + '\'' + ", profileType=" + profileType + '}';
    }
}
