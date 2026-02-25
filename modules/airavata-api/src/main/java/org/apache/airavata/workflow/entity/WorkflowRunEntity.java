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
package org.apache.airavata.workflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.workflow.model.WorkflowRunStepState;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * WorkflowRun entity representing a single execution instance of a {@link WorkflowEntity}.
 *
 * <p>STEP_STATES is persisted as a JSON string (MEDIUMTEXT) mapping step IDs to their
 * runtime state. {@code columnDefinition = "MEDIUMTEXT"} is used instead of {@code @Lob}
 * because MariaDB maps {@code @Lob} on a String to TINYTEXT (255 bytes), which is
 * insufficient for workflows with many steps.
 */
@Entity(name = "WorkflowRunEntity")
@Table(name = "workflow_run")
@EntityListeners(AuditingEntityListener.class)
public class WorkflowRunEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "run_id", nullable = false)
    private String runId;

    @Column(name = "workflow_id", nullable = false)
    private String workflowId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "CREATED";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "step_states", columnDefinition = "json")
    private Map<String, WorkflowRunStepState> stepStates = new HashMap<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public WorkflowRunEntity() {}

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, WorkflowRunStepState> getStepStates() {
        return stepStates;
    }

    public void setStepStates(Map<String, WorkflowRunStepState> stepStates) {
        this.stepStates = stepStates;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
