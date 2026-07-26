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
package org.apache.airavata.models.appcatalog.accountprovisioning;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.accountprovisioning.proto.SSHAccountProvisionerConfigParam}.
 */
public record SSHAccountProvisionerConfigParam(
        String name, SSHAccountProvisionerConfigParamType type, boolean isOptional, String description) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String name = "";
        private SSHAccountProvisionerConfigParamType type =
                SSHAccountProvisionerConfigParamType.SSH_ACCOUNT_PROVISIONER_CONFIG_PARAM_TYPE_UNKNOWN;
        private boolean isOptional;
        private String description = "";

        private Builder() {}

        private Builder(SSHAccountProvisionerConfigParam source) {
            this.name = source.name;
            this.type = source.type;
            this.isOptional = source.isOptional;
            this.description = source.description;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(SSHAccountProvisionerConfigParamType type) {
            this.type = type;
            return this;
        }

        public Builder setIsOptional(boolean isOptional) {
            this.isOptional = isOptional;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public SSHAccountProvisionerConfigParam build() {
            return new SSHAccountProvisionerConfigParam(name, type, isOptional, description);
        }
    }
}
