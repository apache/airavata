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
package org.apache.airavata.models.commons;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.commons.proto.AccessFlags}.
 *
 * <p>The caller's sharing-access to a resource, computed and enforced server-side. Reused by every
 * {@code *WithAccess} wrapper so clients never recompute access from extra sharing round-trips.
 * {@code isOwner} = the caller owns the resource; {@code userHasWriteAccess} = isOwner OR the
 * caller holds a WRITE sharing grant.
 */
public record AccessFlags(boolean isOwner, boolean userHasWriteAccess) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private boolean isOwner;
        private boolean userHasWriteAccess;

        private Builder() {}

        private Builder(AccessFlags source) {
            this.isOwner = source.isOwner;
            this.userHasWriteAccess = source.userHasWriteAccess;
        }

        public Builder setIsOwner(boolean isOwner) {
            this.isOwner = isOwner;
            return this;
        }

        public Builder setUserHasWriteAccess(boolean userHasWriteAccess) {
            this.userHasWriteAccess = userHasWriteAccess;
            return this;
        }

        public AccessFlags build() {
            return new AccessFlags(isOwner, userHasWriteAccess);
        }
    }
}
