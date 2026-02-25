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
package org.apache.airavata.workflow.repository;

import java.util.List;
import org.apache.airavata.workflow.entity.WorkflowRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRunRepository extends JpaRepository<WorkflowRunEntity, String> {

    /**
     * Returns all runs for the given workflow, newest first.
     *
     * @param workflowId  the workflow identifier
     * @return run entities ordered by {@code CREATED_AT} descending, or an empty list
     */
    List<WorkflowRunEntity> findByWorkflowIdOrderByCreatedAtDesc(String workflowId);

    /**
     * Returns all runs initiated by the given user, newest first.
     *
     * @param userName  the owning user
     * @return run entities ordered by {@code CREATED_AT} descending, or an empty list
     */
    List<WorkflowRunEntity> findByUserNameOrderByCreatedAtDesc(String userName);
}
