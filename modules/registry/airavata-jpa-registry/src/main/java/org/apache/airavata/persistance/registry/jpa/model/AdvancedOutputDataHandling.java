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

@DataCache
@Entity
@Table(name = "ADVANCE_OUTPUT_DATA_HANDLING")
public class AdvancedOutputDataHandling {
    @Id
    @GeneratedValue
    @Column(name = "OUTPUT_DATA_HANDLING_ID")
    private int outputDataHandlingId;
    @Column(name = "EXPERIMENT_ID")
    private String expId;
    @Column(name = "TASK_ID")
    private String taskId;
    @Column(name = "OUTPUT_DATA_DIR")
    private String outputDataDir;
    @Column(name = "DATA_REG_URL")
    private String dataRegUrl;
    @Column(name = "PERSIST_OUTPUT_DATA")
    private boolean persistOutputData;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment experiment;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "TASK_ID")
    private TaskDetail task;

    public int getOutputDataHandlingId() {
        return outputDataHandlingId;
    }

    public void setOutputDataHandlingId(int outputDataHandlingId) {
        this.outputDataHandlingId = outputDataHandlingId;
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

    public String getOutputDataDir() {
        return outputDataDir;
    }

    public void setOutputDataDir(String outputDataDir) {
        this.outputDataDir = outputDataDir;
    }

    public String getDataRegUrl() {
        return dataRegUrl;
    }

    public void setDataRegUrl(String dataRegUrl) {
        this.dataRegUrl = dataRegUrl;
    }

    public boolean isPersistOutputData() {
        return persistOutputData;
    }

    public void setPersistOutputData(boolean persistOutputData) {
        this.persistOutputData = persistOutputData;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public TaskDetail getTask() {
        return task;
    }

    public void setTask(TaskDetail task) {
        this.task = task;
    }
}
