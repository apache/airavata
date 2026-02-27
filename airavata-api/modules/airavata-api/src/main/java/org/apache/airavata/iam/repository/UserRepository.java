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
package org.apache.airavata.iam.repository;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.iam.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * User repository. UserEntity stores minimal data (userId, sub, gatewayId, personalGroupId, createdAt).
 * Profile data (name, email, etc.) is fetched from IAM on demand.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    @Query("SELECT u FROM UserEntity u WHERE u.userId = :userId")
    Optional<UserEntity> findByUserId(@Param("userId") String userId);

    @Query("SELECT u FROM UserEntity u WHERE u.sub = :sub AND u.gatewayId = :gatewayId")
    Optional<UserEntity> findBySubAndGatewayId(@Param("sub") String sub, @Param("gatewayId") String gatewayId);

    default Optional<UserEntity> findByUserIdAndGatewayId(String userId, String gatewayId) {
        return findBySubAndGatewayId(userId, gatewayId);
    }

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u "
            + "WHERE u.sub = :sub AND u.gatewayId = :gatewayId")
    boolean existsBySubAndGatewayId(@Param("sub") String sub, @Param("gatewayId") String gatewayId);

    default boolean existsByUserIdAndGatewayId(String userId, String gatewayId) {
        return existsBySubAndGatewayId(userId, gatewayId);
    }

    @Query("SELECT u FROM UserEntity u WHERE u.gatewayId = :gatewayId")
    List<UserEntity> findByGatewayId(@Param("gatewayId") String gatewayId);

    @Query("SELECT u FROM UserEntity u WHERE u.gatewayId = :gatewayId AND u.sub IN :subs")
    List<UserEntity> findByGatewayIdAndSubIn(@Param("gatewayId") String gatewayId, @Param("subs") List<String> subs);

    @Query("SELECT u FROM UserEntity u WHERE u.gatewayId = :gatewayId")
    Page<UserEntity> findByGatewayId(@Param("gatewayId") String gatewayId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.gatewayId = :gatewayId")
    long countByGatewayId(@Param("gatewayId") String gatewayId);

    default List<UserEntity> findByGatewayName(String gatewayName) {
        return findByGatewayId(gatewayName);
    }

    default List<UserEntity> findByGatewayNameAndSubIn(String gatewayName, List<String> subs) {
        return findByGatewayIdAndSubIn(gatewayName, subs);
    }

    default Page<UserEntity> findByGatewayName(String gatewayName, Pageable pageable) {
        return findByGatewayId(gatewayName, pageable);
    }

    default List<UserEntity> findByDomainId(String domainId) {
        return findByGatewayId(domainId);
    }

    default Optional<UserEntity> findByUserIdAndDomainId(String userId, String domainId) {
        return findBySubAndGatewayId(userId, domainId);
    }

    default boolean existsByUserIdAndDomainId(String userId, String domainId) {
        return existsBySubAndGatewayId(userId, domainId);
    }

    @Modifying
    @Query("DELETE FROM UserEntity u WHERE u.gatewayId = :gatewayId")
    int deleteByGatewayId(@Param("gatewayId") String gatewayId);

    default int deleteByGatewayName(String gatewayName) {
        return deleteByGatewayId(gatewayName);
    }
}
