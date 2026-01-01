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
import org.apache.airavata.common.model.ExperimentStatus;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.expcatalog.ExperimentStatusEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.ExperimentStatusMapper;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentStatusRepository;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("expCatalogTransactionManager")
public class ExperimentStatusService {
    private final ExperimentStatusRepository experimentStatusRepository;
    private final ExperimentStatusMapper experimentStatusMapper;

    public ExperimentStatusService(
            ExperimentStatusRepository experimentStatusRepository, ExperimentStatusMapper experimentStatusMapper) {
        this.experimentStatusRepository = experimentStatusRepository;
        this.experimentStatusMapper = experimentStatusMapper;
    }

    public String addExperimentStatus(ExperimentStatus experimentStatus, String experimentId) throws RegistryException {
        if (experimentStatus.getStatusId() == null) {
            experimentStatus.setStatusId(ExpCatalogUtils.getID("EXPERIMENT_STATE"));
        }
        if (experimentStatus.getTimeOfStateChange() == 0) {
            experimentStatus.setTimeOfStateChange(
                    AiravataUtils.getCurrentTimestamp().getTime());
        }
        ExperimentStatusEntity entity = experimentStatusMapper.toEntity(experimentStatus);
        entity.setExperimentId(experimentId);
        ExperimentStatusEntity saved = experimentStatusRepository.save(entity);
        return saved.getStatusId();
    }

    public ExperimentStatus getExperimentStatus(String experimentId) throws RegistryException {
        List<ExperimentStatusEntity> entities =
                experimentStatusRepository.findByExperimentIdOrderByTimeOfStateChangeDesc(experimentId);
        if (entities.isEmpty()) return null;
        return experimentStatusMapper.toModel(entities.get(0));
    }

    public String updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId)
            throws RegistryException {
        if (experimentStatus.getStatusId() == null) {
            experimentStatus.setStatusId(ExpCatalogUtils.getID("EXPERIMENT_STATE"));
        }
        if (experimentStatus.getTimeOfStateChange() == 0) {
            experimentStatus.setTimeOfStateChange(
                    AiravataUtils.getCurrentTimestamp().getTime());
        }
        ExperimentStatusEntity entity = experimentStatusMapper.toEntity(experimentStatus);
        entity.setExperimentId(experimentId);
        ExperimentStatusEntity saved = experimentStatusRepository.save(entity);
        return saved.getStatusId();
    }
}
