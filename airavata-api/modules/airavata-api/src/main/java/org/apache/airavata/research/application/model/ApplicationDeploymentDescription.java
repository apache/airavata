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
package org.apache.airavata.research.application.model;

import java.util.List;

/**
 * Domain model for application deployment configuration on a compute resource.
 */
public class ApplicationDeploymentDescription {

    private String appDeploymentId;
    private String appModuleId;
    private String computeHostId;
    private String executablePath;
    private String parallelism;
    private String appDeploymentDescription;
    private List<String> modulLoadCmds;
    private List<String> libPrependPaths;
    private List<String> libAppendPaths;
    private List<String> setEnvironment;
    private List<String> preJobCommands;
    private List<String> postJobCommands;
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

    public String getComputeResourceId() {
        return computeHostId;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getParallelism() {
        return parallelism;
    }

    public void setParallelism(String parallelism) {
        this.parallelism = parallelism;
    }

    public String getAppDeploymentDescription() {
        return appDeploymentDescription;
    }

    public void setAppDeploymentDescription(String appDeploymentDescription) {
        this.appDeploymentDescription = appDeploymentDescription;
    }

    public List<String> getModulLoadCmds() {
        return modulLoadCmds;
    }

    public void setModulLoadCmds(List<String> modulLoadCmds) {
        this.modulLoadCmds = modulLoadCmds;
    }

    public List<String> getLibPrependPaths() {
        return libPrependPaths;
    }

    public void setLibPrependPaths(List<String> libPrependPaths) {
        this.libPrependPaths = libPrependPaths;
    }

    public List<String> getLibAppendPaths() {
        return libAppendPaths;
    }

    public void setLibAppendPaths(List<String> libAppendPaths) {
        this.libAppendPaths = libAppendPaths;
    }

    public List<String> getSetEnvironment() {
        return setEnvironment;
    }

    public void setSetEnvironment(List<String> setEnvironment) {
        this.setEnvironment = setEnvironment;
    }

    public List<String> getPreJobCommands() {
        return preJobCommands;
    }

    public void setPreJobCommands(List<String> preJobCommands) {
        this.preJobCommands = preJobCommands;
    }

    public List<String> getPostJobCommands() {
        return postJobCommands;
    }

    public void setPostJobCommands(List<String> postJobCommands) {
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
}
