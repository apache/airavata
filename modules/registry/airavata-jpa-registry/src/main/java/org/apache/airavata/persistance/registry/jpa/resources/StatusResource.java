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

package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.DataTransferDetail;
import org.apache.airavata.persistance.registry.jpa.model.WorkflowNodeDetail;

import java.sql.Timestamp;
import java.util.List;

public class StatusResource extends AbstractResource {
    private int statusId;
    private ExperimentResource experimentResource;
    private WorkflowNodeDetail workflowNodeDetail;
    private DataTransferDetail dataTransferDetail;
    private TaskDetailResource taskDetailResource;
    private String jobId;
    private String state;
    private Timestamp statusUpdateTime;
    private String statusType;

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public ExperimentResource getExperimentResource() {
        return experimentResource;
    }

    public void setExperimentResource(ExperimentResource experimentResource) {
        this.experimentResource = experimentResource;
    }

    public WorkflowNodeDetail getWorkflowNodeDetail() {
        return workflowNodeDetail;
    }

    public void setWorkflowNodeDetail(WorkflowNodeDetail workflowNodeDetail) {
        this.workflowNodeDetail = workflowNodeDetail;
    }

    public DataTransferDetail getDataTransferDetail() {
        return dataTransferDetail;
    }

    public void setDataTransferDetail(DataTransferDetail dataTransferDetail) {
        this.dataTransferDetail = dataTransferDetail;
    }

    public TaskDetailResource getTaskDetailResource() {
        return taskDetailResource;
    }

    public void setTaskDetailResource(TaskDetailResource taskDetailResource) {
        this.taskDetailResource = taskDetailResource;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public void setStatusUpdateTime(Timestamp statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    @Override
    public Resource create(ResourceType type) {
        return null;
    }

    @Override
    public void remove(ResourceType type, Object name) {

    }

    @Override
    public Resource get(ResourceType type, Object name) {
        return null;
    }

    @Override
    public List<Resource> get(ResourceType type) {
        return null;
    }

    @Override
    public void save() {

    }
}
