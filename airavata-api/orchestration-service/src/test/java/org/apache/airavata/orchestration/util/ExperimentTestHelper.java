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
package org.apache.airavata.orchestration.util;

import java.util.List;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.util.AiravataUtils;

/**
 * Lightweight orchestration-test helper that creates/reads/removes EXPERIMENT (and its
 * EXPERIMENT_STATUS) rows via native SQL, without depending on research-service's JPA
 * entities or repositories. Provides just enough to satisfy the PROCESS → EXPERIMENT
 * foreign key for process/task/job repository tests.
 */
public class ExperimentTestHelper extends AbstractRepository<ExperimentModel, ExperimentModel, String> {

    public ExperimentTestHelper() {
        super(ExperimentModel.class, ExperimentModel.class);
    }

    @Override
    protected ExperimentModel toModel(ExperimentModel entity) {
        return entity;
    }

    @Override
    protected ExperimentModel toEntity(ExperimentModel model) {
        return model;
    }

    public String addExperiment(ExperimentModel experimentModel) throws RegistryException {
        String expName = experimentModel.getExperimentName();
        String experimentId = AiravataUtils.getId(expName.substring(0, Math.min(expName.length(), 50)));

        executeWithNativeQuery(
                "INSERT INTO EXPERIMENT (EXPERIMENT_ID, PROJECT_ID, GATEWAY_ID, EXPERIMENT_TYPE, USER_NAME, "
                        + "EXPERIMENT_NAME, DESCRIPTION) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)",
                experimentId,
                experimentModel.getProjectId(),
                experimentModel.getGatewayId(),
                experimentModel.getExperimentType().name(),
                experimentModel.getUserName(),
                experimentModel.getExperimentName(),
                experimentModel.getDescription());

        executeWithNativeQuery(
                "INSERT INTO EXPERIMENT_STATUS (STATUS_ID, EXPERIMENT_ID, STATE) VALUES (?1, ?2, ?3)",
                AiravataUtils.getId("EXPERIMENT_STATE"),
                experimentId,
                ExperimentState.EXPERIMENT_STATE_CREATED.name());

        return experimentId;
    }

    @SuppressWarnings("unchecked")
    public ExperimentModel getExperiment(String experimentId) throws RegistryException {
        List<Object> rows = selectWithNativeQuery(
                "SELECT PROJECT_ID, GATEWAY_ID, USER_NAME, EXPERIMENT_NAME, DESCRIPTION "
                        + "FROM EXPERIMENT WHERE EXPERIMENT_ID = ?1",
                experimentId);
        if (rows.isEmpty()) {
            return null;
        }
        Object[] row = (Object[]) rows.get(0);
        ExperimentModel.Builder builder = ExperimentModel.newBuilder()
                .setExperimentId(experimentId)
                .setProjectId(asString(row[0]))
                .setGatewayId(asString(row[1]))
                .setUserName(asString(row[2]))
                .setExperimentName(asString(row[3]))
                .setDescription(asString(row[4]));

        List<Object> processIds = selectWithNativeQuery(
                "SELECT PROCESS_ID FROM PROCESS WHERE EXPERIMENT_ID = ?1", experimentId);
        for (Object pid : processIds) {
            builder.addProcesses(ProcessModel.newBuilder()
                    .setProcessId(asString(pid))
                    .setExperimentId(experimentId)
                    .build());
        }
        return builder.build();
    }

    public void removeExperiment(String experimentId) throws RegistryException {
        executeWithNativeQuery("DELETE FROM EXPERIMENT_STATUS WHERE EXPERIMENT_ID = ?1", experimentId);
        executeWithNativeQuery("DELETE FROM EXPERIMENT WHERE EXPERIMENT_ID = ?1", experimentId);
    }

    private static String asString(Object value) {
        return value == null ? "" : value.toString();
    }
}
