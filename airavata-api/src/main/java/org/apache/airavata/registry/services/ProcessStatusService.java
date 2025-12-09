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
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.registry.entities.expcatalog.ProcessStatusEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.ProcessStatusRepository;
import org.apache.airavata.registry.utils.ExpCatalogUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProcessStatusService {
    private final ProcessStatusRepository processStatusRepository;
    private final Mapper mapper;

    public ProcessStatusService(ProcessStatusRepository processStatusRepository, Mapper mapper) {
        this.processStatusRepository = processStatusRepository;
        this.mapper = mapper;
    }

    public ProcessStatus getProcessStatus(String processId) throws RegistryException {
        List<ProcessStatusEntity> entities =
                processStatusRepository.findByProcessIdOrderByTimeOfStateChangeDesc(processId);
        if (entities.isEmpty()) return null;
        return mapper.map(entities.get(0), ProcessStatus.class);
    }

    public List<ProcessStatus> getProcessStatusList(String processId) throws RegistryException {
        List<ProcessStatusEntity> entities =
                processStatusRepository.findByProcessIdOrderByTimeOfStateChangeDesc(processId);
        List<ProcessStatus> result = new ArrayList<>();
        entities.forEach(e -> result.add(mapper.map(e, ProcessStatus.class)));
        return result;
    }

    public List<ProcessStatus> getProcessStatusList(ProcessState processState, int offset, int limit)
            throws RegistryException {
        Pageable pageable = PageRequest.of(Math.max(0, offset / Math.max(1, limit)), limit);
        List<ProcessStatusEntity> entities = processStatusRepository.findByState(processState, pageable);
        List<ProcessStatus> result = new ArrayList<>();
        entities.forEach(e -> result.add(mapper.map(e, ProcessStatus.class)));
        return result;
    }

    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        if (processStatus.getStatusId() == null) {
            processStatus.setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"));
        }
        processStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        ProcessStatusEntity entity = mapper.map(processStatus, ProcessStatusEntity.class);
        entity.setProcessId(processId);
        processStatusRepository.save(entity);
    }

    public void updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryException {
        if (processStatus.getStatusId() == null) {
            processStatus.setStatusId(ExpCatalogUtils.getID("PROCESS_STATE"));
        }
        if (processStatus.getTimeOfStateChange() == 0) {
            processStatus.setTimeOfStateChange(
                    AiravataUtils.getCurrentTimestamp().getTime());
        }
        ProcessStatusEntity entity = mapper.map(processStatus, ProcessStatusEntity.class);
        entity.setProcessId(processId);
        processStatusRepository.save(entity);
    }
}
