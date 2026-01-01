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
package org.apache.airavata.registry.services;

import java.util.List;
import org.apache.airavata.common.model.AiravataWorkflow;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.AiravataWorkflowEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.WorkflowApplicationEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.WorkflowConnectionEntity;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.WorkflowHandlerEntity;
import org.apache.airavata.registry.exception.WorkflowCatalogException;
import org.apache.airavata.registry.mappers.AiravataWorkflowMapper;
import org.apache.airavata.registry.repositories.workflowcatalog.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final AiravataWorkflowMapper airavataWorkflowMapper;

    public WorkflowService(WorkflowRepository workflowRepository, AiravataWorkflowMapper airavataWorkflowMapper) {
        this.workflowRepository = workflowRepository;
        this.airavataWorkflowMapper = airavataWorkflowMapper;
    }

    public void registerWorkflow(AiravataWorkflow workflow, String experimentId) throws WorkflowCatalogException {
        AiravataWorkflowEntity entity = airavataWorkflowMapper.toEntity(workflow);
        entity.setExperimentId(experimentId);
        // Generate workflow ID if not already set
        if (entity.getId() == null || entity.getId().isEmpty()) {
            String workflowId = org.apache.airavata.common.utils.AiravataUtils.getId(
                    workflow.getDescription() != null ? workflow.getDescription() : "workflow");
            entity.setId(workflowId);
        }
        final String workflowId = entity.getId();
        // Set workflowId on all applications and generate IDs if needed
        if (entity.getApplications() != null) {
            for (WorkflowApplicationEntity application : entity.getApplications()) {
                // Generate application ID if not set
                if (application.getId() == null || application.getId().isEmpty()) {
                    String applicationId = org.apache.airavata.common.utils.AiravataUtils.getId("application");
                    application.setId(applicationId);
                }
                // Set workflowId to match the workflow's ID (required for composite key)
                application.setWorkflowId(workflowId);
            }
        }
        // Set workflowId on all handlers and generate IDs if needed
        if (entity.getHandlers() != null) {
            for (WorkflowHandlerEntity handler : entity.getHandlers()) {
                // Generate handler ID if not set
                if (handler.getId() == null || handler.getId().isEmpty()) {
                    String handlerId = org.apache.airavata.common.utils.AiravataUtils.getId("handler");
                    handler.setId(handlerId);
                }
                // Set workflowId to match the workflow's ID (required for composite key)
                handler.setWorkflowId(workflowId);
            }
        }
        // Set workflowId on all connections and generate IDs if needed
        if (entity.getConnections() != null) {
            for (WorkflowConnectionEntity connection : entity.getConnections()) {
                // Generate connection ID if not set
                if (connection.getId() == null || connection.getId().isEmpty()) {
                    String connectionId = org.apache.airavata.common.utils.AiravataUtils.getId("connection");
                    connection.setId(connectionId);
                }
                // Set workflowId to match the workflow's ID (required for composite key)
                connection.setWorkflowId(workflowId);
            }
        }
        workflowRepository.save(entity);
    }

    public String getWorkflowId(String experimentId) throws WorkflowCatalogException {
        List<AiravataWorkflowEntity> entities = workflowRepository.findByExperimentId(experimentId);
        if (entities.isEmpty()) return null;
        return entities.get(0).getId();
    }

    public AiravataWorkflow getWorkflow(String workflowId) throws WorkflowCatalogException {
        AiravataWorkflowEntity entity = workflowRepository.findById(workflowId).orElse(null);
        if (entity == null) return null;
        AiravataWorkflow workflow = airavataWorkflowMapper.toModel(entity);
        // Note: Nested lists (applications, handlers, connections, statuses, errors) are already
        // mapped by MapStruct through the entity relationships, but we need to ensure they're set
        // if they exist in the entity
        if (entity.getApplications() != null) {
            // Applications are already mapped through the entity relationship
            // MapStruct will handle the conversion if we add proper mappers later
        }
        if (entity.getHandlers() != null) {
            // Handlers are already mapped through the entity relationship
        }
        if (entity.getConnections() != null) {
            // Connections are already mapped through the entity relationship
        }
        if (entity.getStatuses() != null) {
            // Statuses are already mapped through the entity relationship
        }
        if (entity.getErrors() != null) {
            // Errors are already mapped through the entity relationship
        }
        return workflow;
    }
}
