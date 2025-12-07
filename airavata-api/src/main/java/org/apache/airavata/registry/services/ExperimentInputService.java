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
import org.apache.airavata.registry.entities.expcatalog.ExperimentInputEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentInputRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExperimentInputService {
    @Autowired
    private ExperimentInputRepository experimentInputRepository;

    @Autowired
    private Mapper mapper;

    public String addExperimentInputs(List<InputDataObjectType> inputs, String experimentId) throws RegistryException {
        for (InputDataObjectType input : inputs) {
            ExperimentInputEntity entity = mapper.map(input, ExperimentInputEntity.class);
            entity.setExperimentId(experimentId);
            experimentInputRepository.save(entity);
        }
        return experimentId;
    }

    public void updateExperimentInputs(List<InputDataObjectType> inputs, String experimentId) throws RegistryException {
        // Delete existing inputs and add new ones
        List<ExperimentInputEntity> existing = experimentInputRepository.findByExperimentId(experimentId);
        experimentInputRepository.deleteAll(existing);
        addExperimentInputs(inputs, experimentId);
    }

    public List<InputDataObjectType> getExperimentInputs(String experimentId) throws RegistryException {
        List<ExperimentInputEntity> entities = experimentInputRepository.findByExperimentId(experimentId);
        List<InputDataObjectType> result = new ArrayList<>();
        entities.forEach(e -> result.add(mapper.map(e, InputDataObjectType.class)));
        return result;
    }
}
