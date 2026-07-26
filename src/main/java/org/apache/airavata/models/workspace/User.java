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
package org.apache.airavata.models.workspace;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.workspace.proto.User}.
 */
public record User(
        String airavataInternalUserId, String userName, String gatewayId, String firstName, String lastName,
        String email) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String airavataInternalUserId = "";
        private String userName = "";
        private String gatewayId = "";
        private String firstName = "";
        private String lastName = "";
        private String email = "";

        private Builder() {}

        private Builder(User source) {
            this.airavataInternalUserId = source.airavataInternalUserId;
            this.userName = source.userName;
            this.gatewayId = source.gatewayId;
            this.firstName = source.firstName;
            this.lastName = source.lastName;
            this.email = source.email;
        }

        public Builder setAiravataInternalUserId(String airavataInternalUserId) {
            this.airavataInternalUserId = airavataInternalUserId;
            return this;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public User build() {
            return new User(airavataInternalUserId, userName, gatewayId, firstName, lastName, email);
        }
    }
}
