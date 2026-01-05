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

import java.util.Objects;

/**
 * Domain model: ResourceJobManager
 */
public class ResourceJobManager {
    private String resourceJobManagerId;
    private ResourceJobManagerType resourceJobManagerType;
    private String pushMonitoringEndpoint;
    private String jobManagerBinPath;
    private java.util.Map<JobManagerCommand, java.lang.String> jobManagerCommands;
    private java.util.Map<org.apache.airavata.common.model.ApplicationParallelismType, java.lang.String>
            parallelismPrefix;

    public ResourceJobManager() {}

    public String getResourceJobManagerId() {
        return resourceJobManagerId;
    }

    public void setResourceJobManagerId(String resourceJobManagerId) {
        this.resourceJobManagerId = resourceJobManagerId;
    }

    public ResourceJobManagerType getResourceJobManagerType() {
        return resourceJobManagerType;
    }

    public void setResourceJobManagerType(ResourceJobManagerType resourceJobManagerType) {
        this.resourceJobManagerType = resourceJobManagerType;
    }

    public String getPushMonitoringEndpoint() {
        return pushMonitoringEndpoint;
    }

    public void setPushMonitoringEndpoint(String pushMonitoringEndpoint) {
        this.pushMonitoringEndpoint = pushMonitoringEndpoint;
    }

    public String getJobManagerBinPath() {
        return jobManagerBinPath;
    }

    public void setJobManagerBinPath(String jobManagerBinPath) {
        this.jobManagerBinPath = jobManagerBinPath;
    }

    public java.util.Map<JobManagerCommand, java.lang.String> getJobManagerCommands() {
        return jobManagerCommands;
    }

    public void setJobManagerCommands(java.util.Map<JobManagerCommand, java.lang.String> jobManagerCommands) {
        this.jobManagerCommands = jobManagerCommands;
    }

    public java.util.Map<org.apache.airavata.common.model.ApplicationParallelismType, java.lang.String>
            getParallelismPrefix() {
        return parallelismPrefix;
    }

    public void setParallelismPrefix(
            java.util.Map<org.apache.airavata.common.model.ApplicationParallelismType, java.lang.String>
                    parallelismPrefix) {
        this.parallelismPrefix = parallelismPrefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceJobManager that = (ResourceJobManager) o;
        return Objects.equals(resourceJobManagerId, that.resourceJobManagerId)
                && Objects.equals(resourceJobManagerType, that.resourceJobManagerType)
                && Objects.equals(pushMonitoringEndpoint, that.pushMonitoringEndpoint)
                && Objects.equals(jobManagerBinPath, that.jobManagerBinPath)
                && Objects.equals(jobManagerCommands, that.jobManagerCommands)
                && Objects.equals(parallelismPrefix, that.parallelismPrefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                resourceJobManagerId,
                resourceJobManagerType,
                pushMonitoringEndpoint,
                jobManagerBinPath,
                jobManagerCommands,
                parallelismPrefix);
    }

    @Override
    public String toString() {
        return "ResourceJobManager{" + "resourceJobManagerId=" + resourceJobManagerId + ", resourceJobManagerType="
                + resourceJobManagerType + ", pushMonitoringEndpoint=" + pushMonitoringEndpoint + ", jobManagerBinPath="
                + jobManagerBinPath + ", jobManagerCommands=" + jobManagerCommands + ", parallelismPrefix="
                + parallelismPrefix + "}";
    }
}
