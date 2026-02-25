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
package org.apache.airavata.compute.resource.service;

import java.util.List;
import org.apache.airavata.accounting.service.AllocationProjectService;
import org.apache.airavata.compute.resource.mapper.ResourceBindingMapper;
import org.apache.airavata.compute.resource.mapper.ResourceMapper;
import org.apache.airavata.compute.resource.model.Resource;
import org.apache.airavata.compute.resource.model.ResourceBinding;
import org.apache.airavata.compute.resource.repository.ResourceBindingRepository;
import org.apache.airavata.compute.resource.repository.ResourceRepository;
import org.apache.airavata.core.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultResourceService implements ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    private final ResourceRepository resourceRepository;
    private final ResourceMapper mapper;
    private final ResourceBindingRepository bindingRepository;
    private final ResourceBindingMapper bindingMapper;
    private final AllocationProjectService allocationProjectService;

    public DefaultResourceService(
            ResourceRepository resourceRepository,
            ResourceMapper mapper,
            ResourceBindingRepository bindingRepository,
            ResourceBindingMapper bindingMapper,
            AllocationProjectService allocationProjectService) {
        this.resourceRepository = resourceRepository;
        this.mapper = mapper;
        this.bindingRepository = bindingRepository;
        this.bindingMapper = bindingMapper;
        this.allocationProjectService = allocationProjectService;
    }

    public Resource getResource(String resourceId) {
        return resourceRepository
                .findById(resourceId)
                .map(mapper::toModel)
                .orElse(null);
    }

    public List<Resource> getResources(String gatewayId) {
        return mapper.toModelList(resourceRepository.findByGatewayId(gatewayId));
    }

    @Transactional
    public String createResource(Resource resource) {
        resource.setResourceId(IdGenerator.ensureId(resource.getResourceId()));
        var entity = mapper.toEntity(resource);
        var saved = resourceRepository.save(entity);
        logger.debug("Created resource with id={}", saved.getResourceId());
        return saved.getResourceId();
    }

    @Transactional
    public void updateResource(String resourceId, Resource resource) {
        if (!resourceRepository.existsById(resourceId)) {
            throw new IllegalArgumentException("Resource not found: " + resourceId);
        }
        resource.setResourceId(resourceId);
        resourceRepository.save(mapper.toEntity(resource));
        logger.debug("Updated resource with id={}", resourceId);
    }

    @Transactional
    public void deleteResource(String resourceId) {
        if (!resourceRepository.existsById(resourceId)) {
            throw new IllegalArgumentException("Resource not found: " + resourceId);
        }
        resourceRepository.deleteById(resourceId);
        logger.debug("Deleted resource with id={}", resourceId);
    }

    // ========== Binding Operations ==========

    public ResourceBinding getBinding(String bindingId) {
        return bindingRepository.findById(bindingId).map(bindingMapper::toModel).orElse(null);
    }

    public List<ResourceBinding> getBindings(String gatewayId) {
        return bindingMapper.toModelList(bindingRepository.findByGatewayId(gatewayId));
    }

    public List<ResourceBinding> getBindingsByResource(String resourceId) {
        return bindingMapper.toModelList(bindingRepository.findByResourceId(resourceId));
    }

    public List<ResourceBinding> getBindingsByCredential(String credentialId) {
        return bindingMapper.toModelList(bindingRepository.findByCredentialId(credentialId));
    }

    @Transactional
    public String createBinding(ResourceBinding binding) {
        binding.setBindingId(IdGenerator.ensureId(binding.getBindingId()));
        var saved = bindingRepository.save(bindingMapper.toEntity(binding));
        logger.debug("Created resource binding id={}", saved.getBindingId());

        binding.setBindingId(saved.getBindingId());
        allocationProjectService.syncFromBinding(
                binding.getBindingId(), binding.getResourceId(), binding.getGatewayId(),
                binding.getCredentialId(), binding.getMetadata());

        return saved.getBindingId();
    }

    @Transactional
    public void updateBinding(String bindingId, ResourceBinding binding) {
        if (!bindingRepository.existsById(bindingId)) {
            throw new IllegalArgumentException("Resource binding not found with id: " + bindingId);
        }
        binding.setBindingId(bindingId);
        bindingRepository.save(bindingMapper.toEntity(binding));
        logger.debug("Updated resource binding id={}", bindingId);

        allocationProjectService.syncFromBinding(
                binding.getBindingId(), binding.getResourceId(), binding.getGatewayId(),
                binding.getCredentialId(), binding.getMetadata());
    }

    @Transactional
    public void deleteBinding(String bindingId) {
        if (!bindingRepository.existsById(bindingId)) {
            throw new IllegalArgumentException("Resource binding not found with id: " + bindingId);
        }
        allocationProjectService.cleanupForBinding(bindingId);
        bindingRepository.deleteById(bindingId);
        logger.debug("Deleted resource binding id={}", bindingId);
    }
}
