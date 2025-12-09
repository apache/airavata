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
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.registry.entities.expcatalog.ProcessInputEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.ProcessInputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProcessInputService {
    private final ProcessInputRepository processInputRepository;
    private final Mapper mapper;

    public ProcessInputService(ProcessInputRepository processInputRepository, Mapper mapper) {
        this.processInputRepository = processInputRepository;
        this.mapper = mapper;
    }

    public String addProcessInputs(List<InputDataObjectType> inputs, String processId) throws RegistryException {
        for (InputDataObjectType input : inputs) {
            ProcessInputEntity entity = mapper.map(input, ProcessInputEntity.class);
            entity.setProcessId(processId);
            processInputRepository.save(entity);
        }
        return processId;
    }

    public void updateProcessInputs(List<InputDataObjectType> inputs, String processId) throws RegistryException {
        // Delete existing inputs and add new ones
        List<ProcessInputEntity> existing = processInputRepository.findByProcessId(processId);
        processInputRepository.deleteAll(existing);
        addProcessInputs(inputs, processId);
    }

    public List<InputDataObjectType> getProcessInputs(String processId) throws RegistryException {
        List<ProcessInputEntity> entities = processInputRepository.findByProcessId(processId);
        List<InputDataObjectType> result = new ArrayList<>();
        entities.forEach(e -> result.add(mapper.map(e, InputDataObjectType.class)));
        return result;
    }
}
