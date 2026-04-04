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
package org.apache.airavata.research.util;

import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.research.model.ExperimentEntity;
import org.apache.airavata.research.model.ExperimentStatusEntity;
import org.apache.airavata.util.AiravataUtils;

/**
 * Lightweight test helper that creates/removes experiments without depending on research-service's ExperimentRepository.
 * Used by orchestration-service tests that need experiment fixtures for process/task/job testing.
 */
public class ExperimentTestHelper extends AbstractRepository<ExperimentModel, ExperimentEntity, String> {

    public ExperimentTestHelper() {
        super(ExperimentModel.class, ExperimentEntity.class);
    }

    @Override
    protected ExperimentModel toModel(ExperimentEntity entity) {
        if (entity == null) return null;
        ExperimentModel.Builder builder = ExperimentModel.newBuilder()
                .setExperimentId(entity.getExperimentId() != null ? entity.getExperimentId() : "")
                .setProjectId(entity.getProjectId() != null ? entity.getProjectId() : "")
                .setGatewayId(entity.getGatewayId() != null ? entity.getGatewayId() : "")
                .setUserName(entity.getUserName() != null ? entity.getUserName() : "")
                .setExperimentName(entity.getExperimentName() != null ? entity.getExperimentName() : "")
                .setDescription(entity.getDescription() != null ? entity.getDescription() : "");
        if (entity.getExperimentType() != null) {
            builder.setExperimentType(entity.getExperimentType());
        }
        return builder.build();
    }

    @Override
    protected ExperimentEntity toEntity(ExperimentModel model) {
        ExperimentEntity entity = new ExperimentEntity();
        entity.setExperimentId(model.getExperimentId());
        entity.setProjectId(model.getProjectId());
        entity.setGatewayId(model.getGatewayId());
        entity.setUserName(model.getUserName());
        entity.setExperimentName(model.getExperimentName());
        entity.setDescription(model.getDescription());
        entity.setExperimentType(model.getExperimentType());
        return entity;
    }

    public String addExperiment(ExperimentModel experimentModel) throws RegistryException {
        ExperimentStatus experimentStatus = ExperimentStatus.newBuilder()
                .setState(ExperimentState.EXPERIMENT_STATE_CREATED)
                .setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime())
                .build();
        String expName = experimentModel.getExperimentName();
        experimentModel = experimentModel.toBuilder()
                .addExperimentStatus(experimentStatus)
                .setExperimentId(AiravataUtils.getId(expName.substring(0, Math.min(expName.length(), 50))))
                .build();

        ExperimentEntity entity = toEntity(experimentModel);
        String experimentId = entity.getExperimentId();

        // Populate child status entities
        if (!experimentModel.getExperimentStatusList().isEmpty()) {
            java.util.List<ExperimentStatusEntity> statusEntities = new java.util.ArrayList<>();
            for (ExperimentStatus s : experimentModel.getExperimentStatusList()) {
                ExperimentStatusEntity se = new ExperimentStatusEntity();
                se.setExperimentId(experimentId);
                se.setState(s.getState());
                se.setTimeOfStateChange(new java.sql.Timestamp(s.getTimeOfStateChange()));
                statusEntities.add(se);
            }
            entity.setExperimentStatus(statusEntities);
        }

        ExperimentEntity merged = execute(entityManager -> entityManager.merge(entity));
        return merged.getExperimentId();
    }

    public ExperimentModel getExperiment(String experimentId) throws RegistryException {
        return get(experimentId);
    }

    public void removeExperiment(String experimentId) throws RegistryException {
        delete(experimentId);
    }
}
