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
package org.apache.airavata.k8s.api.server.model.commons;

import org.apache.airavata.k8s.api.server.model.task.TaskModel;

import javax.persistence.*;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "ERROR_MODEL")
public class ErrorModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private long creationTime;
    private String actualErrorMessage;
    private String userFriendlyMessage;
    private boolean transientOrPersistent;

    @ManyToOne
    private TaskModel taskModel;

    @OneToMany
    private List<ErrorModel> rootCauseErrorList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
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

    public List<ErrorModel> getRootCauseErrorList() {
        return rootCauseErrorList;
    }

    public void setRootCauseErrorList(List<ErrorModel> rootCauseErrorList) {
        this.rootCauseErrorList = rootCauseErrorList;
    }

    public TaskModel getTaskModel() {
        return taskModel;
    }

    public ErrorModel setTaskModel(TaskModel taskModel) {
        this.taskModel = taskModel;
        return this;
    }
}
