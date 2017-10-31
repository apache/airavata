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
package org.apache.airavata.k8s.api.server.model.process;

import org.apache.airavata.k8s.api.server.model.commons.ErrorModel;
import org.apache.airavata.k8s.api.server.model.experiment.Experiment;
import org.apache.airavata.k8s.api.server.model.task.TaskModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */

@Entity
@Table(name = "PROCESS_MODEL")
public class ProcessModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private Experiment experiment;

    private long creationTime;
    private long lastUpdateTime;

    @OneToMany(mappedBy = "processModel", cascade = CascadeType.ALL)
    private List<ProcessStatus> processStatuses = new ArrayList<>();

    @OneToMany(mappedBy = "parentProcess", cascade = CascadeType.ALL)
    private List<TaskModel> tasks = new ArrayList<>();

    private String taskDag;

    @OneToMany
    private List<ErrorModel> processErrors = new ArrayList<>();

    private String experimentDataDir;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public List<ProcessStatus> getProcessStatuses() {
        return processStatuses;
    }

    public void setProcessStatuses(List<ProcessStatus> processStatuses) {
        this.processStatuses = processStatuses;
    }

    public List<TaskModel> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskModel> tasks) {
        this.tasks = tasks;
    }

    public String getTaskDag() {
        return taskDag;
    }

    public void setTaskDag(String taskDag) {
        this.taskDag = taskDag;
    }

    public List<ErrorModel> getProcessErrors() {
        return processErrors;
    }

    public void setProcessErrors(List<ErrorModel> processErrors) {
        this.processErrors = processErrors;
    }

    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public void setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
    }
}
