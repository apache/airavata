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
package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "EXPCAT_PROCESS_ERROR")
@IdClass(ProcessErrorPK.class)
public class ProcessErrorEntity {
    private String errorId;
    private String processId;
    private long creationTime;
    private String actualErrorMessage;
    private String userFriendlyMessage;
    private boolean transientOrPersistent;
    private List<String> rootCauseErrorIdList;

    private ProcessEntity process;

    @Id
    @Column(name = "ERROR_ID")
    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    @Id
    @Column(name = "PROCESS_ID")
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Column(name = "CREATION_TIME")
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "ACTUAL_ERROR_MESSAGE")
    public String getActualErrorMessage() {
        return actualErrorMessage;
    }

    public void setActualErrorMessage(String actualErrorMessage) {
        this.actualErrorMessage = actualErrorMessage;
    }

    @Column(name = "USER_FRIENDLY_MESSAGE")
    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public void setUserFriendlyMessage(String userFriendlyMessage) {
        this.userFriendlyMessage = userFriendlyMessage;
    }


    @Column(name = "TRANSIENT_OR_PERSISTENT")
    public boolean isTransientOrPersistent() {
        return transientOrPersistent;
    }

    public void setTransientOrPersistent(boolean transientOrPersistent) {
        this.transientOrPersistent = transientOrPersistent;
    }


    @ElementCollection
    @CollectionTable(name="EXPCAT_EXPERIMENT_ERROR_ROOT_CAUSE_ERROR_ID", joinColumns = @JoinColumn(name="ERROR_ID"))
    public List<String> getRootCauseErrorIdList() {
        return rootCauseErrorIdList;
    }

    public void setRootCauseErrorIdList(List<String> rootCauseErrorIdList) {
        this.rootCauseErrorIdList = rootCauseErrorIdList;
    }


    @ManyToOne(targetEntity = ProcessEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", referencedColumnName = "PROCESS_ID")
    public ProcessEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessEntity process) {
        this.process = process;
    }
}