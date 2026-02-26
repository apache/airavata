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
package org.apache.airavata.compute.resource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Objects;

/**
 * Domain model: ComputeCapability
 * Describes the job execution capabilities of a {@link Resource}.
 */
public class ComputeCapability {
    /** Job submission protocol (e.g., "SSH", "LOCAL"). */
    private String protocol;
    /** Job manager type (e.g., "SLURM", "FORK"). */
    private String jobManagerType;
    /** Path to the job manager binaries (e.g., "/usr/bin"). */
    private String jobManagerBinPath;
    /** Command overrides keyed by job manager command type. */
    private Map<JobManagerCommand, String> jobManagerCommands;

    public ComputeCapability() {}

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /** Raw string getter — preserved for JSON serialization compatibility. */
    public String getJobManagerType() {
        return jobManagerType;
    }

    public void setJobManagerType(String jobManagerType) {
        this.jobManagerType = jobManagerType;
    }

    public String getJobManagerBinPath() {
        return jobManagerBinPath;
    }

    public void setJobManagerBinPath(String jobManagerBinPath) {
        this.jobManagerBinPath = jobManagerBinPath;
    }

    public Map<JobManagerCommand, String> getJobManagerCommands() {
        return jobManagerCommands;
    }

    public void setJobManagerCommands(Map<JobManagerCommand, String> jobManagerCommands) {
        this.jobManagerCommands = jobManagerCommands;
    }

    /** Typed accessor for the job manager type. */
    @JsonIgnore
    public ResourceJobManagerType getJobManagerTypeEnum() {
        return ResourceJobManagerType.fromString(jobManagerType);
    }

    /** Derive the coarser ComputeResourceType from the job manager type. */
    @JsonIgnore
    public ComputeResourceType getComputeResourceType() {
        return getJobManagerTypeEnum().toComputeResourceType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputeCapability that = (ComputeCapability) o;
        return Objects.equals(protocol, that.protocol)
                && Objects.equals(jobManagerType, that.jobManagerType)
                && Objects.equals(jobManagerBinPath, that.jobManagerBinPath)
                && Objects.equals(jobManagerCommands, that.jobManagerCommands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, jobManagerType, jobManagerBinPath, jobManagerCommands);
    }

    @Override
    public String toString() {
        return "ComputeCapability{" + "protocol=" + protocol + ", jobManagerType=" + jobManagerType
                + ", jobManagerBinPath=" + jobManagerBinPath + ", jobManagerCommands=" + jobManagerCommands + "}";
    }
}
