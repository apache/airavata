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

import java.util.Objects;

/**
 * Domain model: CloudJobSubmission
 */
public class CloudJobSubmission {
    private String jobSubmissionInterfaceId;
    private SecurityProtocol securityProtocol;
    private String nodeId;
    private String executableType;
    private ProviderName providerName;
    private String userAccountName;

    public CloudJobSubmission() {}

    public String getJobSubmissionInterfaceId() {
        return jobSubmissionInterfaceId;
    }

    public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
        this.jobSubmissionInterfaceId = jobSubmissionInterfaceId;
    }

    public SecurityProtocol getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(SecurityProtocol securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getExecutableType() {
        return executableType;
    }

    public void setExecutableType(String executableType) {
        this.executableType = executableType;
    }

    public ProviderName getProviderName() {
        return providerName;
    }

    public void setProviderName(ProviderName providerName) {
        this.providerName = providerName;
    }

    public String getUserAccountName() {
        return userAccountName;
    }

    public void setUserAccountName(String userAccountName) {
        this.userAccountName = userAccountName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloudJobSubmission that = (CloudJobSubmission) o;
        return Objects.equals(jobSubmissionInterfaceId, that.jobSubmissionInterfaceId)
                && Objects.equals(securityProtocol, that.securityProtocol)
                && Objects.equals(nodeId, that.nodeId)
                && Objects.equals(executableType, that.executableType)
                && Objects.equals(providerName, that.providerName)
                && Objects.equals(userAccountName, that.userAccountName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                jobSubmissionInterfaceId, securityProtocol, nodeId, executableType, providerName, userAccountName);
    }

    @Override
    public String toString() {
        return "CloudJobSubmission{" + "jobSubmissionInterfaceId=" + jobSubmissionInterfaceId + ", securityProtocol="
                + securityProtocol + ", nodeId=" + nodeId + ", executableType=" + executableType + ", providerName="
                + providerName + ", userAccountName=" + userAccountName + "}";
    }
}
