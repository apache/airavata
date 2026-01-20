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
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.registry.entities.expcatalog.ProcessOutputEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.OutputDataObjectTypeMapper;
import org.apache.airavata.registry.repositories.expcatalog.ProcessOutputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProcessOutputService {
    private final ProcessOutputRepository processOutputRepository;
    private final OutputDataObjectTypeMapper outputDataObjectTypeMapper;

    public ProcessOutputService(
            ProcessOutputRepository processOutputRepository, OutputDataObjectTypeMapper outputDataObjectTypeMapper) {
        this.processOutputRepository = processOutputRepository;
        this.outputDataObjectTypeMapper = outputDataObjectTypeMapper;
    }

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryException {
        List<ProcessOutputEntity> entities = processOutputRepository.findByProcessId(processId);
        return outputDataObjectTypeMapper.toModelListFromProcess(entities);
    }

    public void addProcessOutputs(List<OutputDataObjectType> outputs, String processId) throws RegistryException {
        for (OutputDataObjectType output : outputs) {
            ProcessOutputEntity entity = outputDataObjectTypeMapper.toEntityFromProcess(output);
            entity.setProcessId(processId);
            // Note: We don't call entity.setProcess() because the @JoinColumn has insertable=false.
            // The processId field is already set and is the only field that gets persisted.
            processOutputRepository.save(entity);
        }
    }

    public void updateProcessOutputs(List<OutputDataObjectType> outputs, String processId) throws RegistryException {
        List<ProcessOutputEntity> existing = processOutputRepository.findByProcessId(processId);
        if (!existing.isEmpty()) {
            processOutputRepository.deleteAll(existing);
            processOutputRepository.flush(); // Ensure deletes are executed before inserting new ones with same IDs
        }
        addProcessOutputs(outputs, processId);
    }
}
