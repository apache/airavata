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

import java.sql.Timestamp;
import java.util.List;

public class TaskDetailResource extends AbstractResource {
    private String taskId;
    private WorkflowNodeDetailResource workflowNodeDetailResource;
    private Timestamp creationTime;
    private String applicationId;
    private String applicationVersion;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public WorkflowNodeDetailResource getWorkflowNodeDetailResource() {
        return workflowNodeDetailResource;
    }

    public void setWorkflowNodeDetailResource(WorkflowNodeDetailResource workflowNodeDetailResource) {
        this.workflowNodeDetailResource = workflowNodeDetailResource;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
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
