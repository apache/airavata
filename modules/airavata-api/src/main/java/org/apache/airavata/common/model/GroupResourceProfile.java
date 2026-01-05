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
 * Domain model: GroupResourceProfile
 */
public class GroupResourceProfile {
    private String gatewayId;
    private String groupResourceProfileId;
    private String groupResourceProfileName;
    private List<GroupComputeResourcePreference> computePreferences;
    private List<ComputeResourcePolicy> computeResourcePolicies;
    private List<BatchQueueResourcePolicy> batchQueueResourcePolicies;
    private long creationTime;
    private long updatedTime;
    private String defaultCredentialStoreToken;

    public GroupResourceProfile() {}

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getGroupResourceProfileName() {
        return groupResourceProfileName;
    }

    public void setGroupResourceProfileName(String groupResourceProfileName) {
        this.groupResourceProfileName = groupResourceProfileName;
    }

    public List<GroupComputeResourcePreference> getComputePreferences() {
        return computePreferences;
    }

    public void setComputePreferences(List<GroupComputeResourcePreference> computePreferences) {
        this.computePreferences = computePreferences;
    }

    public List<ComputeResourcePolicy> getComputeResourcePolicies() {
        return computeResourcePolicies;
    }

    public void setComputeResourcePolicies(List<ComputeResourcePolicy> computeResourcePolicies) {
        this.computeResourcePolicies = computeResourcePolicies;
    }

    public List<BatchQueueResourcePolicy> getBatchQueueResourcePolicies() {
        return batchQueueResourcePolicies;
    }

    public void setBatchQueueResourcePolicies(List<BatchQueueResourcePolicy> batchQueueResourcePolicies) {
        this.batchQueueResourcePolicies = batchQueueResourcePolicies;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getDefaultCredentialStoreToken() {
        return defaultCredentialStoreToken;
    }

    public void setDefaultCredentialStoreToken(String defaultCredentialStoreToken) {
        this.defaultCredentialStoreToken = defaultCredentialStoreToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupResourceProfile that = (GroupResourceProfile) o;
        return Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(groupResourceProfileId, that.groupResourceProfileId)
                && Objects.equals(groupResourceProfileName, that.groupResourceProfileName)
                && Objects.equals(computePreferences, that.computePreferences)
                && Objects.equals(computeResourcePolicies, that.computeResourcePolicies)
                && Objects.equals(batchQueueResourcePolicies, that.batchQueueResourcePolicies)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(updatedTime, that.updatedTime)
                && Objects.equals(defaultCredentialStoreToken, that.defaultCredentialStoreToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                gatewayId,
                groupResourceProfileId,
                groupResourceProfileName,
                computePreferences,
                computeResourcePolicies,
                batchQueueResourcePolicies,
                creationTime,
                updatedTime,
                defaultCredentialStoreToken);
    }

    @Override
    public String toString() {
        return "GroupResourceProfile{" + "gatewayId=" + gatewayId + ", groupResourceProfileId=" + groupResourceProfileId
                + ", groupResourceProfileName=" + groupResourceProfileName + ", computePreferences="
                + computePreferences + ", computeResourcePolicies=" + computeResourcePolicies
                + ", batchQueueResourcePolicies=" + batchQueueResourcePolicies + ", creationTime=" + creationTime
                + ", updatedTime=" + updatedTime + ", defaultCredentialStoreToken=" + defaultCredentialStoreToken + "}";
    }
}
