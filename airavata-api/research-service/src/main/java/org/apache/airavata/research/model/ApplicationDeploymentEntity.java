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
package org.apache.airavata.research.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.JsonListConverter;
import org.apache.airavata.model.parallelism.proto.ApplicationParallelismType;

/**
 * The persistent class for the application_deployment database table.
 */
@Entity
@Table(name = "APPLICATION_DEPLOYMENT")
public class ApplicationDeploymentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "DEPLOYMENT_ID")
    private String appDeploymentId;

    @Column(name = "APPLICATION_DESC")
    private String appDeploymentDescription;

    @Column(name = "CREATION_TIME", nullable = false, updatable = false)
    private Timestamp creationTime;

    @Column(name = "ENV_MODULE_LOAD_CMD")
    private String envModuleLoadCmd;

    @Column(name = "EXECUTABLE_PATH")
    private String executablePath;

    @Column(name = "GATEWAY_ID", nullable = false, updatable = false)
    private String gatewayId;

    @Column(name = "parallelism")
    @Enumerated(EnumType.STRING)
    private ApplicationParallelismType parallelism;

    @Column(name = "UPDATE_TIME", nullable = false)
    private Timestamp updateTime;

    @Column(name = "COMPUTE_HOSTID")
    private String computeHostId;

    @Column(name = "APP_MODULE_ID")
    private String appModuleId;

    @Column(name = "DEFAULT_NODE_COUNT")
    private int defaultNodeCount;

    @Column(name = "DEFAULT_CPU_COUNT")
    private int defaultCPUCount;

    @Column(name = "DEFAULT_WALLTIME")
    private int defaultWallTime;

    @Column(name = "DEFAULT_QUEUE_NAME")
    private String defaultQueueName;

    @Column(name = "EDITABLE_BY_USER")
    private boolean editableByUser;

    @Lob
    @Column(name = "MODULE_LOAD_CMDS_JSON")
    @Convert(converter = JsonListConverter.class)
    private List<Map<String, Object>> moduleLoadCmds;

    @Lob
    @Column(name = "PREJOB_CMDS_JSON")
    @Convert(converter = JsonListConverter.class)
    private List<Map<String, Object>> preJobCommands;

    @Lob
    @Column(name = "POSTJOB_CMDS_JSON")
    @Convert(converter = JsonListConverter.class)
    private List<Map<String, Object>> postJobCommands;

    @Lob
    @Column(name = "LIB_PREPEND_PATHS_JSON")
    @Convert(converter = JsonListConverter.class)
    private List<Map<String, Object>> libPrependPaths;

    @Lob
    @Column(name = "LIB_APPEND_PATHS_JSON")
    @Convert(converter = JsonListConverter.class)
    private List<Map<String, Object>> libAppendPaths;

    @Lob
    @Column(name = "SET_ENVIRONMENT_JSON")
    @Convert(converter = JsonListConverter.class)
    private List<Map<String, Object>> setEnvironment;

    public ApplicationDeploymentEntity() {}

    public String getAppDeploymentId() {
        return appDeploymentId;
    }

    public void setAppDeploymentId(String appDeploymentId) {
        this.appDeploymentId = appDeploymentId;
    }

    public String getAppDeploymentDescription() {
        return appDeploymentDescription;
    }

    public void setAppDeploymentDescription(String appDeploymentDescription) {
        this.appDeploymentDescription = appDeploymentDescription;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getEnvModuleLoadCmd() {
        return envModuleLoadCmd;
    }

    public void setEnvModuleLoadCmd(String envModuleLoadCmd) {
        this.envModuleLoadCmd = envModuleLoadCmd;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public ApplicationParallelismType getParallelism() {
        return parallelism;
    }

    public void setParallelism(ApplicationParallelismType parallelism) {
        this.parallelism = parallelism;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getComputeHostId() {
        return computeHostId;
    }

    public void setComputeHostId(String computeHostId) {
        this.computeHostId = computeHostId;
    }

    public String getAppModuleId() {
        return appModuleId;
    }

    public void setAppModuleId(String appModuleId) {
        this.appModuleId = appModuleId;
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

    public int getDefaultWallTime() {
        return defaultWallTime;
    }

    public void setDefaultWallTime(int defaultWallTime) {
        this.defaultWallTime = defaultWallTime;
    }

    public String getDefaultQueueName() {
        return defaultQueueName;
    }

    public void setDefaultQueueName(String defaultQueueName) {
        this.defaultQueueName = defaultQueueName;
    }

    public boolean getEditableByUser() {
        return editableByUser;
    }

    public void setEditableByUser(boolean editableByUser) {
        this.editableByUser = editableByUser;
    }

    public List<Map<String, Object>> getModuleLoadCmds() {
        return moduleLoadCmds;
    }

    public void setModuleLoadCmds(List<Map<String, Object>> moduleLoadCmds) {
        this.moduleLoadCmds = moduleLoadCmds;
    }

    public List<Map<String, Object>> getPreJobCommands() {
        return preJobCommands;
    }

    public void setPreJobCommands(List<Map<String, Object>> preJobCommands) {
        this.preJobCommands = preJobCommands;
    }

    public List<Map<String, Object>> getPostJobCommands() {
        return postJobCommands;
    }

    public void setPostJobCommands(List<Map<String, Object>> postJobCommands) {
        this.postJobCommands = postJobCommands;
    }

    public List<Map<String, Object>> getLibPrependPaths() {
        return libPrependPaths;
    }

    public void setLibPrependPaths(List<Map<String, Object>> libPrependPaths) {
        this.libPrependPaths = libPrependPaths;
    }

    public List<Map<String, Object>> getLibAppendPaths() {
        return libAppendPaths;
    }

    public void setLibAppendPaths(List<Map<String, Object>> libAppendPaths) {
        this.libAppendPaths = libAppendPaths;
    }

    public List<Map<String, Object>> getSetEnvironment() {
        return setEnvironment;
    }

    public void setSetEnvironment(List<Map<String, Object>> setEnvironment) {
        this.setEnvironment = setEnvironment;
    }
}
