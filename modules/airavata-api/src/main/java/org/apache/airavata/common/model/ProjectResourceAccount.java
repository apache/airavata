/**
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
 * Domain model: one account per compute resource for a project (workspace).
 * For project P, on resource R use account accountName with credential (gatewayId, credentialToken).
 * Account name must be one of the accounts discovered for this credential on this resource via cluster-info.
 */
public class ProjectResourceAccount {
    private String projectId;
    private String computeResourceId;
    private String gatewayId;
    private String credentialToken;
    private String accountName;

    public ProjectResourceAccount() {}

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
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

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectResourceAccount that = (ProjectResourceAccount) o;
        return Objects.equals(projectId, that.projectId)
                && Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(credentialToken, that.credentialToken)
                && Objects.equals(accountName, that.accountName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, computeResourceId, gatewayId, credentialToken, accountName);
    }
}
