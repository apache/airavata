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
package org.apache.airavata.compute.repository;

import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.mapper.ComputeMapper;
import org.apache.airavata.compute.model.*;
import org.apache.airavata.db.AbstractRepository;
import org.apache.airavata.db.DBConstants;
import org.apache.airavata.db.QueryConstants;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager;
import org.apache.airavata.model.parallelism.proto.ApplicationParallelismType;
import org.springframework.stereotype.Component;

@Component
public class ResourceJobManagerRepository
        extends AbstractRepository<ResourceJobManager, ResourceJobManagerEntity, String> {

    public ResourceJobManagerRepository() {
        super(ResourceJobManager.class, ResourceJobManagerEntity.class);
    }

    @Override
    protected ResourceJobManager toModel(ResourceJobManagerEntity entity) {
        return ComputeMapper.INSTANCE.resourceJobManagerToModel(entity);
    }

    @Override
    protected ResourceJobManagerEntity toEntity(ResourceJobManager model) {
        return ComputeMapper.INSTANCE.resourceJobManagerToEntity(model);
    }

    public void createJobManagerCommand(
            Map<Integer, String> jobManagerCommands, ResourceJobManagerEntity resourceJobManagerEntity) {
        for (Integer commandTypeValue : jobManagerCommands.keySet()) {
            JobManagerCommand commandType = JobManagerCommand.forNumber(commandTypeValue);
            if (commandType != null
                    && jobManagerCommands.get(commandTypeValue) != null
                    && !jobManagerCommands.get(commandTypeValue).isEmpty()) {
                JobManagerCommandEntity jobManagerCommandEntity = new JobManagerCommandEntity();
                jobManagerCommandEntity.setCommand(jobManagerCommands.get(commandTypeValue));
                jobManagerCommandEntity.setResourceJobManager(resourceJobManagerEntity);
                jobManagerCommandEntity.setResourceJobManagerId(resourceJobManagerEntity.getResourceJobManagerId());
                jobManagerCommandEntity.setCommandType(commandType);
                execute(entityManager -> entityManager.merge(jobManagerCommandEntity));
            }
        }
    }

    public void createParallesimPrefix(
            Map<Integer, String> parallelismPrefix, ResourceJobManagerEntity resourceJobManagerEntity) {
        for (Integer commandTypeValue : parallelismPrefix.keySet()) {
            ApplicationParallelismType commandType = ApplicationParallelismType.forNumber(commandTypeValue);
            if (commandType != null
                    && parallelismPrefix.get(commandTypeValue) != null
                    && !parallelismPrefix.get(commandTypeValue).isEmpty()) {
                ParallelismCommandEntity parallelismCommandEntity = new ParallelismCommandEntity();
                parallelismCommandEntity.setCommand(parallelismPrefix.get(commandTypeValue));
                parallelismCommandEntity.setResourceJobManager(resourceJobManagerEntity);
                parallelismCommandEntity.setCommandType(commandType);
                parallelismCommandEntity.setResourceJobManagerId(resourceJobManagerEntity.getResourceJobManagerId());
                execute(entityManager -> entityManager.merge(parallelismCommandEntity));
            }
        }
    }

    public Map<JobManagerCommand, String> getJobManagerCommand(String resourceJobManagerId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ResourceJobManager.RESOURCE_JOB_MANAGER_ID, resourceJobManagerId);

        List resultSet = (List) execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(QueryConstants.GET_JOB_MANAGER_COMMAND);
            for (Map.Entry<String, Object> entry : queryParameters.entrySet()) {
                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }
            return jpaQuery.setFirstResult(0).getResultList();
        });

        List<JobManagerCommandEntity> jobManagerCommandEntityList = resultSet;
        Map<JobManagerCommand, String> jobManagerCommandMap = new HashMap<JobManagerCommand, String>();
        for (JobManagerCommandEntity jm : jobManagerCommandEntityList) {
            jobManagerCommandMap.put(jm.getCommandType(), jm.getCommand());
        }
        return jobManagerCommandMap;
    }

    public Map<ApplicationParallelismType, String> getParallelismPrefix(String resourceJobManagerId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ResourceJobManager.RESOURCE_JOB_MANAGER_ID, resourceJobManagerId);

        List resultSet = (List) execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(QueryConstants.GET_PARALLELISM_PREFIX);
            for (Map.Entry<String, Object> entry : queryParameters.entrySet()) {
                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }
            return jpaQuery.setFirstResult(0).getResultList();
        });

        List<ParallelismCommandEntity> parallelismCommandEntityList = resultSet;
        Map<ApplicationParallelismType, String> applicationParallelismTypeMap =
                new HashMap<ApplicationParallelismType, String>();
        for (ParallelismCommandEntity pc : parallelismCommandEntityList) {
            applicationParallelismTypeMap.put(pc.getCommandType(), pc.getCommand());
        }
        return applicationParallelismTypeMap;
    }
}
