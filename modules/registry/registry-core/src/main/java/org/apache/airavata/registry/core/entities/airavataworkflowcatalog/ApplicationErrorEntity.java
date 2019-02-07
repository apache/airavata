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
@Table(name = "APPLICATION_ERROR")
@IdClass(ApplicationErrorPK.class)
public class ApplicationErrorEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ERROR_ID")
    private String errorId;

    @Id
    @Column(name = "APPLICATION_ID")
    private String applicationId;

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

    @ManyToOne(targetEntity = WorkflowApplicationEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", referencedColumnName = "ID")
    private WorkflowApplicationEntity application;

    public ApplicationErrorEntity() {
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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

    public void setApplication(WorkflowApplicationEntity application) {
        this.application = application;
    }

    public String getErrorId() {
        return errorId;
    }

    public String getApplicationId() {
        return applicationId;
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

    public WorkflowApplicationEntity getApplication() {
        return application;
    }
}
