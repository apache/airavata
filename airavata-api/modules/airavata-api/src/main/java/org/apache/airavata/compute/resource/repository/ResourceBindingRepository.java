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
package org.apache.airavata.compute.resource.repository;

import java.util.List;
import org.apache.airavata.compute.resource.entity.ResourceBindingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ResourceBindingEntity}.
 *
 * <p>Provides query methods for bindings that associate a credential with a compute
 * or storage resource within a gateway, including the login username used for authentication.
 * All finder methods use Spring Data JPA method naming conventions for automatic
 * query derivation.
 */
@Repository
public interface ResourceBindingRepository extends JpaRepository<ResourceBindingEntity, String> {

    /**
     * Find all resource bindings belonging to a specific gateway.
     *
     * @param gatewayId the gateway identifier
     * @return list of bindings for the gateway, empty list if none found
     */
    List<ResourceBindingEntity> findByGatewayId(String gatewayId);

    /**
     * Find all bindings associated with a specific resource.
     *
     * @param resourceId the resource identifier
     * @return list of bindings for the resource, empty list if none found
     */
    List<ResourceBindingEntity> findByResourceId(String resourceId);

    /**
     * Find all bindings associated with a specific credential.
     *
     * @param credentialId the credential identifier
     * @return list of bindings for the credential, empty list if none found
     */
    List<ResourceBindingEntity> findByCredentialId(String credentialId);

    /**
     * Find all bindings for a specific resource within a gateway.
     *
     * @param gatewayId  the gateway identifier
     * @param resourceId the resource identifier
     * @return list of bindings matching the gateway and resource, empty list if none found
     */
    List<ResourceBindingEntity> findByGatewayIdAndResourceId(String gatewayId, String resourceId);
}
