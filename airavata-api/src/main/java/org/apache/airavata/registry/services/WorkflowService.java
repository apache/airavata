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

import com.github.dozermapper.core.Mapper;
import java.util.List;
import org.apache.airavata.model.workflow.AiravataWorkflow;
import org.apache.airavata.registry.entities.airavataworkflowcatalog.AiravataWorkflowEntity;
import org.apache.airavata.registry.exceptions.WorkflowCatalogException;
import org.apache.airavata.registry.repositories.workflowcatalog.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final Mapper mapper;

    public WorkflowService(WorkflowRepository workflowRepository, Mapper mapper) {
        this.workflowRepository = workflowRepository;
        this.mapper = mapper;
    }

    public void registerWorkflow(AiravataWorkflow workflow, String experimentId) throws WorkflowCatalogException {
        AiravataWorkflowEntity entity = mapper.map(workflow, AiravataWorkflowEntity.class);
        entity.setExperimentId(experimentId);
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
        return mapper.map(entity, AiravataWorkflow.class);
    }
}
