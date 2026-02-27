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
package org.apache.airavata.accounting.service;

import java.util.List;
import java.util.Map;
import org.apache.airavata.accounting.entity.AllocationProjectEntity;
import org.apache.airavata.accounting.entity.CredentialAllocationProjectEntity;
import org.apache.airavata.accounting.entity.CredentialAllocationProjectPK;
import org.apache.airavata.accounting.mapper.AllocationProjectMapper;
import org.apache.airavata.accounting.model.AllocationProject;
import org.apache.airavata.accounting.repository.AllocationProjectRepository;
import org.apache.airavata.accounting.repository.CredentialAllocationProjectRepository;
import org.apache.airavata.core.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link AllocationProjectService}.
 *
 * <p>Manages the lifecycle of HPC allocation projects (charge accounts) and the credential
 * memberships that grant credentials access to those projects. The service is the single point
 * of truth for the {@code allocation_project} and {@code credential_allocation_project} tables.
 *
 * <p>The key domain operations are:
 * <ul>
 *   <li>{@link #syncFromBinding}: Called whenever a resource binding is created or updated.
 *       Extracts a project code from the binding metadata, finds or creates the corresponding
 *       allocation project, then upserts the credential membership record for that binding.
 *       Idempotent — safe to call on every binding save.</li>
 *   <li>{@link #cleanupForBinding}: Called when a resource binding is deleted. Removes all
 *       credential-allocation-project membership records that were provisioned through the
 *       deleted binding.</li>
 *   <li>{@link #getProjectCredentials}: Returns the credential IDs of all members of a given
 *       allocation project.</li>
 * </ul>
 */
@Service
@Transactional
public class DefaultAllocationProjectService implements AllocationProjectService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAllocationProjectService.class);

    /**
     * Metadata key used to carry the scheduler-level project code (e.g. a SLURM
     * {@code --account} value) in the resource binding metadata map.
     */
    private static final String METADATA_KEY_PROJECT_CODE = "allocationProjectNumber";

    private final AllocationProjectRepository allocationProjectRepository;
    private final CredentialAllocationProjectRepository credentialAllocationProjectRepository;
    private final AllocationProjectMapper mapper;

    public DefaultAllocationProjectService(
            AllocationProjectRepository allocationProjectRepository,
            CredentialAllocationProjectRepository credentialAllocationProjectRepository,
            AllocationProjectMapper mapper) {
        this.allocationProjectRepository = allocationProjectRepository;
        this.credentialAllocationProjectRepository = credentialAllocationProjectRepository;
        this.mapper = mapper;
    }

    // -------------------------------------------------------------------------
    // Basic CRUD
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    @Override
    public AllocationProject getAllocationProject(String allocationProjectId) {
        return allocationProjectRepository
                .findById(allocationProjectId)
                .map(mapper::toModel)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AllocationProject> getAllocationProjects(String gatewayId) {
        return mapper.toModelList(allocationProjectRepository.findByGatewayId(gatewayId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<AllocationProject> getAllocationProjectsByResource(String resourceId) {
        return mapper.toModelList(allocationProjectRepository.findByResourceId(resourceId));
    }

    @Transactional(readOnly = true)
    @Override
    public AllocationProject findByProjectCodeAndResource(String projectCode, String resourceId) {
        return allocationProjectRepository
                .findByProjectCodeAndResourceId(projectCode, resourceId)
                .map(mapper::toModel)
                .orElse(null);
    }

    @Override
    public String createAllocationProject(AllocationProject project) {
        project.setAllocationProjectId(IdGenerator.ensureId(project.getAllocationProjectId()));
        AllocationProjectEntity entity = mapper.toEntity(project);
        AllocationProjectEntity saved = allocationProjectRepository.save(entity);
        logger.debug(
                "Created allocation project id={} code={} resource={}",
                saved.getAllocationProjectId(),
                saved.getProjectCode(),
                saved.getResourceId());
        return saved.getAllocationProjectId();
    }

    @Override
    public void deleteAllocationProject(String allocationProjectId) {
        allocationProjectRepository.deleteById(allocationProjectId);
        logger.debug("Deleted allocation project id={}", allocationProjectId);
    }

    // -------------------------------------------------------------------------
    // Binding lifecycle hooks
    // -------------------------------------------------------------------------

    /**
     * Synchronise allocation project state with a resource binding.
     *
     * <p>When a resource binding is created or updated the binding may carry an
     * {@code allocationProjectNumber} in its metadata map. This method:
     * <ol>
     *   <li>Extracts the project code from metadata; returns immediately if absent or blank.</li>
     *   <li>Finds the existing {@link AllocationProjectEntity} for that
     *       {@code (projectCode, resourceId)} pair, or creates a new one.</li>
     *   <li>Upserts the {@link CredentialAllocationProjectEntity} record that links
     *       {@code credentialId} to the allocation project via the given {@code bindingId}.</li>
     * </ol>
     *
     * <p>The operation is idempotent: calling it repeatedly with the same arguments produces
     * the same result.
     *
     * @param bindingId    the credential-resource binding identifier
     * @param resourceId   the compute resource identifier
     * @param gatewayId    the gateway owning the allocation project
     * @param credentialId the credential being granted project membership
     * @param metadata     the binding metadata map; may be null
     */
    @Override
    public void syncFromBinding(
            String bindingId, String resourceId, String gatewayId, String credentialId, Map<String, Object> metadata) {

        if (metadata == null) {
            return;
        }
        Object raw = metadata.get(METADATA_KEY_PROJECT_CODE);
        if (raw == null) {
            return;
        }
        String projectCode = raw.toString().trim();
        if (projectCode.isBlank()) {
            return;
        }

        // Find or create the allocation project for this (projectCode, resourceId) pair.
        AllocationProjectEntity project = allocationProjectRepository
                .findByProjectCodeAndResourceId(projectCode, resourceId)
                .orElseGet(() -> {
                    var newProject = new AllocationProjectEntity();
                    newProject.setAllocationProjectId(IdGenerator.ensureId(null));
                    newProject.setProjectCode(projectCode);
                    newProject.setResourceId(resourceId);
                    newProject.setGatewayId(gatewayId);
                    AllocationProjectEntity saved = allocationProjectRepository.save(newProject);
                    logger.info(
                            "Auto-created allocation project id={} code={} resource={}",
                            saved.getAllocationProjectId(),
                            projectCode,
                            resourceId);
                    return saved;
                });

        // Upsert the credential membership for this binding.
        CredentialAllocationProjectPK pk =
                new CredentialAllocationProjectPK(credentialId, project.getAllocationProjectId());

        if (!credentialAllocationProjectRepository.existsById(pk)) {
            var membership = new CredentialAllocationProjectEntity();
            membership.setCredentialId(credentialId);
            membership.setAllocationProjectId(project.getAllocationProjectId());
            membership.setBindingId(bindingId);
            credentialAllocationProjectRepository.save(membership);
            logger.debug(
                    "Linked credential={} to allocation project={} via binding={}",
                    credentialId,
                    project.getAllocationProjectId(),
                    bindingId);
        } else {
            logger.debug(
                    "Credential={} already member of allocation project={} — skipping insert",
                    credentialId,
                    project.getAllocationProjectId());
        }
    }

    /**
     * Remove all credential-allocation-project memberships provisioned through the given binding.
     *
     * <p>Called when a resource binding is deleted so that orphaned membership records are
     * cleaned up. Allocation project records themselves are not deleted — a project may still
     * be referenced by other bindings.
     *
     * @param bindingId the credential-resource binding being removed
     */
    @Override
    public void cleanupForBinding(String bindingId) {
        credentialAllocationProjectRepository.deleteByBindingId(bindingId);
        logger.debug("Removed credential allocation project memberships for binding={}", bindingId);
    }

    // -------------------------------------------------------------------------
    // Credential membership queries
    // -------------------------------------------------------------------------

    /**
     * Return the credential IDs of all members of the given allocation project.
     *
     * @param allocationProjectId the allocation project identifier
     * @return list of credential ID strings; empty if the project has no members
     */
    @Transactional(readOnly = true)
    @Override
    public List<String> getProjectCredentials(String allocationProjectId) {
        return credentialAllocationProjectRepository.findByAllocationProjectId(allocationProjectId).stream()
                .map(CredentialAllocationProjectEntity::getCredentialId)
                .toList();
    }
}
