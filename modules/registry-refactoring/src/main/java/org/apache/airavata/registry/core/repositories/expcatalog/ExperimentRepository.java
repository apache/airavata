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

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentEntity;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.apache.airavata.registry.core.utils.JPAUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExperimentRepository extends AbstractRepository<ExperimentModel, ExperimentEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentRepository.class);

    public ExperimentRepository(Class<ExperimentModel> thriftGenericClass, Class<ExperimentEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }

    @Override
    public ExperimentModel create(ExperimentModel experiment){
        return update(experiment);
    }

    @Override
    public ExperimentModel update(ExperimentModel experiment){
        String experimentId = experiment.getExperimentId();
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ExperimentEntity entity = mapper.map(experiment, ExperimentEntity.class);

        if(entity.getUserConfigurationData() != null) {
            entity.getUserConfigurationData().setExperimentId(experimentId);
            if (entity.getUserConfigurationData().getComputeResourceSchedulingEntity() != null)
                entity.getUserConfigurationData().getComputeResourceSchedulingEntity().setExperimentId(experimentId);
        }
        if(entity.getExperimentInputs() != null)
            entity.getExperimentInputs().forEach(expIn->expIn.setExperimentId(experimentId));
        if(entity.getExperimentOutputs() != null)
            entity.getExperimentOutputs().forEach(expOut->expOut.setExperimentId(experimentId));
        if(entity.getExperimentErrors() != null)
            entity.getExperimentErrors().forEach(expErr->expErr.setExperimentId(experimentId));
        if(entity.getExperimentStatuses() != null)
            entity.getExperimentStatuses().forEach(expStatus->expStatus.setExperimentId(experimentId));

        if(entity.getProcesses() != null){
            entity.getProcesses().forEach(process->{
                process.setExperimentId(experimentId);
                String processId = process.getProcessId();
                if(process.getProcessResourceSchedule() != null)
                    process.getProcessResourceSchedule().setProcessId(processId);
                if(process.getProcessInputs() != null)
                    process.getProcessInputs().forEach(proInput->proInput.setProceseId(processId));
                if(process.getProcessOutputs() != null)
                    process.getProcessOutputs().forEach(proOutput->proOutput.setProcessId(processId));
                if(process.getProcessErrors() != null)
                    process.getProcessErrors().forEach(processErr->processErr.setProcessId(processId));
                if(process.getProcessStatuses() != null)
                    process.getProcessStatuses().forEach(processStat->processStat.setProcessId(processId));

                if(process.getTasks() != null){
                    process.getTasks().forEach(task->{
                        String taskId = task.getTaskId();
                        task.setParentProcessId(processId);


                    });
                }
            });
        }

        ExperimentEntity persistedCopy = JPAUtils.execute(entityManager -> entityManager.merge(entity));
        return mapper.map(persistedCopy, ExperimentModel.class);
    }

    @Override
    public List<ExperimentModel> select(String criteria, int offset, int limit){
        throw new UnsupportedOperationException("Due to performance overheads this method is not supported. Instead use" +
                " ExperimentSummaryRepository");
    }
}