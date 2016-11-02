/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The persistent class for the application_deployment database table.
 */
@Entity
@Table(name = "application_deployment")
public class ApplicationDeploymentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "DEPLOYMENT_ID")
    private String deploymentId;

    @Column(name = "APPLICATION_DESC")
    private String applicationDesc;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "ENV_MODULE_LOAD_CMD")
    private String envModuleLoadCmd;

    @Column(name = "EXECUTABLE_PATH")
    private String executablePath;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "parallelism")
    private String parallelism;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @Column(name = "COMPUTE_HOSTID")
    private String computeHostid;

    @Column(name = "APP_MODULE_ID")
    private String applicationModuleId;

    public ApplicationDeploymentEntity() {
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getApplicationDesc() {
        return applicationDesc;
    }

    public void setApplicationDesc(String applicationDesc) {
        this.applicationDesc = applicationDesc;
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

    public String getParallelism() {
        return parallelism;
    }

    public void setParallelism(String parallelism) {
        this.parallelism = parallelism;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getComputeHostid() {
        return computeHostid;
    }

    public void setComputeHostid(String computeHostid) {
        this.computeHostid = computeHostid;
    }

    public String getApplicationModuleId() {
        return applicationModuleId;
    }

    public void setApplicationModuleId(String applicationModuleId) {
        this.applicationModuleId = applicationModuleId;
    }
}