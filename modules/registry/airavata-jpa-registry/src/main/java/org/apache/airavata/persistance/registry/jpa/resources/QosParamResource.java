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

import java.util.List;

public class QosParamResource extends AbstractResource {
    private int  qosId;
    private ExperimentResource experimentResource;
    private TaskDetailResource taskDetailResource;
    private String startExecutionAt;
    private String executeBefore;
    private int noOfRetries;

    public int getQosId() {
        return qosId;
    }

    public void setQosId(int qosId) {
        this.qosId = qosId;
    }

    public ExperimentResource getExperimentResource() {
        return experimentResource;
    }

    public void setExperimentResource(ExperimentResource experimentResource) {
        this.experimentResource = experimentResource;
    }

    public TaskDetailResource getTaskDetailResource() {
        return taskDetailResource;
    }

    public void setTaskDetailResource(TaskDetailResource taskDetailResource) {
        this.taskDetailResource = taskDetailResource;
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
