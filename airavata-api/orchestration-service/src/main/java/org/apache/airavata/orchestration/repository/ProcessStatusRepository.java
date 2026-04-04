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
package org.apache.airavata.orchestration.repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.orchestration.mapper.ExecutionMapper;
import org.apache.airavata.orchestration.model.ProcessStatusEntity;
import org.apache.airavata.orchestration.model.ProcessStatusPK;
import org.apache.airavata.util.ExpCatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcessStatusRepository extends AbstractRepository<ProcessStatus, ProcessStatusEntity, ProcessStatusPK> {
    private static final Logger logger = LoggerFactory.getLogger(ProcessStatusRepository.class);

    public ProcessStatusRepository() {
        super(ProcessStatus.class, ProcessStatusEntity.class);
    }

    @Override
    protected ProcessStatus toModel(ProcessStatusEntity entity) {
        return ExecutionMapper.INSTANCE.processStatusToModel(entity);
    }

    @Override
    protected ProcessStatusEntity toEntity(ProcessStatus model) {
        return ExecutionMapper.INSTANCE.processStatusToEntity(model);
    }

    protected String saveProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        if (processStatus.getStatusId().isEmpty()) {

            ProcessStatus currentProcessStatus = getProcessStatus(processId);
            if (currentProcessStatus == null || currentProcessStatus.getState() != processStatus.getState()) {
                processStatus = processStatus.toBuilder()
                        .setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"))
                        .build();
            } else {
                // Update the existing current status if processStatus has no status id and the same state
                processStatus = processStatus.toBuilder()
                        .setStatusId(currentProcessStatus.getStatusId())
                        .build();
            }
        }
        ProcessStatusEntity processStatusEntity = ExecutionMapper.INSTANCE.processStatusToEntity(processStatus);

        if (processStatusEntity.getProcessId() == null) {
            logger.debug("Setting the ProcessStatusEntity's ProcessId");
            processStatusEntity.setProcessId(processId);
        }

        execute(entityManager -> entityManager.merge(processStatusEntity));
        return processStatusEntity.getStatusId();
    }

    public String addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {

        if (processStatus.getStatusId().isEmpty()) {
            logger.debug("Setting the ProcessStatus's StatusId");
            processStatus = processStatus.toBuilder()
                    .setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"))
                    .build();
        }

        return saveProcessStatus(processStatus, processId);
    }

    public String updateProcessStatus(ProcessStatus updatedProcessStatus, String processId) throws RegistryException {
        return saveProcessStatus(updatedProcessStatus, processId);
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        ProcessModel processModel = processRepository.getProcess(processId);
        List<ProcessStatus> processStatusList = processModel.getProcessStatusesList();

        if (processStatusList.size() == 0) {
            logger.debug("ProcessStatus list is empty");
            return null;
        } else {
            ProcessStatus latestProcessStatus = processStatusList.get(0);

            for (int i = 1; i < processStatusList.size(); i++) {
                Timestamp timeOfStateChange =
                        new Timestamp(processStatusList.get(i).getTimeOfStateChange());

                if (timeOfStateChange != null) {

                    if (timeOfStateChange.after(new Timestamp(latestProcessStatus.getTimeOfStateChange()))
                            || (timeOfStateChange.equals(latestProcessStatus.getTimeOfStateChange())
                                    && processStatusList.get(i).getState() == ProcessState.PROCESS_STATE_COMPLETED)
                            || (timeOfStateChange.equals(latestProcessStatus.getTimeOfStateChange())
                                    && processStatusList.get(i).getState() == ProcessState.PROCESS_STATE_FAILED)
                            || (timeOfStateChange.equals(latestProcessStatus.getTimeOfStateChange())
                                    && processStatusList.get(i).getState() == ProcessState.PROCESS_STATE_CANCELED)) {
                        latestProcessStatus = processStatusList.get(i);
                    }
                }
            }
            return latestProcessStatus;
        }
    }

    public List<ProcessStatus> getProcessStatusList(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        ProcessModel processModel = processRepository.getProcess(processId);
        return processModel.getProcessStatusesList();
    }

    public List<ProcessStatus> getProcessStatusList(ProcessState processState, int offset, int limit)
            throws RegistryException {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(DBConstants.ProcessStatus.STATE, processState);
        ProcessStatusRepository processStatusRepository = new ProcessStatusRepository();
        return processStatusRepository.select(QueryConstants.FIND_PROCESS_WITH_STATUS, limit, offset, queryMap);
    }
}
