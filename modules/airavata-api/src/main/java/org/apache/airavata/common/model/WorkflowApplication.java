/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: WorkflowApplication
 */
public class WorkflowApplication {
    private String id;
    private String processId;
    private String applicationInterfaceId;
    private String computeResourceId;
    private String queueName;
    private int nodeCount;
    private int coreCount;
    private int wallTimeLimit;
    private int physicalMemory;
    private List<ApplicationStatus> statuses;
    private List<ErrorModel> errors;
    private long createdAt;
    private long updatedAt;

    public WorkflowApplication() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getCoreCount() {
        return coreCount;
    }

    public void setCoreCount(int coreCount) {
        this.coreCount = coreCount;
    }

    public int getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(int wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    public int getPhysicalMemory() {
        return physicalMemory;
    }

    public void setPhysicalMemory(int physicalMemory) {
        this.physicalMemory = physicalMemory;
    }

    public List<ApplicationStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<ApplicationStatus> statuses) {
        this.statuses = statuses;
    }

    public List<ErrorModel> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorModel> errors) {
        this.errors = errors;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowApplication that = (WorkflowApplication) o;
        return Objects.equals(id, that.id)
                && Objects.equals(processId, that.processId)
                && Objects.equals(applicationInterfaceId, that.applicationInterfaceId)
                && Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(queueName, that.queueName)
                && Objects.equals(nodeCount, that.nodeCount)
                && Objects.equals(coreCount, that.coreCount)
                && Objects.equals(wallTimeLimit, that.wallTimeLimit)
                && Objects.equals(physicalMemory, that.physicalMemory)
                && Objects.equals(statuses, that.statuses)
                && Objects.equals(errors, that.errors)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                processId,
                applicationInterfaceId,
                computeResourceId,
                queueName,
                nodeCount,
                coreCount,
                wallTimeLimit,
                physicalMemory,
                statuses,
                errors,
                createdAt,
                updatedAt);
    }

    @Override
    public String toString() {
        return "WorkflowApplication{" + "id=" + id + ", processId=" + processId + ", applicationInterfaceId="
                + applicationInterfaceId + ", computeResourceId=" + computeResourceId + ", queueName=" + queueName
                + ", nodeCount=" + nodeCount + ", coreCount=" + coreCount + ", wallTimeLimit=" + wallTimeLimit
                + ", physicalMemory=" + physicalMemory + ", statuses=" + statuses + ", errors=" + errors
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "}";
    }
}
