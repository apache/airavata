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
package org.apache.airavata.service.domain.impl;

import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.ProjectNotFoundException;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.experiment.ProjectSearchFields;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.RegistryService;
import org.apache.airavata.service.domain.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation of ProjectService.
 */
@Service
public class ProjectServiceImpl implements ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);
    
    private final RegistryService registryService;
    
    public ProjectServiceImpl(RegistryService registryService) {
        this.registryService = registryService;
    }
    
    private AiravataSystemException airavataSystemException(AiravataErrorType errorType, String message, Throwable cause) {
        return org.apache.airavata.common.exception.ExceptionHandlerUtil.wrapAsAiravataException(errorType, message, cause);
    }
    
    @Override
    public String createProject(String gatewayId, Project project) throws AiravataSystemException {
        try {
            return registryService.createProject(gatewayId, project);
        } catch (RegistryServiceException e) {
            String msg = "Error occurred while creating project: " + project.getName() + " " + project.getDescription()
                    + " " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public Project getProject(String projectId) throws AiravataSystemException, ProjectNotFoundException {
        try {
            return registryService.getProject(projectId);
        } catch (ProjectNotFoundException e) {
            throw e;
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving the project: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public void updateProject(String projectId, Project updatedProject) throws AiravataSystemException {
        try {
            registryService.updateProject(projectId, updatedProject);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating project: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public boolean deleteProject(String projectId) throws AiravataSystemException, ProjectNotFoundException {
        try {
            return registryService.deleteProject(projectId);
        } catch (ProjectNotFoundException e) {
            throw e;
        } catch (RegistryServiceException e) {
            String msg = "Error while removing the project: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
    
    @Override
    public List<Project> searchProjects(String gatewayId, String userName, ProjectSearchFields searchFields, int limit, int offset) throws AiravataSystemException {
        try {
            Map<ProjectSearchFields, String> filters = searchFields != null ? Map.of(searchFields, "") : Map.of();
            return registryService.searchProjects(gatewayId, userName, List.of(), filters, limit, offset);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving projects: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
}
