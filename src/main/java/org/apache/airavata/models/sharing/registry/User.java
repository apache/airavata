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
package org.apache.airavata.models.sharing.registry;

/**
 * User is the model used to register a user in the system.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.sharing.registry.models.proto.User}. Profile data (first/last name,
 * email, icon) lives exclusively in iam-service UserProfile and is not part of this type.
 *
 * @param userId User id provided by the client.
 * @param domainId Domain id for that user.
 * @param userName User name for the user.
 * @param createdTime If client provides this value then the system will use it, otherwise the
 *     current time will be set.
 * @param updatedTime If client provides this value then the system will use it, otherwise the
 *     current time will be set.
 */
public record User(String userId, String domainId, String userName, long createdTime, long updatedTime) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String userId = "";
        private String domainId = "";
        private String userName = "";
        private long createdTime;
        private long updatedTime;

        private Builder() {}

        private Builder(User source) {
            this.userId = source.userId;
            this.domainId = source.domainId;
            this.userName = source.userName;
            this.createdTime = source.createdTime;
            this.updatedTime = source.updatedTime;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setDomainId(String domainId) {
            this.domainId = domainId;
            return this;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder setCreatedTime(long createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public Builder setUpdatedTime(long updatedTime) {
            this.updatedTime = updatedTime;
            return this;
        }

        public User build() {
            return new User(userId, domainId, userName, createdTime, updatedTime);
        }
    }
}
