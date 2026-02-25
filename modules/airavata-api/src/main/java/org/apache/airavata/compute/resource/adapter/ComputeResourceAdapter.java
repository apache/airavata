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
package org.apache.airavata.compute.resource.adapter;

import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.compute.resource.model.ComputeCapability;
import org.apache.airavata.compute.resource.model.JobSubmissionProtocol;
import org.apache.airavata.compute.resource.model.Resource;
import org.apache.airavata.compute.resource.model.ResourceJobManagerType;
import org.apache.airavata.protocol.ResourceLookup;
import org.apache.airavata.compute.resource.entity.ResourceEntity;
import org.apache.airavata.compute.resource.repository.ResourceRepository;
import org.apache.airavata.storage.resource.model.DataMovementProtocol;
import org.apache.airavata.storage.resource.model.StorageCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter service that maps compute and storage resource entities to the clean {@link Resource}
 * domain model used by protocol adapters and the workflow engine.
 *
 * <p>Implements {@link ResourceLookup} so that protocol adapters can resolve connection details
 * (hostname, port, capabilities) from resource IDs without depending on JPA entities directly.
 *
 * <p>All methods are read-only and return {@code null} or empty collections when a resource
 * is not found.
 */
@Service
@Transactional(readOnly = true)
public class ComputeResourceAdapter implements ResourceLookup {

    private static final Logger log = LoggerFactory.getLogger(ComputeResourceAdapter.class);

    private final ResourceRepository resourceRepository;

    public ComputeResourceAdapter(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    private Resource toModel(ResourceEntity entity) {
        var model = new Resource();
        model.setResourceId(entity.getResourceId());
        model.setGatewayId(entity.getGatewayId());
        model.setName(entity.getName());
        model.setHostName(entity.getHostName());
        model.setPort(entity.getPort() != null ? entity.getPort() : 22);
        model.setDescription(entity.getDescription());
        model.setCapabilities(entity.getCapabilities());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        return model;
    }

    // -------------------------------------------------------------------------
    // Resource lookup
    // -------------------------------------------------------------------------

    @Override
    public Resource getResource(String resourceId) {
        return resourceRepository.findById(resourceId)
                .map(this::toModel)
                .orElseGet(() -> {
                    log.debug("getResource: no resource found for id={}", resourceId);
                    return null;
                });
    }

    // -------------------------------------------------------------------------
    // Resource name maps
    // -------------------------------------------------------------------------

    /**
     * Return a map of all resource IDs to resource names.
     *
     * @return map; empty if no resources exist
     */
    public Map<String, String> getAllComputeResourceNames() {
        return resourceRepository.findAll().stream()
                .collect(Collectors.toMap(ResourceEntity::getResourceId, ResourceEntity::getName));
    }

    /**
     * Return a map of resource IDs to resource names for resources that have a storage capability.
     *
     * @return map; empty if no storage resources exist
     */
    public Map<String, String> getAllStorageResourceNames() {
        return resourceRepository.findAll().stream()
                .filter(r -> r.getCapabilities() != null && r.getCapabilities().getStorage() != null)
                .collect(Collectors.toMap(ResourceEntity::getResourceId, ResourceEntity::getName));
    }

    // -------------------------------------------------------------------------
    // Enum mapping helpers
    // -------------------------------------------------------------------------

    /**
     * Map the protocol string from {@link ComputeCapability} to the
     * {@link JobSubmissionProtocol} enum.
     *
     * <p>Recognised values (case-insensitive): {@code "SSH_FORK"} →
     * {@link JobSubmissionProtocol#SSH_FORK}, {@code "CLOUD"} →
     * {@link JobSubmissionProtocol#CLOUD}. All other values map to
     * {@link JobSubmissionProtocol#SSH}.
     *
     * @param protocol protocol string from the capability model
     * @return matching enum constant
     */
    public JobSubmissionProtocol mapJobSubmissionProtocol(String protocol) {
        if (protocol == null) {
            return JobSubmissionProtocol.SSH;
        }
        return switch (protocol.toUpperCase()) {
            case "SSH_FORK" -> JobSubmissionProtocol.SSH_FORK;
            case "CLOUD" -> JobSubmissionProtocol.CLOUD;
            default -> JobSubmissionProtocol.SSH;
        };
    }

    /**
     * Map the protocol string from {@link StorageCapability} to the
     * {@link DataMovementProtocol} enum.
     *
     * <p>Recognised values (case-insensitive): {@code "SFTP"} →
     * {@link DataMovementProtocol#SFTP}, {@code "LOCAL"} →
     * {@link DataMovementProtocol#LOCAL}. All other values map to
     * {@link DataMovementProtocol#SCP}.
     *
     * @param protocol protocol string from the capability model
     * @return matching enum constant
     */
    public DataMovementProtocol mapDataMovementProtocol(String protocol) {
        if (protocol == null) {
            return DataMovementProtocol.SCP;
        }
        return switch (protocol.toUpperCase()) {
            case "SFTP" -> DataMovementProtocol.SFTP;
            case "LOCAL" -> DataMovementProtocol.LOCAL;
            default -> DataMovementProtocol.SCP;
        };
    }

    public ResourceJobManagerType mapResourceJobManagerType(String jobManagerType) {
        return ResourceJobManagerType.fromString(jobManagerType);
    }
}
