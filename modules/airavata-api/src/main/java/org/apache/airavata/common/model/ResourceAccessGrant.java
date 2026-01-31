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

import java.util.Date;
import java.util.Objects;

/**
 * Domain model: unified resource access grant linking a credential to a compute resource
 * with access and deployment settings.
 */
public class ResourceAccessGrant {
    private Long id;
    private String gatewayId;
    private String credentialToken;
    private String computeResourceId;
    private String loginUsername;
    private String executablePath;
    private String description;
    private String defaultQueueName;
    private int defaultNodeCount;
    private int defaultCpuCount;
    private int defaultWalltime;
    private ApplicationParallelismType parallelism;
    private boolean enabled = true;
    private Date creationTime;
    private Date updateTime;

    public ResourceAccessGrant() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getCredentialToken() {
        return credentialToken;
    }

    public void setCredentialToken(String credentialToken) {
        this.credentialToken = credentialToken;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getDefaultCpuCount() {
        return defaultCpuCount;
    }

    public void setDefaultCpuCount(int defaultCpuCount) {
        this.defaultCpuCount = defaultCpuCount;
    }

    public int getDefaultWalltime() {
        return defaultWalltime;
    }

    public void setDefaultWalltime(int defaultWalltime) {
        this.defaultWalltime = defaultWalltime;
    }

    public ApplicationParallelismType getParallelism() {
        return parallelism;
    }

    public void setParallelism(ApplicationParallelismType parallelism) {
        this.parallelism = parallelism;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceAccessGrant that = (ResourceAccessGrant) o;
        return defaultNodeCount == that.defaultNodeCount
                && defaultCpuCount == that.defaultCpuCount
                && defaultWalltime == that.defaultWalltime
                && enabled == that.enabled
                && Objects.equals(id, that.id)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(credentialToken, that.credentialToken)
                && Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(loginUsername, that.loginUsername)
                && Objects.equals(executablePath, that.executablePath)
                && Objects.equals(description, that.description)
                && Objects.equals(defaultQueueName, that.defaultQueueName)
                && parallelism == that.parallelism;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                gatewayId,
                credentialToken,
                computeResourceId,
                loginUsername,
                executablePath,
                description,
                defaultQueueName,
                defaultNodeCount,
                defaultCpuCount,
                defaultWalltime,
                parallelism,
                enabled);
    }
}
