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
@Table(name = "ADVANCE_INPUT_DATA_HANDLING")
public class AdvancedInputDataHandling implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "INPUT_DATA_HANDLING_ID")
    private int dataHandlingId;
    @Column(name = "EXPERIMENT_ID")
    private String expId;
    @Column(name = "TASK_ID")
    private String taskId;
    @Column(name = "WORKING_DIR_PARENT")
    private String parentWorkingDir;
    @Column(name = "UNIQUE_WORKING_DIR")
    private String workingDir;
    @Column(name = "STAGE_INPUT_FILES_TO_WORKING_DIR")
    private boolean stageInputsToWorkingDir;
    @Column(name = "CLEAN_AFTER_JOB")
    private boolean cleanAfterJob;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment experiment;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "TASK_ID")
    private TaskDetail task;

    public int getDataHandlingId() {
        return dataHandlingId;
    }

    public void setDataHandlingId(int dataHandlingId) {
        this.dataHandlingId = dataHandlingId;
    }

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getParentWorkingDir() {
        return parentWorkingDir;
    }

    public void setParentWorkingDir(String parentWorkingDir) {
        this.parentWorkingDir = parentWorkingDir;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public boolean isStageInputsToWorkingDir() {
        return stageInputsToWorkingDir;
    }

    public void setStageInputsToWorkingDir(boolean stageInputsToWorkingDir) {
        this.stageInputsToWorkingDir = stageInputsToWorkingDir;
    }

    public boolean isCleanAfterJob() {
        return cleanAfterJob;
    }

    public void setCleanAfterJob(boolean cleanAfterJob) {
        this.cleanAfterJob = cleanAfterJob;
    }
}
