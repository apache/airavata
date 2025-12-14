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
 * Domain model: ApplicationDeploymentDescription
 */
public class ApplicationDeploymentDescription {
    private String appDeploymentId;
    private String appModuleId;
    private String computeHostId;
    private String executablePath;
    private ApplicationParallelismType parallelism;
    private String appDeploymentDescription;
    private List<CommandObject> moduleLoadCmds;
    private List<SetEnvPaths> libPrependPaths;
    private List<SetEnvPaths> libAppendPaths;
    private List<SetEnvPaths> setEnvironment;
    private List<CommandObject> preJobCommands;
    private List<CommandObject> postJobCommands;
    private String defaultQueueName;
    private int defaultNodeCount;
    private int defaultCPUCount;
    private int defaultWalltime;
    private boolean editableByUser;

    public ApplicationDeploymentDescription() {}

    public String getAppDeploymentId() {
        return appDeploymentId;
    }

    public void setAppDeploymentId(String appDeploymentId) {
        this.appDeploymentId = appDeploymentId;
    }

    public String getAppModuleId() {
        return appModuleId;
    }

    public void setAppModuleId(String appModuleId) {
        this.appModuleId = appModuleId;
    }

    public String getComputeHostId() {
        return computeHostId;
    }

    public void setComputeHostId(String computeHostId) {
        this.computeHostId = computeHostId;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public ApplicationParallelismType getParallelism() {
        return parallelism;
    }

    public void setParallelism(ApplicationParallelismType parallelism) {
        this.parallelism = parallelism;
    }

    public String getAppDeploymentDescription() {
        return appDeploymentDescription;
    }

    public void setAppDeploymentDescription(String appDeploymentDescription) {
        this.appDeploymentDescription = appDeploymentDescription;
    }

    public List<CommandObject> getModuleLoadCmds() {
        return moduleLoadCmds;
    }

    public void setModuleLoadCmds(List<CommandObject> moduleLoadCmds) {
        this.moduleLoadCmds = moduleLoadCmds;
    }

    public List<SetEnvPaths> getLibPrependPaths() {
        return libPrependPaths;
    }

    public void setLibPrependPaths(List<SetEnvPaths> libPrependPaths) {
        this.libPrependPaths = libPrependPaths;
    }

    public List<SetEnvPaths> getLibAppendPaths() {
        return libAppendPaths;
    }

    public void setLibAppendPaths(List<SetEnvPaths> libAppendPaths) {
        this.libAppendPaths = libAppendPaths;
    }

    public List<SetEnvPaths> getSetEnvironment() {
        return setEnvironment;
    }

    public void setSetEnvironment(List<SetEnvPaths> setEnvironment) {
        this.setEnvironment = setEnvironment;
    }

    public List<CommandObject> getPreJobCommands() {
        return preJobCommands;
    }

    public void setPreJobCommands(List<CommandObject> preJobCommands) {
        this.preJobCommands = preJobCommands;
    }

    public List<CommandObject> getPostJobCommands() {
        return postJobCommands;
    }

    public void setPostJobCommands(List<CommandObject> postJobCommands) {
        this.postJobCommands = postJobCommands;
    }

    public String getDefaultQueueName() {
        return defaultQueueName;
    }

    public void setDefaultQueueName(String defaultQueueName) {
        this.defaultQueueName = defaultQueueName;
    }

    public int getDefaultNodeCount() {
        return defaultNodeCount;
    }

    public void setDefaultNodeCount(int defaultNodeCount) {
        this.defaultNodeCount = defaultNodeCount;
    }

    public int getDefaultCPUCount() {
        return defaultCPUCount;
    }

    public void setDefaultCPUCount(int defaultCPUCount) {
        this.defaultCPUCount = defaultCPUCount;
    }

    public int getDefaultWalltime() {
        return defaultWalltime;
    }

    public void setDefaultWalltime(int defaultWalltime) {
        this.defaultWalltime = defaultWalltime;
    }

    public boolean getEditableByUser() {
        return editableByUser;
    }

    public void setEditableByUser(boolean editableByUser) {
        this.editableByUser = editableByUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationDeploymentDescription that = (ApplicationDeploymentDescription) o;
        return Objects.equals(appDeploymentId, that.appDeploymentId)
                && Objects.equals(appModuleId, that.appModuleId)
                && Objects.equals(computeHostId, that.computeHostId)
                && Objects.equals(executablePath, that.executablePath)
                && Objects.equals(parallelism, that.parallelism)
                && Objects.equals(appDeploymentDescription, that.appDeploymentDescription)
                && Objects.equals(moduleLoadCmds, that.moduleLoadCmds)
                && Objects.equals(libPrependPaths, that.libPrependPaths)
                && Objects.equals(libAppendPaths, that.libAppendPaths)
                && Objects.equals(setEnvironment, that.setEnvironment)
                && Objects.equals(preJobCommands, that.preJobCommands)
                && Objects.equals(postJobCommands, that.postJobCommands)
                && Objects.equals(defaultQueueName, that.defaultQueueName)
                && Objects.equals(defaultNodeCount, that.defaultNodeCount)
                && Objects.equals(defaultCPUCount, that.defaultCPUCount)
                && Objects.equals(defaultWalltime, that.defaultWalltime)
                && Objects.equals(editableByUser, that.editableByUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                appDeploymentId,
                appModuleId,
                computeHostId,
                executablePath,
                parallelism,
                appDeploymentDescription,
                moduleLoadCmds,
                libPrependPaths,
                libAppendPaths,
                setEnvironment,
                preJobCommands,
                postJobCommands,
                defaultQueueName,
                defaultNodeCount,
                defaultCPUCount,
                defaultWalltime,
                editableByUser);
    }

    @Override
    public String toString() {
        return "ApplicationDeploymentDescription{" + "appDeploymentId=" + appDeploymentId + ", appModuleId="
                + appModuleId + ", computeHostId=" + computeHostId + ", executablePath=" + executablePath
                + ", parallelism=" + parallelism + ", appDeploymentDescription=" + appDeploymentDescription
                + ", moduleLoadCmds=" + moduleLoadCmds + ", libPrependPaths=" + libPrependPaths + ", libAppendPaths="
                + libAppendPaths + ", setEnvironment=" + setEnvironment + ", preJobCommands=" + preJobCommands
                + ", postJobCommands=" + postJobCommands + ", defaultQueueName=" + defaultQueueName
                + ", defaultNodeCount=" + defaultNodeCount + ", defaultCPUCount=" + defaultCPUCount
                + ", defaultWalltime=" + defaultWalltime + ", editableByUser=" + editableByUser + "}";
    }
}
