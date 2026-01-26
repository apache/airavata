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
package org.apache.airavata.registry.repositories.appcatalog;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.common.model.ProfileOwnerType;
import org.apache.airavata.registry.entities.appcatalog.ResourceProfileEntity;
import org.apache.airavata.registry.entities.appcatalog.ResourceProfileEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for unified resource profile operations.
 *
 * <p>This repository provides access to both gateway and user resource profiles
 * through the unified RESOURCE_PROFILE table.
 */
@Repository
public interface ResourceProfileRepository extends JpaRepository<ResourceProfileEntity, ResourceProfileEntityPK> {

    /**
     * Find all profiles of a specific type.
     *
     * @param profileType the profile type (GATEWAY or USER)
     * @return list of profiles matching the type
     */
    List<ResourceProfileEntity> findByProfileType(ProfileOwnerType profileType);

    /**
     * Find all profiles for a specific gateway (includes both gateway and user profiles).
     *
     * @param gatewayId the gateway ID
     * @return list of all profiles for the gateway
     */
    List<ResourceProfileEntity> findByGatewayId(String gatewayId);

    /**
     * Find all profiles for a specific gateway and type.
     *
     * @param gatewayId the gateway ID
     * @param profileType the profile type
     * @return list of profiles matching both criteria
     */
    List<ResourceProfileEntity> findByGatewayIdAndProfileType(String gatewayId, ProfileOwnerType profileType);

    /**
     * Find all profiles for a specific gateway, type, and profile IDs.
     *
     * @param gatewayId the gateway ID
     * @param profileType the profile type
     * @param profileIds the list of profile IDs to filter by
     * @return list of profiles matching all criteria
     */
    List<ResourceProfileEntity> findByGatewayIdAndProfileTypeAndProfileIdIn(
            String gatewayId, ProfileOwnerType profileType, List<String> profileIds);

    /**
     * Find all gateway profiles.
     *
     * @return list of all gateway profiles
     */
    default List<ResourceProfileEntity> findAllGatewayProfiles() {
        return findByProfileType(ProfileOwnerType.GATEWAY);
    }

    /**
     * Find all user profiles.
     *
     * @return list of all user profiles
     */
    default List<ResourceProfileEntity> findAllUserProfiles() {
        return findByProfileType(ProfileOwnerType.USER);
    }

    /**
     * Find all user profiles for a specific gateway.
     *
     * @param gatewayId the gateway ID
     * @return list of user profiles for the gateway
     */
    default List<ResourceProfileEntity> findUserProfilesByGatewayId(String gatewayId) {
        return findByGatewayIdAndProfileType(gatewayId, ProfileOwnerType.USER);
    }

    /**
     * Find a gateway profile by gateway ID.
     *
     * @param gatewayId the gateway ID
     * @return the gateway profile, if found
     */
    default Optional<ResourceProfileEntity> findGatewayProfile(String gatewayId) {
        return findById(ResourceProfileEntityPK.forGateway(gatewayId));
    }

    /**
     * Find a user profile by user ID and gateway ID.
     *
     * @param userId the user ID
     * @param gatewayId the gateway ID
     * @return the user profile, if found
     */
    default Optional<ResourceProfileEntity> findUserProfile(String userId, String gatewayId) {
        return findById(ResourceProfileEntityPK.forUser(userId, gatewayId));
    }

    /**
     * Check if a gateway profile exists.
     *
     * @param gatewayId the gateway ID
     * @return true if the gateway profile exists
     */
    default boolean gatewayProfileExists(String gatewayId) {
        return existsById(ResourceProfileEntityPK.forGateway(gatewayId));
    }

    /**
     * Check if a user profile exists.
     *
     * @param userId the user ID
     * @param gatewayId the gateway ID
     * @return true if the user profile exists
     */
    default boolean userProfileExists(String userId, String gatewayId) {
        return existsById(ResourceProfileEntityPK.forUser(userId, gatewayId));
    }

    /**
     * Delete a gateway profile.
     *
     * @param gatewayId the gateway ID
     */
    default void deleteGatewayProfile(String gatewayId) {
        deleteById(ResourceProfileEntityPK.forGateway(gatewayId));
    }

    /**
     * Delete a user profile.
     *
     * @param userId the user ID
     * @param gatewayId the gateway ID
     */
    default void deleteUserProfile(String userId, String gatewayId) {
        deleteById(ResourceProfileEntityPK.forUser(userId, gatewayId));
    }

    /**
     * Count profiles by type.
     *
     * @param profileType the profile type
     * @return the count of profiles
     */
    long countByProfileType(ProfileOwnerType profileType);

    /**
     * Count gateway profiles.
     *
     * @return the count of gateway profiles
     */
    default long countGatewayProfiles() {
        return countByProfileType(ProfileOwnerType.GATEWAY);
    }

    /**
     * Count user profiles.
     *
     * @return the count of user profiles
     */
    default long countUserProfiles() {
        return countByProfileType(ProfileOwnerType.USER);
    }

    /**
     * Find user profiles by user ID across all gateways.
     *
     * @param userId the user ID
     * @return list of user profiles for the user
     */
    @Query("SELECT p FROM ResourceProfileEntity p WHERE p.profileType = 'USER' AND p.profileId LIKE :userId || '@%'")
    List<ResourceProfileEntity> findUserProfilesByUserId(@Param("userId") String userId);
}
