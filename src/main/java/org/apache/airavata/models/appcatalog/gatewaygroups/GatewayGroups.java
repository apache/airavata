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
package org.apache.airavata.models.appcatalog.gatewaygroups;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups}.
 */
public record GatewayGroups(
        String gatewayId, String adminsGroupId, String readOnlyAdminsGroupId, String defaultGatewayUsersGroupId) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String gatewayId = "";
        private String adminsGroupId = "";
        private String readOnlyAdminsGroupId = "";
        private String defaultGatewayUsersGroupId = "";

        private Builder() {}

        private Builder(GatewayGroups source) {
            this.gatewayId = source.gatewayId;
            this.adminsGroupId = source.adminsGroupId;
            this.readOnlyAdminsGroupId = source.readOnlyAdminsGroupId;
            this.defaultGatewayUsersGroupId = source.defaultGatewayUsersGroupId;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setAdminsGroupId(String adminsGroupId) {
            this.adminsGroupId = adminsGroupId;
            return this;
        }

        public Builder setReadOnlyAdminsGroupId(String readOnlyAdminsGroupId) {
            this.readOnlyAdminsGroupId = readOnlyAdminsGroupId;
            return this;
        }

        public Builder setDefaultGatewayUsersGroupId(String defaultGatewayUsersGroupId) {
            this.defaultGatewayUsersGroupId = defaultGatewayUsersGroupId;
            return this;
        }

        public GatewayGroups build() {
            return new GatewayGroups(
                    gatewayId, adminsGroupId, readOnlyAdminsGroupId, defaultGatewayUsersGroupId);
        }
    }
}
