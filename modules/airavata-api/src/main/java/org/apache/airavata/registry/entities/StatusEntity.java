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
package org.apache.airavata.registry.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import org.apache.airavata.common.model.StatusParentType;
import org.apache.airavata.common.utils.AiravataUtils;

/**
 * Unified StatusEntity that consolidates status records from experiments, processes,
 * tasks, jobs, queues, workflows, applications, and handlers.
 *
 * <p>This entity replaces the following separate entities:
 * <ul>
 *   <li>{@code ExperimentStatusEntity}</li>
 *   <li>{@code ProcessStatusEntity}</li>
 *   <li>{@code TaskStatusEntity}</li>
 *   <li>{@code JobStatusEntity}</li>
 *   <li>{@code QueueStatusEntity}</li>
 *   <li>{@code AiravataWorkflowStatusEntity}</li>
 *   <li>{@code ApplicationStatusEntity}</li>
 *   <li>{@code HandlerStatusEntity}</li>
 * </ul>
 *
 * <p>The {@code parentType} field discriminates between different status sources,
 * while {@code parentId} stores the ID of the parent entity (experiment ID, process ID, etc.).
 * The {@code state} field stores the state as a string, which allows for different state
 * enum types (ExperimentState, ProcessState, TaskState, etc.) to be stored in a single table.
 */
@Entity(name = "StatusEntity")
@Table(
        name = "STATUS",
        indexes = {
            @Index(name = "idx_status_parent", columnList = "PARENT_ID, PARENT_TYPE"),
            @Index(name = "idx_status_time", columnList = "TIME_OF_STATE_CHANGE"),
            @Index(name = "idx_status_state", columnList = "STATE"),
            @Index(name = "idx_status_sequence", columnList = "SEQUENCE_NUM")
        })
@IdClass(StatusEntityPK.class)
public class StatusEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STATUS_ID", nullable = false)
    private String statusId;

    @Id
    @Column(name = "PARENT_ID", nullable = false)
    private String parentId;

    @Id
    @Column(name = "PARENT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusParentType parentType;

    /**
     * The state value stored as a string.
     * This allows different state enums (ExperimentState, ProcessState, TaskState, etc.)
     * to be stored in a single column. The service layer is responsible for validating
     * and converting the state string to the appropriate enum type.
     */
    @Column(name = "STATE")
    private String state;

    @Column(
            name = "TIME_OF_STATE_CHANGE",
            nullable = false,
            columnDefinition = "TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)")
    private Timestamp timeOfStateChange;

    @Lob
    @Column(name = "REASON")
    private String reason;

    /**
     * Sequence number that guarantees deterministic ordering per parent.
     * Assigned by the service layer using DB-backed {@code SELECT MAX(SEQUENCE_NUM)+1 ... FOR UPDATE}
     * so that ordering is correct in multi-instance deployments. If null at persist time,
     * a fallback in-memory value is used (ordering then per-process only).
     */
    @Column(name = "SEQUENCE_NUM", nullable = false)
    private Long sequenceNum;

    public StatusEntity() {}

    /**
     * Creates a status entity for a specific parent type.
     * The timestamp and sequence number are set immediately to ensure correct chronological ordering.
     *
     * @param statusId the unique status identifier
     * @param parentId the parent entity ID (experiment ID, process ID, etc.)
     * @param parentType the type of parent entity
     * @param state the state value as a string
     */
    public StatusEntity(String statusId, String parentId, StatusParentType parentType, String state) {
        this.statusId = statusId;
        this.parentId = parentId;
        this.parentType = parentType;
        this.state = state;
        this.timeOfStateChange = AiravataUtils.getUniqueTimestamp();
        // sequenceNum must be set by the service layer via getNextSequenceNum() for DB-backed ordering
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public StatusParentType getParentType() {
        return parentType;
    }

    public void setParentType(StatusParentType parentType) {
        this.parentType = parentType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public void setTimeOfStateChange(Timestamp timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(Long sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    @PrePersist
    void setDefaults() {
        if (this.timeOfStateChange == null) {
            this.timeOfStateChange = AiravataUtils.getUniqueTimestamp();
        }
        // sequenceNum: if still null, use timestamp-based fallback for single-instance compatibility
        if (this.sequenceNum == null) {
            this.sequenceNum = System.currentTimeMillis();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StatusEntity that = (StatusEntity) obj;
        return Objects.equals(statusId, that.statusId)
                && Objects.equals(parentId, that.parentId)
                && parentType == that.parentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusId, parentId, parentType);
    }

    @Override
    public String toString() {
        return "StatusEntity{"
                + "statusId='"
                + statusId
                + '\''
                + ", parentId='"
                + parentId
                + '\''
                + ", parentType="
                + parentType
                + ", state='"
                + state
                + '\''
                + ", timeOfStateChange="
                + timeOfStateChange
                + ", reason='"
                + (reason != null ? reason.substring(0, Math.min(50, reason.length())) + "..." : "null")
                + '\''
                + '}';
    }
}
