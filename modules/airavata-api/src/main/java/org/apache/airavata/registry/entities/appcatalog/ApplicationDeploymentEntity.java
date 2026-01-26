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
package org.apache.airavata.registry.entities.appcatalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.ApplicationParallelismType;
import org.apache.airavata.common.model.DeploymentCommandType;
import org.apache.airavata.common.model.LibraryPathType;

/**
 * The persistent class for the application_deployment database table.
 */
@Entity
@Table(name = "APPLICATION_DEPLOYMENT")
public class ApplicationDeploymentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "DEPLOYMENT_ID", nullable = false)
    private String appDeploymentId;

    @Column(name = "APPLICATION_DESC")
    private String appDeploymentDescription;

    @Column(
            name = "CREATION_TIME",
            nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Timestamp creationTime;

    @Column(name = "ENV_MODULE_LOAD_CMD")
    private String envModuleLoadCmd;

    @Column(name = "EXECUTABLE_PATH")
    private String executablePath;

    @Column(name = "GATEWAY_ID", nullable = false, updatable = false)
    private String gatewayId;

    @Column(name = "PARALLELISM")
    @Enumerated(EnumType.STRING)
    private ApplicationParallelismType parallelism;

    @Column(
            name = "UPDATE_TIME",
            nullable = false,
            columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updateTime;

    @Column(name = "COMPUTE_HOSTID", nullable = false)
    private String computeHostId;

    @Column(name = "APP_MODULE_ID", nullable = false)
    private String appModuleId;

    /**
     * Optional direct reference to the application interface.
     * This enables deployments to be linked directly to interfaces,
     * bypassing the module indirection.
     * 
     * <p>When set, this provides a direct link from deployment to interface,
     * simplifying the relationship model. The appModuleId is retained for
     * backward compatibility with existing data.
     */
    @Column(name = "APPLICATION_INTERFACE_ID")
    private String applicationInterfaceId;

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

    // Unified command entity for all command types
    @OneToMany(
            targetEntity = ApplicationDeploymentCommandEntity.class,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "applicationDeployment",
            fetch = FetchType.EAGER)
    private List<ApplicationDeploymentCommandEntity> commands;

    @OneToMany(
            targetEntity = AppEnvironmentEntity.class,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "applicationDeployment",
            fetch = FetchType.EAGER)
    private List<AppEnvironmentEntity> setEnvironment;

    // Unified library path entity for prepend and append paths
    @OneToMany(
            targetEntity = LibraryPathEntity.class,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "applicationDeployment",
            fetch = FetchType.EAGER)
    private List<LibraryPathEntity> libraryPaths;

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

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
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

    // ============================================
    // UNIFIED COMMAND ACCESSORS
    // ============================================

    public List<ApplicationDeploymentCommandEntity> getCommands() {
        return commands;
    }

    public void setCommands(List<ApplicationDeploymentCommandEntity> commands) {
        this.commands = commands;
    }

    /**
     * Get module load commands filtered from unified commands list.
     */
    public List<ApplicationDeploymentCommandEntity> getModuleLoadCommands() {
        if (commands == null) return new ArrayList<>();
        return commands.stream()
                .filter(c -> c.getCommandType() == DeploymentCommandType.MODULE_LOAD)
                .collect(Collectors.toList());
    }

    /**
     * Get prejob commands filtered from unified commands list.
     */
    public List<ApplicationDeploymentCommandEntity> getPreJobCommands() {
        if (commands == null) return new ArrayList<>();
        return commands.stream()
                .filter(c -> c.getCommandType() == DeploymentCommandType.PREJOB)
                .collect(Collectors.toList());
    }

    /**
     * Get postjob commands filtered from unified commands list.
     */
    public List<ApplicationDeploymentCommandEntity> getPostJobCommands() {
        if (commands == null) return new ArrayList<>();
        return commands.stream()
                .filter(c -> c.getCommandType() == DeploymentCommandType.POSTJOB)
                .collect(Collectors.toList());
    }

    // ============================================
    // ENVIRONMENT ACCESSORS
    // ============================================

    public List<AppEnvironmentEntity> getSetEnvironment() {
        return setEnvironment;
    }

    public void setSetEnvironment(List<AppEnvironmentEntity> setEnvironment) {
        this.setEnvironment = setEnvironment;
    }

    // ============================================
    // UNIFIED LIBRARY PATH ACCESSORS
    // ============================================

    public List<LibraryPathEntity> getLibraryPaths() {
        return libraryPaths;
    }

    public void setLibraryPaths(List<LibraryPathEntity> libraryPaths) {
        this.libraryPaths = libraryPaths;
    }

    /**
     * Get library prepend paths filtered from unified library paths list.
     */
    public List<LibraryPathEntity> getLibPrependPaths() {
        if (libraryPaths == null) return new ArrayList<>();
        return libraryPaths.stream()
                .filter(p -> p.getPathType() == LibraryPathType.PREPEND)
                .collect(Collectors.toList());
    }

    /**
     * Get library append paths filtered from unified library paths list.
     */
    public List<LibraryPathEntity> getLibAppendPaths() {
        if (libraryPaths == null) return new ArrayList<>();
        return libraryPaths.stream()
                .filter(p -> p.getPathType() == LibraryPathType.APPEND)
                .collect(Collectors.toList());
    }
}
