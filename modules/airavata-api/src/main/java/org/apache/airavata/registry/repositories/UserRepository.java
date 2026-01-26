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
package org.apache.airavata.registry.repositories;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.registry.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Unified User Repository for OIDC-based UserEntity.
 *
 * <p>The UserEntity uses OIDC standard claims:
 * <ul>
 *   <li>airavataInternalUserId - primary key (sub@gatewayId format)</li>
 *   <li>sub - OIDC subject identifier (replaces userId)</li>
 *   <li>gatewayId - gateway context</li>
 *   <li>givenName, familyName - name fields</li>
 *   <li>preferredUsername - display name</li>
 *   <li>email - user's email</li>
 * </ul>
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    // ==================== Primary Key Lookups ====================

    /**
     * Find a user by their airavata internal user ID (the primary key).
     * The ID format is: sub@gatewayId
     *
     * @param airavataInternalUserId the internal user ID
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM UserEntity u WHERE u.airavataInternalUserId = :airavataInternalUserId")
    Optional<UserEntity> findByAiravataInternalUserId(
            @Param("airavataInternalUserId") String airavataInternalUserId);

    // ==================== Composite Key Lookups ====================

    /**
     * Find a user by sub (userId) and gatewayId combination.
     *
     * @param sub the OIDC subject identifier (userId)
     * @param gatewayId the gateway identifier
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM UserEntity u WHERE u.sub = :sub AND u.gatewayId = :gatewayId")
    Optional<UserEntity> findBySubAndGatewayId(
            @Param("sub") String sub, @Param("gatewayId") String gatewayId);

    /**
     * Find a user by userId and gatewayId combination (alias for findBySubAndGatewayId).
     * Kept for backward compatibility.
     */
    default Optional<UserEntity> findByUserIdAndGatewayId(String userId, String gatewayId) {
        return findBySubAndGatewayId(userId, gatewayId);
    }

    /**
     * Check if a user exists by sub (userId) and gatewayId combination.
     *
     * @param sub the OIDC subject identifier
     * @param gatewayId the gateway identifier
     * @return true if the user exists
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u "
            + "WHERE u.sub = :sub AND u.gatewayId = :gatewayId")
    boolean existsBySubAndGatewayId(@Param("sub") String sub, @Param("gatewayId") String gatewayId);

    /**
     * Check if a user exists by userId and gatewayId (alias).
     */
    default boolean existsByUserIdAndGatewayId(String userId, String gatewayId) {
        return existsBySubAndGatewayId(userId, gatewayId);
    }

    // ==================== Gateway-based Lookups ====================

    /**
     * Find all users in a specific gateway.
     *
     * @param gatewayId the gateway identifier
     * @return List of users in the gateway
     */
    @Query("SELECT u FROM UserEntity u WHERE u.gatewayId = :gatewayId")
    List<UserEntity> findByGatewayId(@Param("gatewayId") String gatewayId);

    /**
     * Find all users in a specific gateway with pagination.
     *
     * @param gatewayId the gateway identifier
     * @param pageable pagination parameters
     * @return Page of users in the gateway
     */
    @Query("SELECT u FROM UserEntity u WHERE u.gatewayId = :gatewayId")
    Page<UserEntity> findByGatewayId(@Param("gatewayId") String gatewayId, Pageable pageable);

    /**
     * Count users in a specific gateway.
     *
     * @param gatewayId the gateway identifier
     * @return count of users in the gateway
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.gatewayId = :gatewayId")
    long countByGatewayId(@Param("gatewayId") String gatewayId);

    // ==================== Email-based Lookups ====================

    /**
     * Find a user by their email address.
     *
     * @param email the email address
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email")
    Optional<UserEntity> findByEmail(@Param("email") String email);

    /**
     * Find users by email and gateway.
     *
     * @param email the email address
     * @param gatewayId the gateway identifier
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.gatewayId = :gatewayId")
    Optional<UserEntity> findByEmailAndGatewayId(
            @Param("email") String email, @Param("gatewayId") String gatewayId);

    // ==================== Name-based Lookups ====================

    /**
     * Find users by given and family name within a gateway.
     *
     * @param givenName the given name
     * @param familyName the family name
     * @param gatewayId the gateway identifier
     * @return List of matching users
     */
    @Query("SELECT u FROM UserEntity u WHERE u.givenName = :givenName "
            + "AND u.familyName = :familyName AND u.gatewayId = :gatewayId")
    List<UserEntity> findByGivenNameAndFamilyNameAndGatewayId(
            @Param("givenName") String givenName,
            @Param("familyName") String familyName,
            @Param("gatewayId") String gatewayId);

    /**
     * Search users by name pattern within a gateway (partial matching).
     *
     * @param namePattern the name pattern (e.g., "%john%")
     * @param gatewayId the gateway identifier
     * @return List of matching users
     */
    @Query("SELECT u FROM UserEntity u WHERE u.gatewayId = :gatewayId "
            + "AND (LOWER(u.givenName) LIKE LOWER(:namePattern) "
            + "OR LOWER(u.familyName) LIKE LOWER(:namePattern) "
            + "OR LOWER(u.preferredUsername) LIKE LOWER(:namePattern))")
    List<UserEntity> searchByNameInGateway(
            @Param("namePattern") String namePattern, @Param("gatewayId") String gatewayId);

    // ==================== Sharing Registry Compatibility ====================

    /**
     * Find users by domain ID (alias for gatewayId, used by sharing registry).
     * In the unified model, domainId maps to gatewayId.
     *
     * @param domainId the domain identifier (same as gatewayId)
     * @return List of users in the domain
     */
    default List<UserEntity> findByDomainId(String domainId) {
        return findByGatewayId(domainId);
    }

    /**
     * Find a user by userId and domainId (sharing registry compatibility).
     * In the unified model, domainId maps to gatewayId.
     *
     * @param userId the user identifier (maps to sub)
     * @param domainId the domain identifier (same as gatewayId)
     * @return Optional containing the user if found
     */
    default Optional<UserEntity> findByUserIdAndDomainId(String userId, String domainId) {
        return findBySubAndGatewayId(userId, domainId);
    }

    /**
     * Check if a user exists by userId and domainId (sharing registry compatibility).
     *
     * @param userId the user identifier (maps to sub)
     * @param domainId the domain identifier (same as gatewayId)
     * @return true if the user exists
     */
    default boolean existsByUserIdAndDomainId(String userId, String domainId) {
        return existsBySubAndGatewayId(userId, domainId);
    }

    // ==================== Bulk Operations ====================

    /**
     * Delete all users in a specific gateway.
     *
     * @param gatewayId the gateway identifier
     * @return number of users deleted
     */
    @Modifying
    @Query("DELETE FROM UserEntity u WHERE u.gatewayId = :gatewayId")
    int deleteByGatewayId(@Param("gatewayId") String gatewayId);
}
