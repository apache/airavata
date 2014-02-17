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

import javax.persistence.*;

@Entity
@Table(name = "QOS_PARAMS")
public class QosParam {
    @Id
    @GeneratedValue
    @Column(name = "QOS_ID")
    private int qosId;
    @Column(name = "EXPERIMENT_ID")
    private String expId;
    @Column(name = "TASK_ID")
    private String taskId;
    @Column(name = "START_EXECUTION_AT")
    private String startExecutionAt;
    @Column(name = "EXECUTE_BEFORE")
    private String executeBefore;
    @Column(name = "EXECUTE_BEFORE")
    private int noOfRetries;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment experiment;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "TASK_ID")
    private TaskDetail task;

    public int getQosId() {
        return qosId;
    }

    public void setQosId(int qosId) {
        this.qosId = qosId;
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

    public String getStartExecutionAt() {
        return startExecutionAt;
    }

    public void setStartExecutionAt(String startExecutionAt) {
        this.startExecutionAt = startExecutionAt;
    }

    public String getExecuteBefore() {
        return executeBefore;
    }

    public void setExecuteBefore(String executeBefore) {
        this.executeBefore = executeBefore;
    }

    public int getNoOfRetries() {
        return noOfRetries;
    }

    public void setNoOfRetries(int noOfRetries) {
        this.noOfRetries = noOfRetries;
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
