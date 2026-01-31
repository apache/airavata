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
import org.apache.airavata.registry.entities.ResourceAccessGrantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for unified resource access grants (credential + compute resource + deployment settings).
 */
@Repository
public interface ResourceAccessGrantRepository extends JpaRepository<ResourceAccessGrantEntity, Long> {

    List<ResourceAccessGrantEntity> findByGatewayId(String gatewayId);

    List<ResourceAccessGrantEntity> findByGatewayIdAndEnabledTrue(String gatewayId);

    List<ResourceAccessGrantEntity> findByCredentialToken(String credentialToken);

    List<ResourceAccessGrantEntity> findByCredentialTokenAndEnabledTrue(String credentialToken);

    List<ResourceAccessGrantEntity> findByComputeResourceId(String computeResourceId);

    List<ResourceAccessGrantEntity> findByComputeResourceIdAndEnabledTrue(String computeResourceId);

    List<ResourceAccessGrantEntity> findByGatewayIdAndComputeResourceId(String gatewayId, String computeResourceId);

    Optional<ResourceAccessGrantEntity> findByGatewayIdAndCredentialTokenAndComputeResourceId(
            String gatewayId, String credentialToken, String computeResourceId);
}
