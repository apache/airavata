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

import org.apache.airavata.registry.core.entities.expcatalog.ProcessStatusEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ProcessStatusPK;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

public class ProcessStatusRepository extends ExpCatAbstractRepository<ProcessStatus, ProcessStatusEntity, ProcessStatusPK> {
    private final static Logger logger = LoggerFactory.getLogger(ProcessStatusRepository.class);

    public ProcessStatusRepository() { super(ProcessStatus.class, ProcessStatusEntity.class); }

    protected String saveProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        if (processStatus.getStatusId() == null) {

            ProcessStatus currentProcessStatus = getProcessStatus(processId);
            if (currentProcessStatus == null || currentProcessStatus.getState() != currentProcessStatus.getState()) {
                processStatus.setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"));
            } else {
                // Update the existing current status if processStatus has no status id and the same state
                processStatus.setStatusId(currentProcessStatus.getStatusId());
            }
        }

        Mapper mapper = ObjectMapperSingleton.getInstance();
        ProcessStatusEntity processStatusEntity = mapper.map(processStatus, ProcessStatusEntity.class);

        if (processStatusEntity.getProcessId() == null) {
            logger.debug("Setting the ProcessStatusEntity's ProcessId");
            processStatusEntity.setProcessId(processId);
        }

        execute(entityManager -> entityManager.merge(processStatusEntity));
        return processStatusEntity.getStatusId();
    }

    public String addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {

        if (processStatus.getStatusId() == null) {
            logger.debug("Setting the ProcessStatus's StatusId");
            processStatus.setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"));
        }

        return saveProcessStatus(processStatus, processId);
    }

    public String updateProcessStatus(ProcessStatus updatedProcessStatus, String processId) throws RegistryException {
        return saveProcessStatus(updatedProcessStatus, processId);
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryException {
        ProcessRepository processRepository = new ProcessRepository();
        ProcessModel processModel = processRepository.getProcess(processId);
        List<ProcessStatus> processStatusList = processModel.getProcessStatuses();

        if(processStatusList.size() == 0) {
            logger.debug("ProcessStatus list is empty");
            return null;
        }

        else {
            ProcessStatus latestProcessStatus = processStatusList.get(0);

            for(int i = 1; i < processStatusList.size(); i++){
                Timestamp timeOfStateChange = new Timestamp(processStatusList.get(i).getTimeOfStateChange());

                if (timeOfStateChange != null) {

                    if (timeOfStateChange.after(new Timestamp(latestProcessStatus.getTimeOfStateChange()))
                            || (timeOfStateChange.equals(latestProcessStatus.getTimeOfStateChange()) && processStatusList.get(i).getState().equals(ProcessState.COMPLETED.toString()))
                            || (timeOfStateChange.equals(latestProcessStatus.getTimeOfStateChange()) && processStatusList.get(i).getState().equals(ProcessState.FAILED.toString()))
                            || (timeOfStateChange.equals(latestProcessStatus.getTimeOfStateChange()) && processStatusList.get(i).getState().equals(ProcessState.CANCELED.toString()))) {
                        latestProcessStatus = processStatusList.get(i);
                    }

                }

            }
            return latestProcessStatus;
        }
    }

}
