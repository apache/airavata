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
package org.apache.airavata.accounting.repository;

import java.util.List;
import org.apache.airavata.accounting.entity.CredentialAllocationProjectEntity;
import org.apache.airavata.accounting.entity.CredentialAllocationProjectPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link CredentialAllocationProjectEntity}.
 *
 * <p>Manages the join records that grant credentials access to HPC allocation projects.
 * The entity uses a composite primary key ({@link CredentialAllocationProjectPK}) composed of
 * {@code credentialId} and {@code allocationProjectId}.
 *
 * <p>Simple lookups use Spring Data JPA method naming conventions. The
 * {@link #deleteByBindingId(String)} method uses an explicit {@code @Query} because
 * deletion by a non-PK field cannot be derived automatically.
 */
@Repository
public interface CredentialAllocationProjectRepository
        extends JpaRepository<CredentialAllocationProjectEntity, CredentialAllocationProjectPK> {

    /**
     * Find all credential-allocation-project memberships for a specific allocation project.
     *
     * @param allocationProjectId the allocation project identifier
     * @return list of memberships for the allocation project, empty list if none found
     */
    List<CredentialAllocationProjectEntity> findByAllocationProjectId(String allocationProjectId);

    /**
     * Find all allocation projects a specific credential is a member of.
     *
     * @param credentialId the credential identifier
     * @return list of memberships for the credential, empty list if none found
     */
    List<CredentialAllocationProjectEntity> findByCredentialId(String credentialId);

    /**
     * Find all credential-allocation-project memberships backed by a specific credential-resource binding.
     *
     * @param bindingId the credential-resource binding identifier
     * @return list of memberships referencing the binding, empty list if none found
     */
    List<CredentialAllocationProjectEntity> findByBindingId(String bindingId);

    /**
     * Delete all credential-allocation-project memberships associated with a given
     * credential-resource binding. Called when a binding is removed to cascade
     * removal of orphaned allocation project memberships.
     *
     * @param bindingId the credential-resource binding identifier
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM CredentialAllocationProjectEntity u WHERE u.bindingId = :bindingId")
    void deleteByBindingId(@Param("bindingId") String bindingId);
}
