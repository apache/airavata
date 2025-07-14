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
package org.apache.airavata.registry.core.repositories.expcatalog;

import com.github.dozermapper.core.Mapper;
import java.util.Collections;
import java.util.List;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.process.ProcessWorkflow;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessWorkflowEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessWorkflowPK;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessWorkflowRepository
        extends ExpCatAbstractRepository<ProcessWorkflow, ProcessWorkflowEntity, ProcessWorkflowPK> {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInputRepository.class);

    public ProcessWorkflowRepository() {
        super(ProcessWorkflow.class, ProcessWorkflowEntity.class);
    }

    protected void saveProcessWorkflow(List<ProcessWorkflow> processWorkflows, String processId)
            throws RegistryException {

        for (ProcessWorkflow processWorkflow : processWorkflows) {
            Mapper mapper = ObjectMapperSingleton.getInstance();
            ProcessWorkflowEntity processWorkflowEntity = mapper.map(processWorkflow, ProcessWorkflowEntity.class);

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
