/**
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
package org.apache.airavata.registry.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.common.model.ClusterInfo;
import org.apache.airavata.common.model.ProjectResourceAccount;
import org.apache.airavata.registry.entities.expcatalog.ProjectEntity;
import org.apache.airavata.registry.entities.expcatalog.ProjectResourceAccountEntity;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.ProjectRepository;
import org.apache.airavata.registry.repositories.expcatalog.ProjectResourceAccountRepository;
import org.apache.airavata.service.cluster.ClusterInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for project–resource–account bindings: one account per compute resource per project.
 * Validates that accountName is among accounts discovered for the credential on the resource
 * (from cluster-info cache) when adding or updating a binding.
 */
@Service
@Transactional
public class ProjectResourceAccountService {

    private final ProjectResourceAccountRepository projectResourceAccountRepository;
    private final ProjectRepository projectRepository;
    @Autowired(required = false)
    private ClusterInfoService clusterInfoService;

    public ProjectResourceAccountService(
            ProjectResourceAccountRepository projectResourceAccountRepository,
            ProjectRepository projectRepository) {
        this.projectResourceAccountRepository = projectResourceAccountRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Add or update a project–resource–account binding. Validates that accountName is in the
     * cached cluster info for (gatewayId, credentialToken, computeResourceId) when cluster-info
     * is available; otherwise proceeds (caller should fetch cluster info first for validation).
     */
    public void addOrUpdateBinding(ProjectResourceAccount binding) throws RegistryException {
        if (binding.getProjectId() == null || binding.getProjectId().isBlank()
                || binding.getComputeResourceId() == null || binding.getComputeResourceId().isBlank()
                || binding.getGatewayId() == null || binding.getGatewayId().isBlank()
                || binding.getCredentialToken() == null || binding.getCredentialToken().isBlank()
                || binding.getAccountName() == null || binding.getAccountName().isBlank()) {
            throw new RegistryException("projectId, computeResourceId, gatewayId, credentialToken, and accountName are required");
        }
        ProjectEntity project = projectRepository.findById(binding.getProjectId())
                .orElseThrow(() -> new RegistryException("Project not found: " + binding.getProjectId()));
        if (!project.getGatewayId().equals(binding.getGatewayId())) {
            throw new RegistryException("gatewayId must match the project's gateway");
        }
        validateAccountForCredentialAndResource(
                binding.getGatewayId(),
                binding.getCredentialToken(),
                binding.getComputeResourceId(),
                binding.getAccountName());

        ProjectResourceAccountEntity entity = projectResourceAccountRepository
                .findByProjectIdAndComputeResourceId(binding.getProjectId(), binding.getComputeResourceId())
                .orElseGet(ProjectResourceAccountEntity::new);
        entity.setProjectId(binding.getProjectId());
        entity.setComputeResourceId(binding.getComputeResourceId());
        entity.setGatewayId(binding.getGatewayId());
        entity.setCredentialToken(binding.getCredentialToken());
        entity.setAccountName(binding.getAccountName());
        projectResourceAccountRepository.save(entity);
    }

    /**
     * Remove the binding for the given project and compute resource.
     */
    public boolean removeBinding(String projectId, String computeResourceId) throws RegistryException {
        Optional<ProjectResourceAccountEntity> existing =
                projectResourceAccountRepository.findByProjectIdAndComputeResourceId(projectId, computeResourceId);
        if (existing.isEmpty()) {
            return false;
        }
        projectResourceAccountRepository.delete(existing.get());
        return true;
    }

    /**
     * List all resource–account bindings for a project.
     */
    public List<ProjectResourceAccount> getBindings(String projectId) throws RegistryException {
        if (!projectRepository.existsById(projectId)) {
            throw new RegistryException("Project not found: " + projectId);
        }
        List<ProjectResourceAccountEntity> entities = projectResourceAccountRepository.findByProjectId(projectId);
        List<ProjectResourceAccount> result = new ArrayList<>();
        for (ProjectResourceAccountEntity e : entities) {
            result.add(entityToModel(e));
        }
        return result;
    }

    /**
     * Get the binding for the given project and compute resource, if any.
     */
    public Optional<ProjectResourceAccount> getBinding(String projectId, String computeResourceId) {
        return projectResourceAccountRepository.findByProjectIdAndComputeResourceId(projectId, computeResourceId)
                .map(this::entityToModel);
    }

    /**
     * Resolve the allocation account name for a project on a given compute resource.
     * Returns empty if the project has no binding for that resource.
     */
    public Optional<String> resolveAccountForProjectAndResource(String projectId, String computeResourceId) {
        return projectResourceAccountRepository.findByProjectIdAndComputeResourceId(projectId, computeResourceId)
                .map(ProjectResourceAccountEntity::getAccountName);
    }

    private void validateAccountForCredentialAndResource(
            String gatewayId, String credentialToken, String computeResourceId, String accountName)
            throws RegistryException {
        if (clusterInfoService == null) {
            return;
        }
        Optional<ClusterInfo> cached = clusterInfoService.getCached(gatewayId, credentialToken, computeResourceId);
        if (cached.isEmpty()) {
            throw new RegistryException(
                    "No cluster info cached for this credential and resource. Fetch cluster info first (POST /api/v1/cluster-info/fetch) and ensure accountName is one of the returned accounts.");
        }
        ClusterInfo info = cached.get();
        if (!info.getAccounts().contains(accountName)) {
            throw new RegistryException(
                    "accountName '" + accountName + "' is not among the accounts for this credential on this resource. Cached accounts: " + info.getAccountsList());
        }
    }

    private ProjectResourceAccount entityToModel(ProjectResourceAccountEntity e) {
        ProjectResourceAccount m = new ProjectResourceAccount();
        m.setProjectId(e.getProjectId());
        m.setComputeResourceId(e.getComputeResourceId());
        m.setGatewayId(e.getGatewayId());
        m.setCredentialToken(e.getCredentialToken());
        m.setAccountName(e.getAccountName());
        return m;
    }
}
