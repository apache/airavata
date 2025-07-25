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
package org.apache.airavata.registry.core.entities.expcatalog;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The persistent class for the process_error database table.
 */
@Entity
@Table(name = "PROCESS_ERROR")
@IdClass(ProcessErrorPK.class)
public class ProcessErrorEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ERROR_ID")
    private String errorId;

    @Id
    @Column(name = "PROCESS_ID")
    private String processId;

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

    @ManyToOne(targetEntity = ProcessEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", referencedColumnName = "PROCESS_ID")
    private ProcessEntity process;

    public ProcessErrorEntity() {}

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
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

    public ProcessEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessEntity process) {
        this.process = process;
    }
}
