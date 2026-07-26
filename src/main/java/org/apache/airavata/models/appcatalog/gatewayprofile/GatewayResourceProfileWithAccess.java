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
package org.apache.airavata.models.appcatalog.gatewayprofile;

import org.apache.airavata.models.commons.AccessFlags;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.api.gatewayprofile.GatewayResourceProfileWithAccess}.
 *
 * <p>A {@link GatewayResourceProfile} unioned with the caller's access flags. A gateway resource
 * profile is a gateway-level entity with no owner and no sharing entity, so {@code isOwner} is
 * always false and {@code userHasWriteAccess} reflects gateway-admin (admin-rw).
 */
public record GatewayResourceProfileWithAccess(GatewayResourceProfile gatewayResourceProfile, AccessFlags access) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private GatewayResourceProfile gatewayResourceProfile;
        private AccessFlags access;

        private Builder() {}

        private Builder(GatewayResourceProfileWithAccess source) {
            this.gatewayResourceProfile = source.gatewayResourceProfile;
            this.access = source.access;
        }

        public Builder setGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) {
            this.gatewayResourceProfile = gatewayResourceProfile;
            return this;
        }

        public Builder setAccess(AccessFlags access) {
            this.access = access;
            return this;
        }

        public GatewayResourceProfileWithAccess build() {
            return new GatewayResourceProfileWithAccess(gatewayResourceProfile, access);
        }
    }
}
