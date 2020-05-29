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
package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "CHILD_JOB")
public class ChildJobEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CHILD_JOB_ID")
    private String childJobId;

    @Column(name = "PARENT_JOB_ID")
    private String parentJobId;

    @Column(name = "PARENT_TASK_ID")
    private String parentTaskId;

    @Column(name = "JOB_INDEX")
    private int jobIndex;

    @ManyToOne(targetEntity = JobEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "PARENT_JOB_ID", referencedColumnName = "JOB_ID", nullable = false, updatable = false),
            @JoinColumn(name = "PARENT_TASK_ID", referencedColumnName = "TASK_ID", nullable = false, updatable = false)
    })
    private JobEntity parentJob;

    @OneToMany(targetEntity = ChildJobStatusEntity.class, cascade = CascadeType.ALL,
            mappedBy = "job", fetch = FetchType.EAGER)
    @OrderBy("timeOfStateChange ASC")
    private List<ChildJobStatusEntity> jobStatuses;

    public String getChildJobId() {
        return childJobId;
    }

    public void setChildJobId(String childJobId) {
        this.childJobId = childJobId;
    }

    public String getParentJobId() {
        return parentJobId;
    }

    public void setParentJobId(String parentJobId) {
        this.parentJobId = parentJobId;
    }

    public int getJobIndex() {
        return jobIndex;
    }

    public void setJobIndex(int jobIndex) {
        this.jobIndex = jobIndex;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public JobEntity getParentJob() {
        return parentJob;
    }

    public void setParentJob(JobEntity parentJob) {
        this.parentJob = parentJob;
    }

    public List<ChildJobStatusEntity> getJobStatuses() {
        return jobStatuses;
    }

    public void setJobStatuses(List<ChildJobStatusEntity> jobStatuses) {
        this.jobStatuses = jobStatuses;
    }
}
