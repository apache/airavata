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
 * Domain model: GroupComputeResourcePreference
 */
public class GroupComputeResourcePreference {
    private String computeResourceId;
    private String groupResourceProfileId;
    private boolean overridebyAiravata;
    private String loginUserName;
    private String scratchLocation;
    private JobSubmissionProtocol preferredJobSubmissionProtocol;
    private DataMovementProtocol preferredDataMovementProtocol;
    private String resourceSpecificCredentialStoreToken;
    private ComputeResourceType resourceType;
    private EnvironmentSpecificPreferences specificPreferences;

    public GroupComputeResourcePreference() {}

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public boolean getOverridebyAiravata() {
        return overridebyAiravata;
    }

    public void setOverridebyAiravata(boolean overridebyAiravata) {
        this.overridebyAiravata = overridebyAiravata;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String getScratchLocation() {
        return scratchLocation;
    }

    public void setScratchLocation(String scratchLocation) {
        this.scratchLocation = scratchLocation;
    }

    public JobSubmissionProtocol getPreferredJobSubmissionProtocol() {
        return preferredJobSubmissionProtocol;
    }

    public void setPreferredJobSubmissionProtocol(JobSubmissionProtocol preferredJobSubmissionProtocol) {
        this.preferredJobSubmissionProtocol = preferredJobSubmissionProtocol;
    }

    public DataMovementProtocol getPreferredDataMovementProtocol() {
        return preferredDataMovementProtocol;
    }

    public void setPreferredDataMovementProtocol(DataMovementProtocol preferredDataMovementProtocol) {
        this.preferredDataMovementProtocol = preferredDataMovementProtocol;
    }

    public String getResourceSpecificCredentialStoreToken() {
        return resourceSpecificCredentialStoreToken;
    }

    public void setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
        this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
    }

    public ComputeResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ComputeResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public EnvironmentSpecificPreferences getSpecificPreferences() {
        return specificPreferences;
    }

    public void setSpecificPreferences(EnvironmentSpecificPreferences specificPreferences) {
        this.specificPreferences = specificPreferences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupComputeResourcePreference that = (GroupComputeResourcePreference) o;
        return Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(groupResourceProfileId, that.groupResourceProfileId)
                && Objects.equals(overridebyAiravata, that.overridebyAiravata)
                && Objects.equals(loginUserName, that.loginUserName)
                && Objects.equals(scratchLocation, that.scratchLocation)
                && Objects.equals(preferredJobSubmissionProtocol, that.preferredJobSubmissionProtocol)
                && Objects.equals(preferredDataMovementProtocol, that.preferredDataMovementProtocol)
                && Objects.equals(resourceSpecificCredentialStoreToken, that.resourceSpecificCredentialStoreToken)
                && Objects.equals(resourceType, that.resourceType)
                && Objects.equals(specificPreferences, that.specificPreferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                computeResourceId,
                groupResourceProfileId,
                overridebyAiravata,
                loginUserName,
                scratchLocation,
                preferredJobSubmissionProtocol,
                preferredDataMovementProtocol,
                resourceSpecificCredentialStoreToken,
                resourceType,
                specificPreferences);
    }

    @Override
    public String toString() {
        return "GroupComputeResourcePreference{" + "computeResourceId=" + computeResourceId
                + ", groupResourceProfileId=" + groupResourceProfileId + ", overridebyAiravata=" + overridebyAiravata
                + ", loginUserName=" + loginUserName + ", scratchLocation=" + scratchLocation
                + ", preferredJobSubmissionProtocol=" + preferredJobSubmissionProtocol
                + ", preferredDataMovementProtocol=" + preferredDataMovementProtocol
                + ", resourceSpecificCredentialStoreToken=" + resourceSpecificCredentialStoreToken + ", resourceType="
                + resourceType + ", specificPreferences=" + specificPreferences + "}";
    }
}
