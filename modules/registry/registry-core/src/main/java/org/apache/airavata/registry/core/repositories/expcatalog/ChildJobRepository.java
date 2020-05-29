/**
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
 */
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.job.ChildJobModel;
import org.apache.airavata.registry.core.entities.expcatalog.ChildJobEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildJobRepository extends ExpCatAbstractRepository<ChildJobModel, ChildJobEntity, String>  {

    private final static Logger logger = LoggerFactory.getLogger(ChildJobRepository.class);

    public ChildJobRepository() { super(ChildJobModel.class, ChildJobEntity.class); }

    protected String saveChildJob(ChildJobModel jobModel) throws RegistryException {

        if (jobModel.getJobStatuses() != null) {
            logger.debug("Populating the status ids of JobStatus objects for the Job");
            jobModel.getJobStatuses().forEach(jobStatus -> {
                if (jobStatus.getStatusId() == null) {
                    jobStatus.setStatusId(ExpCatalogUtils.getID("CHILD_JOB_STATE"));
                }
            });
        }

        Mapper mapper = ObjectMapperSingleton.getInstance();
        ChildJobEntity jobEntity = mapper.map(jobModel, ChildJobEntity.class);
        populateParentIds(jobEntity);
        ChildJobEntity savedJobEntity = execute(entityManager -> entityManager.merge(jobEntity));
        return savedJobEntity.getChildJobId();
    }

    public String addChildJob(ChildJobModel jobModel) throws RegistryException {
        return saveChildJob(jobModel);
    }

    public String updateChildJob(ChildJobModel jobModel) throws RegistryException {
        return saveChildJob(jobModel);
    }

    public List<ChildJobModel> getChildJobsForParent(String parentJobId, String parentTaskId) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.ChildJob.PARENT_JOB_ID, parentJobId);
        queryParameters.put(DBConstants.ChildJob.PARENT_TASK_ID, parentTaskId);
        List<ChildJobModel> childJobModels = select(QueryConstants.GET_CHILD_JOB_FOR_PARENT, -1, 0, queryParameters);
        return childJobModels;
    }

    protected void populateParentIds(ChildJobEntity jobEntity) {

        String jobId = jobEntity.getChildJobId();
        if (jobEntity.getJobStatuses() != null) {
            logger.debug("Populating the Primary Key of Child JobS tatus objects for the Job {}", jobId);
            jobEntity.getJobStatuses().forEach(jobStatusEntity -> {
                jobStatusEntity.setChildJobId(jobId);
            });
        }
    }
}
