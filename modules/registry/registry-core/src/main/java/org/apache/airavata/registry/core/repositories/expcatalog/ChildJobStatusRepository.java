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

import org.apache.airavata.model.status.ChildJobStatus;
import org.apache.airavata.registry.core.entities.expcatalog.ChildJobStatusEntity;
import org.apache.airavata.registry.core.entities.expcatalog.ChildJobStatusPK;
import org.apache.airavata.registry.core.utils.ExpCatalogUtils;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChildJobStatusRepository extends ExpCatAbstractRepository<ChildJobStatus, ChildJobStatusEntity, ChildJobStatusPK> {

    private final static Logger logger = LoggerFactory.getLogger(ChildJobStatusRepository.class);

    public ChildJobStatusRepository() { super(ChildJobStatus.class, ChildJobStatusEntity.class); }

    public String addChildJobStatus(ChildJobStatus jobStatus, String childJobId) {
        jobStatus.setStatusId(ExpCatalogUtils.getID("CHILD_JOB_STATE"));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        ChildJobStatusEntity jobStatusEntity = mapper.map(jobStatus, ChildJobStatusEntity.class);
        jobStatusEntity.setChildJobId(childJobId);
        execute(entityManager -> entityManager.merge(jobStatusEntity));
        return jobStatusEntity.getStatusId();
    }
}
