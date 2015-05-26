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

package org.apache.airavata.persistance.registry.jpa.model;

import org.apache.openjpa.persistence.DataCache;

import javax.persistence.*;
import java.io.Serializable;

@DataCache
@Entity
@Table(name = "APPLICATION_OUTPUT")
@IdClass(ApplicationOutput_PK.class)
public class ApplicationOutput implements Serializable {
    @Id
    @Column(name = "TASK_ID")
    private String taskId;
    @Id
    @Column(name = "OUTPUT_KEY")
    private String outputKey;
    @Column(name = "DATA_TYPE")
    private String dataType;
    @Lob
    @Column(name = "VALUE")
    private char[] value;

    @Column(name = "IS_REQUIRED")
    private boolean isRequired;
    @Column(name="REQUIRED_TO_COMMANDLINE")
    private boolean addedToCmd;
    @Column(name = "DATA_MOVEMENT")
    private boolean dataMovement;
    @Column(name = "DATA_NAME_LOCATION")
    private String dataNameLocation;
    @Column(name = "SEARCH_QUERY")
    private String searchQuery;
    @Column(name = "APP_ARGUMENT")
    private String applicationArgument;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "TASK_ID")
    private TaskDetail task;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public char[] getValue() {
        return value;
    }

    public void setValue(char[] value) {
        this.value = value;
    }

    public TaskDetail getTask() {
        return task;
    }

    public void setTask(TaskDetail task) {
        this.task = task;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public boolean isAddedToCmd() {
        return addedToCmd;
    }

    public void setAddedToCmd(boolean addedToCmd) {
        this.addedToCmd = addedToCmd;
    }

    public boolean isDataMovement() {
        return dataMovement;
    }

    public void setDataMovement(boolean dataMovement) {
        this.dataMovement = dataMovement;
    }

    public String getDataNameLocation() {
        return dataNameLocation;
    }

    public void setDataNameLocation(String dataNameLocation) {
        this.dataNameLocation = dataNameLocation;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getApplicationArgument() {
        return applicationArgument;
    }

    public void setApplicationArgument(String applicationArgument) {
        this.applicationArgument = applicationArgument;
    }
}
