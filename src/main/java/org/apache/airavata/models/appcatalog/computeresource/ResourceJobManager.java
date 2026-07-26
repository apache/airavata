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
package org.apache.airavata.models.appcatalog.computeresource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resource Job Manager Information.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManager}.
 *
 * @param resourceJobManagerId Airavata Internal Unique ID.
 * @param resourceJobManagerType A typical HPC cluster has a single Job Manager.
 * @param pushMonitoringEndpoint If the job manager pushes out state changes to a database or bus,
 *     specify the service endpoint.
 * @param jobManagerBinPath Path to the Job Manager Installation Binary directory.
 * @param jobManagerCommands An enumeration of commonly used manager commands.
 * @param parallelismPrefix Prefix to use for each parallelism type.
 */
public record ResourceJobManager(
        String resourceJobManagerId,
        ResourceJobManagerType resourceJobManagerType,
        String pushMonitoringEndpoint,
        String jobManagerBinPath,
        Map<Integer, String> jobManagerCommands,
        Map<Integer, String> parallelismPrefix) {

    public ResourceJobManager {
        jobManagerCommands = jobManagerCommands == null ? Map.of() : Map.copyOf(jobManagerCommands);
        parallelismPrefix = parallelismPrefix == null ? Map.of() : Map.copyOf(parallelismPrefix);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String resourceJobManagerId = "";
        private ResourceJobManagerType resourceJobManagerType = ResourceJobManagerType.RESOURCE_JOB_MANAGER_TYPE_UNKNOWN;
        private String pushMonitoringEndpoint = "";
        private String jobManagerBinPath = "";
        private Map<Integer, String> jobManagerCommands = new LinkedHashMap<>();
        private Map<Integer, String> parallelismPrefix = new LinkedHashMap<>();

        private Builder() {}

        private Builder(ResourceJobManager source) {
            this.resourceJobManagerId = source.resourceJobManagerId;
            this.resourceJobManagerType = source.resourceJobManagerType;
            this.pushMonitoringEndpoint = source.pushMonitoringEndpoint;
            this.jobManagerBinPath = source.jobManagerBinPath;
            this.jobManagerCommands = new LinkedHashMap<>(source.jobManagerCommands);
            this.parallelismPrefix = new LinkedHashMap<>(source.parallelismPrefix);
        }

        public Builder setResourceJobManagerId(String resourceJobManagerId) {
            this.resourceJobManagerId = resourceJobManagerId;
            return this;
        }

        public Builder setResourceJobManagerType(ResourceJobManagerType resourceJobManagerType) {
            this.resourceJobManagerType = resourceJobManagerType;
            return this;
        }

        public Builder setPushMonitoringEndpoint(String pushMonitoringEndpoint) {
            this.pushMonitoringEndpoint = pushMonitoringEndpoint;
            return this;
        }

        public Builder setJobManagerBinPath(String jobManagerBinPath) {
            this.jobManagerBinPath = jobManagerBinPath;
            return this;
        }

        public Builder putJobManagerCommands(Integer key, String value) {
            this.jobManagerCommands.put(key, value);
            return this;
        }

        public Builder putAllJobManagerCommands(Map<Integer, String> values) {
            this.jobManagerCommands.putAll(values);
            return this;
        }

        public Builder clearJobManagerCommands() {
            this.jobManagerCommands.clear();
            return this;
        }

        public Builder putParallelismPrefix(Integer key, String value) {
            this.parallelismPrefix.put(key, value);
            return this;
        }

        public Builder putAllParallelismPrefix(Map<Integer, String> values) {
            this.parallelismPrefix.putAll(values);
            return this;
        }

        public Builder clearParallelismPrefix() {
            this.parallelismPrefix.clear();
            return this;
        }

        public ResourceJobManager build() {
            return new ResourceJobManager(
                    resourceJobManagerId, resourceJobManagerType, pushMonitoringEndpoint, jobManagerBinPath,
                    jobManagerCommands, parallelismPrefix);
        }
    }
}
