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
 * {@code org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupAccountSSHProvisionerConfig}.
 */
public record GroupAccountSSHProvisionerConfig(
        String resourceId, String groupResourceProfileId, String configName, String configValue) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String resourceId = "";
        private String groupResourceProfileId = "";
        private String configName = "";
        private String configValue = "";

        private Builder() {}

        private Builder(GroupAccountSSHProvisionerConfig source) {
            this.resourceId = source.resourceId;
            this.groupResourceProfileId = source.groupResourceProfileId;
            this.configName = source.configName;
            this.configValue = source.configValue;
        }

        public Builder setResourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder setGroupResourceProfileId(String groupResourceProfileId) {
            this.groupResourceProfileId = groupResourceProfileId;
            return this;
        }

        public Builder setConfigName(String configName) {
            this.configName = configName;
            return this;
        }

        public Builder setConfigValue(String configValue) {
            this.configValue = configValue;
            return this;
        }

        public GroupAccountSSHProvisionerConfig build() {
            return new GroupAccountSSHProvisionerConfig(resourceId, groupResourceProfileId, configName, configValue);
        }
    }
}
