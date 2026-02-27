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
package org.apache.airavata.core.model;

import java.util.Objects;

/**
 * Unified identifier for resources in the Airavata hierarchy.
 *
 * <p>Replaces the former ProcessIdentifier, TaskIdentifier, and JobIdentifier matryoshka pattern
 * with a single class using nullable fields and static factory methods.
 */
public class ResourceIdentifier {
    private String jobId;
    private String taskId;
    private String processId;
    private String experimentId;
    private String gatewayId;

    public ResourceIdentifier() {}

    public static ResourceIdentifier forProcess(String processId, String experimentId, String gatewayId) {
        var id = new ResourceIdentifier();
        id.processId = processId;
        id.experimentId = experimentId;
        id.gatewayId = gatewayId;
        return id;
    }

    public static ResourceIdentifier forTask(String taskId, String processId, String experimentId, String gatewayId) {
        var id = new ResourceIdentifier();
        id.taskId = taskId;
        id.processId = processId;
        id.experimentId = experimentId;
        id.gatewayId = gatewayId;
        return id;
    }

    public static ResourceIdentifier forJob(
            String jobId, String taskId, String processId, String experimentId, String gatewayId) {
        var id = new ResourceIdentifier();
        id.jobId = jobId;
        id.taskId = taskId;
        id.processId = processId;
        id.experimentId = experimentId;
        id.gatewayId = gatewayId;
        return id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceIdentifier that = (ResourceIdentifier) o;
        return Objects.equals(jobId, that.jobId)
                && Objects.equals(taskId, that.taskId)
                && Objects.equals(processId, that.processId)
                && Objects.equals(experimentId, that.experimentId)
                && Objects.equals(gatewayId, that.gatewayId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, taskId, processId, experimentId, gatewayId);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("ResourceIdentifier{");
        if (jobId != null) sb.append("jobId=").append(jobId).append(", ");
        if (taskId != null) sb.append("taskId=").append(taskId).append(", ");
        sb.append("processId=").append(processId);
        sb.append(", experimentId=").append(experimentId);
        sb.append(", gatewayId=").append(gatewayId);
        sb.append('}');
        return sb.toString();
    }
}
