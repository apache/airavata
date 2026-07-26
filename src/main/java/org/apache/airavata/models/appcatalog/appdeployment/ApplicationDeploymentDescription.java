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
package org.apache.airavata.models.appcatalog.appdeployment;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.models.parallelism.ApplicationParallelismType;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription}.
 */
public record ApplicationDeploymentDescription(
        String appDeploymentId,
        String appModuleId,
        String computeHostId,
        String executablePath,
        ApplicationParallelismType parallelism,
        String appDeploymentDescription,
        List<CommandObject> moduleLoadCmds,
        List<SetEnvPaths> libPrependPaths,
        List<SetEnvPaths> libAppendPaths,
        List<SetEnvPaths> setEnvironment,
        List<CommandObject> preJobCommands,
        List<CommandObject> postJobCommands,
        String defaultQueueName,
        int defaultNodeCount,
        int defaultCpuCount,
        int defaultWalltime,
        boolean editableByUser) {

    public ApplicationDeploymentDescription {
        moduleLoadCmds = moduleLoadCmds == null ? List.of() : List.copyOf(moduleLoadCmds);
        libPrependPaths = libPrependPaths == null ? List.of() : List.copyOf(libPrependPaths);
        libAppendPaths = libAppendPaths == null ? List.of() : List.copyOf(libAppendPaths);
        setEnvironment = setEnvironment == null ? List.of() : List.copyOf(setEnvironment);
        preJobCommands = preJobCommands == null ? List.of() : List.copyOf(preJobCommands);
        postJobCommands = postJobCommands == null ? List.of() : List.copyOf(postJobCommands);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String appDeploymentId = "";
        private String appModuleId = "";
        private String computeHostId = "";
        private String executablePath = "";
        private ApplicationParallelismType parallelism = ApplicationParallelismType.APPLICATION_PARALLELISM_TYPE_UNKNOWN;
        private String appDeploymentDescription = "";
        private List<CommandObject> moduleLoadCmds = new ArrayList<>();
        private List<SetEnvPaths> libPrependPaths = new ArrayList<>();
        private List<SetEnvPaths> libAppendPaths = new ArrayList<>();
        private List<SetEnvPaths> setEnvironment = new ArrayList<>();
        private List<CommandObject> preJobCommands = new ArrayList<>();
        private List<CommandObject> postJobCommands = new ArrayList<>();
        private String defaultQueueName = "";
        private int defaultNodeCount;
        private int defaultCpuCount;
        private int defaultWalltime;
        private boolean editableByUser;

        private Builder() {}

        private Builder(ApplicationDeploymentDescription source) {
            this.appDeploymentId = source.appDeploymentId;
            this.appModuleId = source.appModuleId;
            this.computeHostId = source.computeHostId;
            this.executablePath = source.executablePath;
            this.parallelism = source.parallelism;
            this.appDeploymentDescription = source.appDeploymentDescription;
            this.moduleLoadCmds = new ArrayList<>(source.moduleLoadCmds);
            this.libPrependPaths = new ArrayList<>(source.libPrependPaths);
            this.libAppendPaths = new ArrayList<>(source.libAppendPaths);
            this.setEnvironment = new ArrayList<>(source.setEnvironment);
            this.preJobCommands = new ArrayList<>(source.preJobCommands);
            this.postJobCommands = new ArrayList<>(source.postJobCommands);
            this.defaultQueueName = source.defaultQueueName;
            this.defaultNodeCount = source.defaultNodeCount;
            this.defaultCpuCount = source.defaultCpuCount;
            this.defaultWalltime = source.defaultWalltime;
            this.editableByUser = source.editableByUser;
        }

        public Builder setAppDeploymentId(String appDeploymentId) {
            this.appDeploymentId = appDeploymentId;
            return this;
        }

        public Builder setAppModuleId(String appModuleId) {
            this.appModuleId = appModuleId;
            return this;
        }

        public Builder setComputeHostId(String computeHostId) {
            this.computeHostId = computeHostId;
            return this;
        }

        public Builder setExecutablePath(String executablePath) {
            this.executablePath = executablePath;
            return this;
        }

        public Builder setParallelism(ApplicationParallelismType parallelism) {
            this.parallelism = parallelism;
            return this;
        }

        public Builder setAppDeploymentDescription(String appDeploymentDescription) {
            this.appDeploymentDescription = appDeploymentDescription;
            return this;
        }

        public Builder addModuleLoadCmds(CommandObject value) {
            this.moduleLoadCmds.add(value);
            return this;
        }

        public Builder addAllModuleLoadCmds(Iterable<CommandObject> values) {
            values.forEach(this.moduleLoadCmds::add);
            return this;
        }

        public Builder clearModuleLoadCmds() {
            this.moduleLoadCmds.clear();
            return this;
        }

        public Builder addLibPrependPaths(SetEnvPaths value) {
            this.libPrependPaths.add(value);
            return this;
        }

        public Builder addAllLibPrependPaths(Iterable<SetEnvPaths> values) {
            values.forEach(this.libPrependPaths::add);
            return this;
        }

        public Builder clearLibPrependPaths() {
            this.libPrependPaths.clear();
            return this;
        }

        public Builder addLibAppendPaths(SetEnvPaths value) {
            this.libAppendPaths.add(value);
            return this;
        }

        public Builder addAllLibAppendPaths(Iterable<SetEnvPaths> values) {
            values.forEach(this.libAppendPaths::add);
            return this;
        }

        public Builder clearLibAppendPaths() {
            this.libAppendPaths.clear();
            return this;
        }

        public Builder addSetEnvironment(SetEnvPaths value) {
            this.setEnvironment.add(value);
            return this;
        }

        public Builder addAllSetEnvironment(Iterable<SetEnvPaths> values) {
            values.forEach(this.setEnvironment::add);
            return this;
        }

        public Builder clearSetEnvironment() {
            this.setEnvironment.clear();
            return this;
        }

        public Builder addPreJobCommands(CommandObject value) {
            this.preJobCommands.add(value);
            return this;
        }

        public Builder addAllPreJobCommands(Iterable<CommandObject> values) {
            values.forEach(this.preJobCommands::add);
            return this;
        }

        public Builder clearPreJobCommands() {
            this.preJobCommands.clear();
            return this;
        }

        public Builder addPostJobCommands(CommandObject value) {
            this.postJobCommands.add(value);
            return this;
        }

        public Builder addAllPostJobCommands(Iterable<CommandObject> values) {
            values.forEach(this.postJobCommands::add);
            return this;
        }

        public Builder clearPostJobCommands() {
            this.postJobCommands.clear();
            return this;
        }

        public Builder setDefaultQueueName(String defaultQueueName) {
            this.defaultQueueName = defaultQueueName;
            return this;
        }

        public Builder setDefaultNodeCount(int defaultNodeCount) {
            this.defaultNodeCount = defaultNodeCount;
            return this;
        }

        public Builder setDefaultCpuCount(int defaultCpuCount) {
            this.defaultCpuCount = defaultCpuCount;
            return this;
        }

        public Builder setDefaultWalltime(int defaultWalltime) {
            this.defaultWalltime = defaultWalltime;
            return this;
        }

        public Builder setEditableByUser(boolean editableByUser) {
            this.editableByUser = editableByUser;
            return this;
        }

        public ApplicationDeploymentDescription build() {
            return new ApplicationDeploymentDescription(
                    appDeploymentId, appModuleId, computeHostId, executablePath, parallelism,
                    appDeploymentDescription, moduleLoadCmds, libPrependPaths, libAppendPaths, setEnvironment,
                    preJobCommands, postJobCommands, defaultQueueName, defaultNodeCount, defaultCpuCount,
                    defaultWalltime, editableByUser);
        }
    }
}
