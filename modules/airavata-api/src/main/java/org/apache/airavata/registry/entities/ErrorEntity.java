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
import org.apache.airavata.common.model.ErrorParentType;
import org.apache.airavata.common.utils.AiravataUtils;

/**
 * Unified ErrorEntity that consolidates error records from experiments, processes,
 * tasks, workflows, applications, and handlers.
 *
 * <p>This entity replaces the following separate entities:
 * <ul>
 *   <li>{@code ExperimentErrorEntity}</li>
 *   <li>{@code ProcessErrorEntity}</li>
 *   <li>{@code TaskErrorEntity}</li>
 *   <li>{@code AiravataWorkflowErrorEntity}</li>
 *   <li>{@code ApplicationErrorEntity}</li>
 *   <li>{@code HandlerErrorEntity}</li>
 * </ul>
 *
 * <p>The {@code parentType} field discriminates between different error sources,
 * while {@code parentId} stores the ID of the parent entity (experiment ID, process ID, etc.).
 */
@Entity(name = "ErrorEntity")
@Table(
        name = "ERROR",
        indexes = {
            @Index(name = "idx_error_parent", columnList = "PARENT_ID, PARENT_TYPE"),
            @Index(name = "idx_error_creation_time", columnList = "CREATION_TIME")
        })
@IdClass(ErrorEntityPK.class)
public class ErrorEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ERROR_ID", nullable = false)
    private String errorId;

    @Id
    @Column(name = "PARENT_ID", nullable = false)
    private String parentId;

    @Id
    @Column(name = "PARENT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ErrorParentType parentType;

    @Column(name = "CREATION_TIME", nullable = false, columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Timestamp creationTime;

    @Lob
    @Column(name = "ACTUAL_ERROR_MESSAGE")
    private String actualErrorMessage;

    @Lob
    @Column(name = "USER_FRIENDLY_MESSAGE")
    private String userFriendlyMessage;

    @Column(name = "TRANSIENT_OR_PERSISTENT")
    private boolean transientOrPersistent;

    @Lob
    @Column(name = "ROOT_CAUSE_ERROR_ID_LIST")
    private String rootCauseErrorIdList;

    public ErrorEntity() {}

    /**
     * Creates an error entity for a specific parent type.
     *
     * @param errorId the unique error identifier
     * @param parentId the parent entity ID (experiment ID, process ID, etc.)
     * @param parentType the type of parent entity
     */
    public ErrorEntity(String errorId, String parentId, ErrorParentType parentType) {
        this.errorId = errorId;
        this.parentId = parentId;
        this.parentType = parentType;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public ErrorParentType getParentType() {
        return parentType;
    }

    public void setParentType(ErrorParentType parentType) {
        this.parentType = parentType;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getActualErrorMessage() {
        return actualErrorMessage;
    }

    public void setActualErrorMessage(String actualErrorMessage) {
        this.actualErrorMessage = actualErrorMessage;
    }

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public void setUserFriendlyMessage(String userFriendlyMessage) {
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public boolean isTransientOrPersistent() {
        return transientOrPersistent;
    }

    public void setTransientOrPersistent(boolean transientOrPersistent) {
        this.transientOrPersistent = transientOrPersistent;
    }

    public String getRootCauseErrorIdList() {
        return rootCauseErrorIdList;
    }

    public void setRootCauseErrorIdList(String rootCauseErrorIdList) {
        this.rootCauseErrorIdList = rootCauseErrorIdList;
    }

    @PrePersist
    void setCreationTime() {
        if (this.creationTime == null) {
            this.creationTime = AiravataUtils.getUniqueTimestamp();
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
        ErrorEntity that = (ErrorEntity) obj;
        return Objects.equals(errorId, that.errorId)
                && Objects.equals(parentId, that.parentId)
                && parentType == that.parentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorId, parentId, parentType);
    }

    @Override
    public String toString() {
        return "ErrorEntity{"
                + "errorId='"
                + errorId
                + '\''
                + ", parentId='"
                + parentId
                + '\''
                + ", parentType="
                + parentType
                + ", creationTime="
                + creationTime
                + ", actualErrorMessage='"
                + (actualErrorMessage != null ? actualErrorMessage.substring(0, Math.min(50, actualErrorMessage.length())) + "..." : "null")
                + '\''
                + ", transientOrPersistent="
                + transientOrPersistent
                + '}';
    }
}
