/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessOutputEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessOutputPK;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProcessOutputRepository extends ExpCatAbstractRepository<OutputDataObjectType, ProcessOutputEntity, ProcessOutputPK> {
    private final static Logger logger = LoggerFactory.getLogger(ProcessOutputRepository.class);

    public ProcessOutputRepository() { super(OutputDataObjectType.class, ProcessOutputEntity.class); }

    protected void saveProcessOutput(List<OutputDataObjectType> processOutputs, String processId) throws RegistryException {

        for (OutputDataObjectType output : processOutputs) {
            Mapper mapper = ObjectMapperSingleton.getInstance();
            ProcessOutputEntity processOutputEntity = mapper.map(output, ProcessOutputEntity.class);

            if (processOutputEntity.getProcessId() == null) {
                logger.debug("Setting the ProcessOutputEntity's ProcesstId");
                processOutputEntity.setProcessId(processId);
            }

            execute(entityManager -> entityManager.merge(processOutputEntity));
        }

    }

    public String addProcessOutputs(List<OutputDataObjectType> processOutputs, String processId) throws RegistryException {
        saveProcessOutput(processOutputs, processId);
        return processId;
    }

    public void updateProcessOutputs(List<OutputDataObjectType> updatedProcessOutputs, String processId) throws RegistryException {
        saveProcessOutput(updatedProcessOutputs, processId);
    }

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        ProcessModel processModel = processRepository.getProcess(processId);
        return processModel.getProcessOutputs();
    }

}
