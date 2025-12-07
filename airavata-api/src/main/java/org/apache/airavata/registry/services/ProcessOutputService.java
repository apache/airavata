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
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.registry.entities.expcatalog.ProcessOutputEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.ProcessOutputRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProcessOutputService {
    @Autowired
    private ProcessOutputRepository processOutputRepository;

    @Autowired
    private Mapper mapper;

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryException {
        List<ProcessOutputEntity> entities = processOutputRepository.findByProcessId(processId);
        List<OutputDataObjectType> result = new ArrayList<>();
        entities.forEach(e -> result.add(mapper.map(e, OutputDataObjectType.class)));
        return result;
    }

    public void addProcessOutputs(List<OutputDataObjectType> outputs, String processId) throws RegistryException {
        for (OutputDataObjectType output : outputs) {
            ProcessOutputEntity entity = mapper.map(output, ProcessOutputEntity.class);
            entity.setProcessId(processId);
            processOutputRepository.save(entity);
        }
    }

    public void updateProcessOutputs(List<OutputDataObjectType> outputs, String processId) throws RegistryException {
        // Delete existing outputs and add new ones
        List<ProcessOutputEntity> existing = processOutputRepository.findByProcessId(processId);
        processOutputRepository.deleteAll(existing);
        addProcessOutputs(outputs, processId);
    }
}
