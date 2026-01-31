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
package org.apache.airavata.common.model;

/**
 * Enum representing the type of owner for a resource profile.
 *
 * <p>A resource profile can be owned by a gateway, a group, or a user within a gateway.
 * This enum is used to discriminate between these types of profiles in the
 * unified RESOURCE_PROFILE table.
 *
 * <p><strong>Profile ID Format:</strong>
 * <ul>
 *   <li>{@code GATEWAY}: profileId = gatewayId</li>
 *   <li>{@code GROUP}: profileId = groupResourceProfileId</li>
 *   <li>{@code USER}: profileId = "userId@gatewayId"</li>
 * </ul>
 *
 * <p>This enum maps to {@link PreferenceLevel} for preference resolution:
 * <ul>
 *   <li>{@code GATEWAY} maps to {@link PreferenceLevel#GATEWAY}</li>
 *   <li>{@code GROUP} maps to {@link PreferenceLevel#GROUP}</li>
 *   <li>{@code USER} is deprecated; map to personal group and use {@link PreferenceLevel#GROUP}</li>
 * </ul>
 *
 * @see PreferenceLevel
 */
public enum ProfileOwnerType {
    /**
     * Gateway-level profile. The profileId is the gatewayId.
     * These profiles contain default preferences for all users in a gateway.
     */
    GATEWAY,

    /**
     * Group-level profile. The profileId is the groupResourceProfileId.
     * All groups (personal, admin, custom) are at this level.
     */
    GROUP,

    /**
     * User-level profile. Deprecated: use GROUP with profileId = user's personal group ID.
     *
     * @deprecated Map to personal group and use GROUP type instead.
     */
    @Deprecated
    USER;

    /**
     * Convert this ProfileOwnerType to the corresponding PreferenceLevel.
     *
     * @return the corresponding PreferenceLevel
     */
    public PreferenceLevel toPreferenceLevel() {
        switch (this) {
            case GATEWAY:
                return PreferenceLevel.GATEWAY;
            case GROUP:
            case USER:
                return PreferenceLevel.GROUP;
            default:
                throw new IllegalStateException("Unknown ProfileOwnerType: " + this);
        }
    }

    /**
     * Create a ProfileOwnerType from a PreferenceLevel.
     *
     * @param level the preference level
     * @return the corresponding ProfileOwnerType
     */
    public static ProfileOwnerType fromPreferenceLevel(PreferenceLevel level) {
        switch (level) {
            case SYSTEM:
                // SYSTEM has no ProfileOwnerType equivalent; use GATEWAY for legacy
                return GATEWAY;
            case GATEWAY:
                return GATEWAY;
            case GROUP:
            case USER:
                return GROUP;
            default:
                throw new IllegalArgumentException("Unknown PreferenceLevel: " + level);
        }
    }

    /**
     * Build a profile ID from the owner type and identifiers.
     *
     * @param gatewayId the gateway ID (used for GATEWAY type, extracted from USER type)
     * @param userId the user ID (only used for USER type)
     * @return the profile ID
     */
    public String buildProfileId(String gatewayId, String userId) {
        switch (this) {
            case GATEWAY:
                return gatewayId;
            case GROUP:
                // For GROUP, the gatewayId parameter is actually the groupResourceProfileId
                return gatewayId;
            case USER:
                if (userId == null || userId.isEmpty()) {
                    throw new IllegalArgumentException("userId is required for USER profile type");
                }
                return userId + "@" + gatewayId;
            default:
                throw new IllegalStateException("Unknown ProfileOwnerType: " + this);
        }
    }

    /**
     * Extract the gateway ID from a profile ID.
     * Note: For GROUP profiles, this returns the groupResourceProfileId (gateway must be looked up separately).
     *
     * @param profileId the profile ID
     * @return the gateway ID (or groupResourceProfileId for GROUP type)
     */
    public String extractGatewayId(String profileId) {
        switch (this) {
            case GATEWAY:
                return profileId;
            case GROUP:
                // For GROUP, the profileId is the groupResourceProfileId, not the gatewayId
                // Gateway must be looked up from the GroupResourceProfile entity
                return profileId;
            case USER:
                int atIndex = profileId.lastIndexOf('@');
                if (atIndex < 0) {
                    throw new IllegalArgumentException("Invalid USER profile ID format: " + profileId);
                }
                return profileId.substring(atIndex + 1);
            default:
                throw new IllegalStateException("Unknown ProfileOwnerType: " + this);
        }
    }

    /**
     * Extract the user ID from a profile ID (USER type only).
     *
     * @param profileId the profile ID
     * @return the user ID
     * @throws IllegalStateException if called on non-USER type
     */
    public String extractUserId(String profileId) {
        if (this != USER) {
            throw new IllegalStateException("extractUserId() is only valid for USER profile type");
        }
        int atIndex = profileId.lastIndexOf('@');
        if (atIndex < 0) {
            throw new IllegalArgumentException("Invalid USER profile ID format: " + profileId);
        }
        return profileId.substring(0, atIndex);
    }
}
