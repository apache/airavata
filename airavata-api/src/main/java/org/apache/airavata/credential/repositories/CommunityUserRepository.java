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
package org.apache.airavata.credential.repositories;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.credential.entities.CommunityUserEntity;
import org.apache.airavata.credential.entities.CommunityUserEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for CommunityUserEntity.
 */
@Repository
public interface CommunityUserRepository extends JpaRepository<CommunityUserEntity, CommunityUserEntityPK> {

    /**
     * Find community user by gateway ID and community user name.
     */
    Optional<CommunityUserEntity> findByGatewayIdAndCommunityUserName(String gatewayId, String communityUserName);

    /**
     * Find community user by gateway ID and token ID.
     */
    Optional<CommunityUserEntity> findByGatewayIdAndTokenId(String gatewayId, String tokenId);

    /**
     * Find all community users for a gateway.
     */
    List<CommunityUserEntity> findByGatewayId(String gatewayId);

    /**
     * Delete community user by gateway ID and community user name.
     */
    void deleteByGatewayIdAndCommunityUserName(String gatewayId, String communityUserName);

    /**
     * Delete community user by gateway ID, community user name, and token ID.
     */
    void deleteByGatewayIdAndCommunityUserNameAndTokenId(String gatewayId, String communityUserName, String tokenId);
}
