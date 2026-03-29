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
package org.apache.airavata.execution.repository;

import java.util.Collections;
import java.util.List;
import org.apache.airavata.execution.mapper.ExecutionMapper;
import org.apache.airavata.execution.model.ProcessWorkflowEntity;
import org.apache.airavata.execution.model.ProcessWorkflowPK;
import org.apache.airavata.execution.util.AbstractRepository;
import org.apache.airavata.execution.util.cpi.RegistryException;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.process.ProcessWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessWorkflowRepository
        extends AbstractRepository<ProcessWorkflow, ProcessWorkflowEntity, ProcessWorkflowPK> {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInputRepository.class);

    public ProcessWorkflowRepository() {
        super(ProcessWorkflow.class, ProcessWorkflowEntity.class);
    }

    @Override
    protected ProcessWorkflow toModel(ProcessWorkflowEntity entity) {
        return ExecutionMapper.INSTANCE.processWorkflowToModel(entity);
    }

    @Override
    protected ProcessWorkflowEntity toEntity(ProcessWorkflow model) {
        return ExecutionMapper.INSTANCE.processWorkflowToEntity(model);
    }

    protected void saveProcessWorkflow(List<ProcessWorkflow> processWorkflows, String processId)
            throws RegistryException {

        for (ProcessWorkflow processWorkflow : processWorkflows) {
            ProcessWorkflowEntity processWorkflowEntity =
                    ExecutionMapper.INSTANCE.processWorkflowToEntity(processWorkflow);

            if (processWorkflowEntity.getProcessId() == null) {
                logger.debug("Setting the ProcessWorkflowEntity's ProcessId");
                processWorkflowEntity.setProcessId(processId);
            }
            execute(entityManager -> entityManager.merge(processWorkflowEntity));
        }
    }

    public String addProcessWorkflow(ProcessWorkflow processWorkflow, String processId) throws RegistryException {
        saveProcessWorkflow(Collections.singletonList(processWorkflow), processId);
        return processId;
    }

    public void addProcessWorkflows(List<ProcessWorkflow> processWorkflows, String processId) throws RegistryException {
        saveProcessWorkflow(processWorkflows, processId);
    }

    public List<ProcessWorkflow> getProcessWorkflows(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        ProcessModel processModel = processRepository.getProcess(processId);
        return processModel.getProcessWorkflows();
    }
}
