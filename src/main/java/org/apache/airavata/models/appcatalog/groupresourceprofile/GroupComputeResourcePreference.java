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
package org.apache.airavata.models.appcatalog.groupresourceprofile;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference}.
 *
 * @param specificPreferences {@code null} if unset, matching the generated {@code hasSpecificPreferences()}
 *     presence check.
 */
public record GroupComputeResourcePreference(
        String computeResourceId,
        String groupResourceProfileId,
        boolean overrideByAiravata,
        String loginUserName,
        String scratchLocation,
        String resourceSpecificCredentialStoreToken,
        ResourceType resourceType,
        EnvironmentSpecificPreferences specificPreferences) {

    public boolean hasSpecificPreferences() {
        return specificPreferences != null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String computeResourceId = "";
        private String groupResourceProfileId = "";
        private boolean overrideByAiravata;
        private String loginUserName = "";
        private String scratchLocation = "";
        private String resourceSpecificCredentialStoreToken = "";
        private ResourceType resourceType = ResourceType.RESOURCE_TYPE_UNKNOWN;
        private EnvironmentSpecificPreferences specificPreferences;

        private Builder() {}

        private Builder(GroupComputeResourcePreference source) {
            this.computeResourceId = source.computeResourceId;
            this.groupResourceProfileId = source.groupResourceProfileId;
            this.overrideByAiravata = source.overrideByAiravata;
            this.loginUserName = source.loginUserName;
            this.scratchLocation = source.scratchLocation;
            this.resourceSpecificCredentialStoreToken = source.resourceSpecificCredentialStoreToken;
            this.resourceType = source.resourceType;
            this.specificPreferences = source.specificPreferences;
        }

        public Builder setComputeResourceId(String computeResourceId) {
            this.computeResourceId = computeResourceId;
            return this;
        }

        public Builder setGroupResourceProfileId(String groupResourceProfileId) {
            this.groupResourceProfileId = groupResourceProfileId;
            return this;
        }

        public Builder setOverrideByAiravata(boolean overrideByAiravata) {
            this.overrideByAiravata = overrideByAiravata;
            return this;
        }

        public Builder setLoginUserName(String loginUserName) {
            this.loginUserName = loginUserName;
            return this;
        }

        public Builder setScratchLocation(String scratchLocation) {
            this.scratchLocation = scratchLocation;
            return this;
        }

        public Builder setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
            this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
            return this;
        }

        public Builder setResourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder setSpecificPreferences(EnvironmentSpecificPreferences specificPreferences) {
            this.specificPreferences = specificPreferences;
            return this;
        }

        public GroupComputeResourcePreference build() {
            return new GroupComputeResourcePreference(
                    computeResourceId, groupResourceProfileId, overrideByAiravata, loginUserName, scratchLocation,
                    resourceSpecificCredentialStoreToken, resourceType, specificPreferences);
        }
    }
}
