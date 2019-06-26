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
package org.apache.airavata.registry.core.entities.airavataworkflowcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "HANDLER_ERROR")
@IdClass(HandlerErrorPK.class)
public class HandlerErrorEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ERROR_ID")
    private String errorId;

    @Id
    @Column(name = "HANDLER_ID")
    private String handlerId;

    @Column(name = "CREATION_TIME")
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

    @ManyToOne(targetEntity = WorkflowHandlerEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "HANDLER_ID", referencedColumnName = "ID", nullable = false, updatable = false),
            @JoinColumn(name = "WORKFLOW_ID", referencedColumnName = "WORKFLOW_ID", nullable = false, updatable = false)
    })
    private WorkflowHandlerEntity handler;

    public HandlerErrorEntity() {
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public void setHandlerId(String handlerId) {
        this.handlerId = handlerId;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public void setActualErrorMessage(String actualErrorMessage) {
        this.actualErrorMessage = actualErrorMessage;
    }

    public void setUserFriendlyMessage(String userFriendlyMessage) {
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public void setTransientOrPersistent(boolean transientOrPersistent) {
        this.transientOrPersistent = transientOrPersistent;
    }

    public void setRootCauseErrorIdList(String rootCauseErrorIdList) {
        this.rootCauseErrorIdList = rootCauseErrorIdList;
    }

    public void setHandler(WorkflowHandlerEntity handler) {
        this.handler = handler;
    }

    public String getErrorId() {
        return errorId;
    }

    public String getHandlerId() {
        return handlerId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public String getActualErrorMessage() {
        return actualErrorMessage;
    }

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public boolean isTransientOrPersistent() {
        return transientOrPersistent;
    }

    public String getRootCauseErrorIdList() {
        return rootCauseErrorIdList;
    }

    public WorkflowHandlerEntity getHandler() {
        return handler;
    }
}
